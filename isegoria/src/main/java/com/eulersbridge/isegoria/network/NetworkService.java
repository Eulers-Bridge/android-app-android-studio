package com.eulersbridge.isegoria.network;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Transformations;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
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
import com.eulersbridge.isegoria.util.data.RetrofitLiveData;
import com.eulersbridge.isegoria.util.data.SingleLiveData;
import com.eulersbridge.isegoria.util.network.SimpleCallback;
import com.google.firebase.iid.FirebaseInstanceId;
import com.squareup.moshi.Moshi;

import java.io.File;
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

    /**
     * Flag to indicate whether HTTP Client & API needed to be created (lazily).
     */
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
                String cacheHeaderValue = Utils.INSTANCE.isNetworkAvailable(application)
                        ? "public, max-age=60" // 60 seconds / 1 minute
                        : "public, only-if-cached, max-stale=300"; // 300 seconds / 5 minutes

                return response.newBuilder()
                        .removeHeader("Pragma")
                        .removeHeader("Cache-Control")
                        .header("Cache-Control", cacheHeaderValue)
                        .build();
            }

            return response;
        };

        final int cacheSize = 40 * 1024 * 1024; // Maximum cache size of 40 MiB
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
    public void updateAPIBaseURL(@NonNull final User user) {
        if (user.institutionId == null)
            return;

        api.getInstitution(user.institutionId).enqueue(new SimpleCallback<Institution>() {
            @Override
            protected void handleResponse(retrofit2.Response<Institution> response) {
                Institution institution = response.body();
                if (institution == null)
                    return;

                final String institutionName = institution.getName();
                if (TextUtils.isEmpty(institutionName))
                    return;

                api.getInstitutionURLs().enqueue(new SimpleCallback<List<ClientInstitution>>() {
                    @Override
                    protected void handleResponse(retrofit2.Response<List<ClientInstitution>> response) {
                        List<ClientInstitution> institutions = response.body();

                        if (institutions == null)
                            return;

                        for (ClientInstitution clientInstitution : institutions) {
                            if (clientInstitution.name.equals(institutionName)
                                    && !TextUtils.isEmpty(clientInstitution.apiRoot)) {

                                apiBaseURL = clientInstitution.apiRoot + "api/";

                                // Recreate the API with the new base URL
                                //createAPI(institution.apiRoot);
                                createAPI();

                                application.setLoggedInUser(user, password);
                            }
                        }
                    }
                });
            }
        });
    }

	public LiveData<LoginResponse> login(@NonNull String email, @NonNull String password) {
        setEmail(email);
        setPassword(password);

        setup();

        final String snsPlatformArn = Constants.SNS_PLATFORM_APPLICATION_ARN;
        final String deviceToken = FirebaseInstanceId.getInstance().getToken();

        return new RetrofitLiveData<>(api.login(snsPlatformArn, deviceToken));
    }

    public LiveData<Boolean> signUp(@NonNull SignUpUser user) {
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("email", email)
                .addFormDataPart("givenName", user.getGivenName())
                .addFormDataPart("familyName", user.getFamilyName())
                .addFormDataPart("gender", user.getGender())
                .addFormDataPart("nationality", user.getNationality())
                .addFormDataPart("yearOfBirth", user.getYearOfBirth())
                .addFormDataPart("accountVerified", String.valueOf(user.getAccountVerified()))
                .addFormDataPart("password", password)
                .addFormDataPart("institutionId", String.valueOf(user.getInstitutionId()))
                .addFormDataPart("hasPersonality", String.valueOf(user.getHasPersonality()))
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
        final OkHttpClient httpClient = new OkHttpClient();

        final LiveData<String> responseBody = new OkHttpLiveData(httpClient.newCall(request));
        return Transformations.switchMap(responseBody, bodyString ->
           new SingleLiveData<>(bodyString != null && bodyString.contains(email))
        );
    }

    public LiveData<Boolean> uploadNewUserPhoto(@NonNull File imageFile) {
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

        LiveData<TransferState> transfer = new AWSTransferLiveData(transferUtility.upload(S3_PICTURES_BUCKET_NAME, key, imageFile));
        return Transformations.switchMap(transfer, state -> {
            if (state == TransferState.COMPLETED) {
                return updateDisplayPicturePhoto(S3_PICTURES_PATH + key);
            } else {
                return new SingleLiveData<>(null);
            }
        });
    }

    private LiveData<Boolean> updateDisplayPicturePhoto(@NonNull String pictureURL) {
        setup();

        long timestamp = System.currentTimeMillis() / 1000L;
        User loggedInUser = application.loggedInUser.getValue();

        if (loggedInUser == null)
            return new SingleLiveData<>(false);

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

        final LiveData<String> updateRequest = new OkHttpLiveData(httpClient.newCall(request));
        return Transformations.switchMap(updateRequest, bodyString ->
            new SingleLiveData<>(bodyString != null)
        );
    }
}
