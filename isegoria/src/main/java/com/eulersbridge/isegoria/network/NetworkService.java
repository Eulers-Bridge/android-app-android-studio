package com.eulersbridge.isegoria.network;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Transformations;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.eulersbridge.isegoria.BuildConfig;
import com.eulersbridge.isegoria.IsegoriaApp;
import com.eulersbridge.isegoria.auth.signup.SignUpUser;
import com.eulersbridge.isegoria.network.adapters.LenientLongAdapter;
import com.eulersbridge.isegoria.network.adapters.NullPrimitiveAdapter;
import com.eulersbridge.isegoria.network.adapters.TimestampAdapter;
import com.eulersbridge.isegoria.network.api.API;
import com.eulersbridge.isegoria.network.api.models.ClientInstitution;
import com.eulersbridge.isegoria.network.api.models.Institution;
import com.eulersbridge.isegoria.network.api.models.User;
import com.eulersbridge.isegoria.network.api.responses.LoginResponse;
import com.eulersbridge.isegoria.util.Constants;
import com.eulersbridge.isegoria.util.Utils;
import com.eulersbridge.isegoria.util.data.FixedData;
import com.eulersbridge.isegoria.util.data.RetrofitLiveData;
import com.google.firebase.iid.FirebaseInstanceId;
import com.squareup.moshi.Moshi;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;

public class NetworkService {

    private static final String SERVER_URL = "http://54.79.70.241:8080/dbInterface/api/";

    private static final String S3_PICTURES_BUCKET_NAME = "isegoriauserpics";
    private static final String S3_PICTURES_PATH = "https://s3.amazonaws.com/isegoriauserpics/";

    private String apiBaseURL = SERVER_URL;

    private final IsegoriaApp application;

    private final File cacheDirectory;
    private OkHttpClient httpClient;
    private final MoshiConverterFactory moshiConverterFactory;
    private API api;

    private String email;
    private String password;

    private boolean needsSetup;

    public NetworkService(@NonNull IsegoriaApp application) {
        this.application = application;

        cacheDirectory = new File(application.getCacheDir(), "network");

        Moshi moshi = new Moshi.Builder()
                .add(new LenientLongAdapter())
                .add(new NullPrimitiveAdapter())
                .add(new TimestampAdapter())
                .build();

        moshiConverterFactory = MoshiConverterFactory.create(moshi);

        needsSetup = true;
	}

	public void setEmail(String email) {
        this.email = email;
        needsSetup = true;
    }

    public void setPassword(String password) {
        this.password = password;
        needsSetup = true;
    }

    private void setup() {
        if (needsSetup) {
            createHttpClient();
            createAPI();
        }
    }

