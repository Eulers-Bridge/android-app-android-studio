package com.eulersbridge.isegoria.network;

import android.text.TextUtils;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.eulersbridge.isegoria.BuildConfig;
import com.eulersbridge.isegoria.Isegoria;
import com.eulersbridge.isegoria.common.Constant;
import com.eulersbridge.isegoria.common.Utils;
import com.eulersbridge.isegoria.models.Institution;
import com.eulersbridge.isegoria.models.SignUpUser;
import com.eulersbridge.isegoria.models.User;
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
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;

public class NetworkService {

    private String apiBaseURL = Constant.SERVER_URL;

    private final Isegoria application;

    private final File cacheDirectory;
    private OkHttpClient httpClient;
    private final MoshiConverterFactory moshiConverterFactory;
    private API api;

    private String email;
    private String password;

    private boolean needsSetup;

    public NetworkService(Isegoria application) {
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
                            .addHeader("User-Agent", "Isegoria Android");

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
    private void updateAPIBaseURL(final User user) {
        if (user.institutionId == null) {
            finishedLogin(user);
            return;
        }

        api.getInstitution(user.institutionId).enqueue(new SimpleCallback<Institution>() {
            @Override
            protected void handleResponse(retrofit2.Response<Institution> response) {
                Institution institution = response.body();
                if (institution != null) {

                    final String institutionName = institution.getName();
                    if (!TextUtils.isEmpty(institutionName)) {

                        final SimpleCallback<List<ClientInstitution>> URLsCallback = new SimpleCallback<List<ClientInstitution>>() {
                            @Override
                            protected void handleResponse(retrofit2.Response<List<ClientInstitution>> response) {
                                List<ClientInstitution> institutions = response.body();
                                if (institutions != null) {
                                    for (ClientInstitution institution : institutions) {
                                        if (institution.name.equals(institutionName)
                                                && !TextUtils.isEmpty(institution.apiRoot)) {

                                            apiBaseURL = institution.apiRoot + "api/";

                                            // Recreate the API with the new base URL
                                            //createAPI(institution.apiRoot);
                                            createAPI();

                                            finishedLogin(user);
                                        }
                                    }
                                }
                            }
                        };

                        api.getInstitutionURLs().enqueue(URLsCallback);
                    }
                }
            }
        });
    }

    // Allow the rest of the first-launch actions to take place
    private void finishedLogin(User user) {
        application.setLoggedInUser(user, password);

        if (user.accountVerified) application.onLoginSuccess();
    }

	public void login(String email, String password) {
        setEmail(email);
        setPassword(password);

        setup();

        String snsPlatformArn = Constant.SNS_PLATFORM_APPLICATION_ARN;
        String deviceToken = FirebaseInstanceId.getInstance().getToken();

        api.attemptLogin(snsPlatformArn, deviceToken).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, retrofit2.Response<LoginResponse> response) {
                boolean userAccountVerified = false;
                boolean success = false;

                if (response.isSuccessful()) {
                    LoginResponse loginResponse = response.body();

                    if (loginResponse != null) {
                        success = true;

                        User user = loginResponse.user;
                        user.setId(loginResponse.userId);

                        userAccountVerified = user.accountVerified;

                        if (userAccountVerified) updateAPIBaseURL(user);
                    }
                }

                if (!success) {
                    application.onLoginFailure();

                } else if (!userAccountVerified) {
                    application.setVerification();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                t.printStackTrace();
                application.onLoginFailure();
            }
        });
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean signUp(SignUpUser user) {
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
                .url(Constant.SERVER_URL + "signUp")
                .method("POST", requestBody)
                .addHeader("Accept", "application/json")
                .addHeader("Content-type", "application/json")
                .addHeader("User-Agent", "Isegoria Android")
                .post(requestBody)
                .build();

        // Create new HTTP client rather than using application's, as no auth is required
        OkHttpClient httpClient = new OkHttpClient();

        boolean success = false;

        try {
            Response response = httpClient.newCall(request).execute();

            if (response.isSuccessful() && response.body() != null) {
                String bodyString = response.body().toString();
                success = (bodyString != null && bodyString.contains(email));
            }

        } catch (IOException e) {
            e.printStackTrace();

        }

        return success;
    }

    public void s3Upload(File imageFile) {
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

        String path = imageFile.getPath();
        int dotIndex = path.lastIndexOf('.');

        String imageFileExtension = null;

        if (dotIndex > -1)
            imageFileExtension = imageFile.getPath().substring( + 1);

        if (TextUtils.isEmpty(imageFileExtension))
            imageFileExtension = "jpg";

        String key = String.format("%s.%s", UUID.randomUUID().toString(), imageFileExtension);

        TransferObserver observer = transferUtility.upload(Constant.S3_PICTURES_BUCKET_NAME, key, imageFile);
        observer.setTransferListener(new TransferListener() {
            @Override
            public void onStateChanged(int id, TransferState state) {
                if (state == TransferState.COMPLETED)
                    updateDisplayPicturePhoto(Constant.S3_PICTURES_PATH + key);
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) { }

            @Override
            public void onError(int id, Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    private void updateDisplayPicturePhoto(String pictureURL) {
        setup();

        long timestamp = System.currentTimeMillis() / 1000L;

        User loggedInUser = application.getLoggedInUser();

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
                .addHeader("User-Agent", "Isegoria Android")
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
