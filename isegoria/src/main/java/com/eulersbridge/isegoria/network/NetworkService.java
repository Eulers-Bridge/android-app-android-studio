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
import com.eulersbridge.isegoria.Constant;
import com.eulersbridge.isegoria.Isegoria;
import com.eulersbridge.isegoria.models.User;
import com.google.firebase.iid.FirebaseInstanceId;
import com.squareup.moshi.Moshi;

import java.io.File;
import java.io.IOException;

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

    private final Isegoria application;

    private OkHttpClient httpClient;
    private final MoshiConverterFactory moshiConverterFactory;
    private API api;

    private String email;
    private String password;

    private boolean needsSetup;

    public NetworkService(Isegoria application) {
        this.application = application;

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
        OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Request.Builder request = chain.request().newBuilder()
                            .addHeader("Accept", "application/json")
                            .addHeader("Content-Type", "application/json")
                            .addHeader("User-Agent", "Isegoria Android");

                    return chain.proceed(request.build());
                });

        if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BASIC);

            //For more detailed debug logging, uncomment the following line:
            //logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            httpClientBuilder.addInterceptor(logging);
        }

        if (!TextUtils.isEmpty(password)) {
            httpClientBuilder.addInterceptor(new AuthenticationInterceptor(email, password));
        }

        httpClient = httpClientBuilder.build();
    }

    private void createAPI() {
        Retrofit retrofit = new Retrofit.Builder()
                .client(httpClient)
                .baseUrl(Constant.SERVER_URL)
                .addConverterFactory(new UnwrapConverterFactory(moshiConverterFactory))
                .addConverterFactory(moshiConverterFactory)
                .build();

        api = retrofit.create(API.class);
    }

    public API getAPI() {
        setup();

        return api;
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

                if (response.isSuccessful()) {
                    LoginResponse loginResponse = response.body();

                    if (loginResponse != null) {
                        User user = loginResponse.user;
                        user.setId(loginResponse.userId);

                        application.setLoggedInUser(user, password);

                        if (user.accountVerified) {
                            userAccountVerified = true;

                            application.onLoginSuccess();
                        }
                    }
                }

                if (!userAccountVerified) application.setVerification();
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                t.printStackTrace();
                application.getMainActivity().runOnUiThread(application::onLoginFailure);
            }
        });
    }

    public void signUp(String firstName, String lastName, String gender, String country, String yearOfBirth, String email, String password, long institution) {
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("email", email)
                .addFormDataPart("givenName", firstName)
                .addFormDataPart("familyName", lastName)
                .addFormDataPart("gender", gender)
                .addFormDataPart("nationality", country)
                .addFormDataPart("yearOfBirth",yearOfBirth)
                .addFormDataPart("accountVerified", String.valueOf(false))
                .addFormDataPart("password", password)
                .addFormDataPart("institutionId", String.valueOf(institution))
                .addFormDataPart("hasPersonality", String.valueOf(false))
                .build();

        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(Constant.SERVER_URL + "/signUp")
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

        } finally {
            if (success) {
                application.getMainActivity().runOnUiThread(application::onSignUpSuccess);
            } else {
                application.getMainActivity().runOnUiThread(application::onSignUpFailure);
            }
        }
    }

    public void s3Upload(File file) {
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

        long timestamp = System.currentTimeMillis() / 1000L;
        String filename = String.valueOf(application.getLoggedInUser().email) + "_" + String.valueOf(timestamp) + "_" + file.getName();

        TransferObserver observer = transferUtility.upload(Constant.S3_PICTURES_BUCKET_NAME, filename, file);
        observer.setTransferListener(new TransferListener() {
            @Override
            public void onStateChanged(int id, TransferState state) {
                if (state == TransferState.COMPLETED) {
                    updateDisplayPicturePhoto(Constant.S3_PICTURES_PATH + filename);
                }
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
                .url(Constant.SERVER_URL + "photo")
                .method("PUT", requestBody)
                .addHeader("Accept", "application/json")
                .addHeader("Content-type", "application/json")
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
