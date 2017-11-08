package com.eulersbridge.isegoria.network;

import android.util.Log;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.android.volley.Cache;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.eulersbridge.isegoria.Isegoria;
import com.eulersbridge.isegoria.IsegoriaFirebaseInstanceIDService;
import com.eulersbridge.isegoria.models.UserProfile;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;
import retrofit2.Call;
import retrofit2.Callback;

public class Network {
	private static final String SERVER_URL = "http://54.79.70.241:8080/dbInterface/api";
	private static String PICTURE_URL = "https://s3-ap-southeast-2.amazonaws.com/isegoria/";

    private static final int socketTimeout = 30000; //30 seconds

    private String password;

    private final Isegoria application;

    private int electionId;
    private int userDPId;

    private RequestQueue mRequestQueue;

	public Network(Isegoria application) {
        this.application = application;
	}

	public Network(Isegoria application, String username, String password) {
		this.application = application;
        this.password = password;

        AuthorisedJsonObjectRequest.username = username;
        AuthorisedJsonObjectRequest.password = password;

        Cache cache = new DiskBasedCache(application.getCacheDir(), 1024 * 1024); // 1MB cap
        BasicNetwork network = new BasicNetwork(new HurlStack());

        //For detailed debug network logging, uncomment the following line:
        //VolleyLog.DEBUG = true;

        mRequestQueue = new RequestQueue(cache, network);
        mRequestQueue.start();
	}

	private void setLoggedInUser(UserProfile user) {
        application.setLoggedInUser(user);
    }

	private UserProfile getLoggedInUser() {
        return application.getLoggedInUser();
    }

    public void setTrackingOff(boolean trackingOff) {
        application.setTrackingOff(trackingOff);
    }

    public void setOptedOutOfDataCollection(boolean optedOutOfDataCollection) {
        application.setOptedOutOfDataCollection(optedOutOfDataCollection);
    }

	public void login() {
        String snsPlatformArn = IsegoriaFirebaseInstanceIDService.SNSPlatformApplicationArn;
        String deviceToken = FirebaseInstanceId.getInstance().getToken();

        application.getAPI().attemptLogin(snsPlatformArn, deviceToken).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, retrofit2.Response<LoginResponse> response) {
                boolean userAccountVerified = false;

                if (response.isSuccessful()) {
                    LoginResponse loginResponse = response.body();

                    if (loginResponse != null) {
                        UserProfile user = loginResponse.user;

                        if (user.id < 1) user.id = loginResponse.userId;

                        user.setPassword(password);

                        setLoggedInUser(user);

                        if (user.accountVerified) {
                            userAccountVerified = true;

                            if (user.hasPersonality) {
                                application.setFeedFragment();
                            } else {
                                application.setPersonality();
                            }
                        }
                    }
                }

                if (!userAccountVerified) {
                    application.setVerification();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                t.printStackTrace();
                application.getMainActivity().runOnUiThread(application::loginFailed);
            }
        });
    }

    public void signUp(String firstName, String lastName, String gender, String country, String yearOfBirth, String email, String password, String confirmPassword, long institution) {
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
                .url(SERVER_URL + "/signUp")
                .method("POST", requestBody)
                .addHeader("Accept", "application/json")
                .addHeader("Content-type", "application/json")
                .post(requestBody)
                .build();

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
                application.getMainActivity().runOnUiThread(application::signupSucceeded);
            } else {
                application.getMainActivity().runOnUiThread(application::signupFailed);
            }
        }
    }

    public void s3Upload(final File file) {
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
        final String filename = String.valueOf(getLoggedInUser().id) + "_" + String.valueOf(timestamp) + "_" + file.getName();
        TransferObserver observer = transferUtility.upload("isegoriauserpics", filename, file);
        observer.setTransferListener(new TransferListener() {
            @Override
            public void onStateChanged(int id, TransferState state) {
                if (state == TransferState.COMPLETED) {
                    updateDisplayPicturePhoto("https://s3.amazonaws.com/isegoriauserpics/" + filename);
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
        String url = String.format("%s/photo", SERVER_URL);
        long timestamp = System.currentTimeMillis() / 1000L;

        UserProfile loggedInUser = getLoggedInUser();

        HashMap<String, String> params = new HashMap<>();
        params.put("url", pictureURL);
        params.put("thumbNailUrl", pictureURL);
        params.put("title", "Profile Picture");
        params.put("description", "Profile Picture");
        params.put("date", String.valueOf(timestamp));
        params.put("ownerId", String.valueOf(loggedInUser.id));
        params.put("sequence", String.valueOf("0"));

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(Request.Method.POST, url, new JSONObject(params),
                response -> {
                    try {
                        String result = response.toString();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }, error -> Log.e("Volley", error.toString()));


        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);

        Log.d("VolleyRequest", req.toString());
    }
}