    private void createHttpClient() {
        // Force caching of GET requests for 1 minute, or 5 minutes if no network connection.
        Interceptor interceptor = chain -> {
            Request request = chain.request();

            Response response = chain.proceed(request);

            if (request.method().equals("GET")) {
                String cacheHeaderValue = Utils.isNetworkAvailable(application)
                        ? "public, max-age=60" // 60 seconds / 1 minute
                        : "public, only-if-cached, max-stale=300"; // 300 seconds / 5 minutes

                return response.newBuilder()
                        .removeHeader("Pragma")
                        .removeHeader("Cache-Control")
                        .header("Cache-Control", cacheHeaderValue)
                        .build();
            } else {
                return response;
            }
        };

        int cacheSize = 40 * 1024 * 1024; // Maximum cache size of 40 MiB
        Cache cache = new Cache(cacheDirectory, cacheSize);

        OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Request.Builder request = chain.request().newBuilder()
                            .addHeader("Accept", "application/json")
                            .addHeader("Content-Type", "application/json")
                            .addHeader("User-Agent", "IsegoriaApp Android");

                    return chain.proceed(request.build());
                })
                .addInterceptor(interceptor)
                .addNetworkInterceptor(interceptor)
                .cache(cache);

        if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BASIC);

            //For more detailed debug logging, uncomment the following line:
            //logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            httpClientBuilder.addInterceptor(logging);
        }

        if (!TextUtils.isEmpty(password))
            httpClientBuilder.addInterceptor(new AuthenticationInterceptor(email, password));

        httpClient = httpClientBuilder.build();
    }

    private void createAPI() {
        Retrofit retrofit = new Retrofit.Builder()
                .client(httpClient)
                .baseUrl(apiBaseURL)
                .addConverterFactory(new UnwrapConverterFactory(moshiConverterFactory))
                .addConverterFactory(moshiConverterFactory)
                .build();

        api = retrofit.create(API.class);
    }

    public API getAPI() {
        setup();

        return api;
    }

    // Updates the base URL of the API by fetching the API root for the user's institution
    private LiveData<Boolean> updateAPIBaseURL(@NonNull final User user) {
        if (user.institutionId == null) {
            finishedLogin(user);
            return new FixedData<>(false);
        }

        LiveData<Institution> institutionRequest = new RetrofitLiveData<>(api.getInstitution(user.institutionId));
        return Transformations.switchMap(institutionRequest, institution -> {
            if (institution != null) {

                final String institutionName = institution.getName();
                if (!TextUtils.isEmpty(institutionName)) {

                    LiveData<List<ClientInstitution>> clientsRequest = new RetrofitLiveData<>(api.getInstitutionURLs());
                    return Transformations.switchMap(clientsRequest, institutions -> {
                        if (institutions != null) {
                            for (ClientInstitution clientInstitution : institutions) {
                                if (clientInstitution.name.equals(institutionName)
                                        && !TextUtils.isEmpty(clientInstitution.apiRoot)) {

                                    apiBaseURL = clientInstitution.apiRoot + "api/";

                                    // Recreate the API with the new base URL
                                    //createAPI(institution.apiRoot);
                                    createAPI();

                                    finishedLogin(user);

                                    return new FixedData<>(true);
                                }
                            }
                        }
                        return new FixedData<>(false);
                    });
                }
            }

            return new FixedData<>(false);
        });
    }

    // Allow the rest of the first-launch actions to take place
    private void finishedLogin(@NonNull User user) {
        application.setLoggedInUser(user, password);
    }

	public LiveData<Boolean> login(@NonNull String email, @NonNull String password) {
        setEmail(email);
        setPassword(password);

        setup();

        String snsPlatformArn = Constants.SNS_PLATFORM_APPLICATION_ARN;
        String deviceToken = FirebaseInstanceId.getInstance().getToken();

        LiveData<LoginResponse> loginAttempt = new RetrofitLiveData<>(api.attemptLogin(snsPlatformArn, deviceToken));
        return Transformations.switchMap(loginAttempt, response -> {
            boolean success;

            if (response == null) {
                success = false;

            } else {
                success = true;

                User user = response.user;
                user.setId(response.userId);

                if (user.accountVerified) {
                    return updateAPIBaseURL(user);

                } else {
                    application.showVerification();
                }
            }

            return new FixedData<>(success);
        });
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean signUp(@NonNull SignUpUser user) {
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("email", email)
                .addFormDataPart("givenName", user.givenName)
                .addFormDataPart("familyName", user.familyName)
                .addFormDataPart("gender", user.gender)
                .addFormDataPart("nationality", user.nationality)
                .addFormDataPart("yearOfBirth", user.yearOfBirth)
                .addFormDataPart("accountVerified", String.valueOf(user.accountVerified))
                .addFormDataPart("password", password)
                .addFormDataPart("institutionId", String.valueOf(user.institutionId))
                .addFormDataPart("hasPersonality", String.valueOf(user.hasPersonality))
                .build();

        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(SERVER_URL + "signUp")
                .method("POST", requestBody)
                .addHeader("Accept", "application/json")
                .addHeader("Content-type", "application/json")
                .addHeader("User-Agent", "IsegoriaApp Android")
                .post(requestBody)
                .build();

        // Create new HTTP client rather than using application's, as no auth is required
        OkHttpClient httpClient = new OkHttpClient();

        boolean success = false;

        try {
            Response response = httpClient.newCall(request).execute();

            if (response.isSuccessful() && response.body() != null) {
                //noinspection ConstantConditions
                String bodyString = response.body().toString();
                success = (bodyString != null && bodyString.contains(email));
            }

        } catch (IOException e) {
            e.printStackTrace();

        }

        return success;
    }

    public void s3Upload(@NonNull File imageFile) {
        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                application.getApplicationContext(), // Context,
                "715927704730",
                "us-east-1:73ae30c9-393c-44cf-a0ac-049cc0838428",
                "arn:aws:iam::715927704730:role/Cognito_isegoriaUnauth_Role",
                "arn:aws:iam::715927704730:role/Cognito_isegoriaAuth_Role",
                Regions.US_EAST_1 // Region
        );

        AmazonS3Client s3Client = new AmazonS3Client(credentialsProvider);

        TransferUtility transferUtility = TransferUtility.builder()
                .s3Client(s3Client)
                .context(application.getApplicationContext())
                .build();

        int dotIndex = imageFile.getPath().lastIndexOf('.');
        String imageFileExtension = null;

        if (dotIndex > -1)
            imageFileExtension = imageFile.getPath().substring( + 1);

        if (TextUtils.isEmpty(imageFileExtension))
            imageFileExtension = "jpg";

        String key = String.format("%s.%s", UUID.randomUUID().toString(), imageFileExtension);

        TransferObserver observer = transferUtility.upload(S3_PICTURES_BUCKET_NAME, key, imageFile);
        observer.setTransferListener(new TransferListener() {
            @Override
            public void onStateChanged(int id, TransferState state) {
                if (state == TransferState.COMPLETED)
                    updateDisplayPicturePhoto(S3_PICTURES_PATH + key);
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) { }

            @Override
            public void onError(int id, Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    private void updateDisplayPicturePhoto(@NonNull String pictureURL) {
        setup();

        long timestamp = System.currentTimeMillis() / 1000L;
        User loggedInUser = application.loggedInUser.getValue();

        if (loggedInUser == null)
            return;

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("url", pictureURL)
                .addFormDataPart("thumbNailUrl", pictureURL)
                .addFormDataPart("title", "Profile Picture")
                .addFormDataPart("description", "Profile Picture")
                .addFormDataPart("date", String.valueOf(timestamp))
                .addFormDataPart("ownerId", String.valueOf(loggedInUser.email))
                .addFormDataPart("sequence", String.valueOf("0"))
                .build();

        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(apiBaseURL + "photo")
                .method("PUT", requestBody)
                .addHeader("Accept", "application/json")
                .addHeader("Content-type", "application/json")
                .addHeader("User-Agent", "IsegoriaApp Android")
                .post(requestBody)
                .build();

        httpClient.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {
                // Ignored
            }
        });
    }
}
