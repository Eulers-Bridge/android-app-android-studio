package com.eulersbridge.isegoria;

import android.app.Application;

import com.eulersbridge.isegoria.feed.FeedFragment;
import com.eulersbridge.isegoria.login.EmailVerificationFragment;
import com.eulersbridge.isegoria.login.PersonalityQuestionsFragment;
import com.eulersbridge.isegoria.models.Country;
import com.eulersbridge.isegoria.models.UserProfile;
import com.eulersbridge.isegoria.network.API;
import com.eulersbridge.isegoria.network.AuthenticationInterceptor;
import com.eulersbridge.isegoria.network.Network;
import com.eulersbridge.isegoria.network.UnwrapConverterFactory;
import com.google.gson.JsonObject;
import com.securepreferences.SecurePreferences;

import java.io.IOException;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Isegoria extends Application {

	private MainActivity mainActivity;
	private Network network;

	private OkHttpClient httpClient;
    private final GsonConverterFactory gsonConverterFactory;
	private API apiService;

	private UserProfile loggedInUser;

	private String username = "";
	private String password = "";

	private List<Country> countryObjects;
	
	public Isegoria() {
		super();

        gsonConverterFactory = GsonConverterFactory.create();
	}
	
	public MainActivity getMainActivity() {
		return mainActivity;
	}
	
	public void setMainActivity(MainActivity mainActivity) {
		this.mainActivity = mainActivity;
	}
	
	public List<Country> getCountryObjects() {
		return countryObjects;
	}

	public void setCountryObjects(List<Country> countryObjects) {
		this.countryObjects = countryObjects;
	}

	public void setFeedFragment() {
		mainActivity.runOnUiThread(() -> {
            mainActivity.hideDialog();

            mainActivity.setNavigationDrawerEnabled(true);
            mainActivity.setToolbarVisible(true);

            final FeedFragment feedFragment = new FeedFragment();
            feedFragment.setTabLayout(mainActivity.getTabLayout());

            mainActivity.switchContent(feedFragment);
        });
	}

    public void setVerification() {
        mainActivity.runOnUiThread(() -> {
            mainActivity.hideDialog();
            mainActivity.switchContent(new EmailVerificationFragment());
        });
    }

    public void setPersonality() {
        mainActivity.runOnUiThread(() -> {
            mainActivity.hideDialog();

            PersonalityQuestionsFragment personalityQuestionsFragment = new PersonalityQuestionsFragment();
            personalityQuestionsFragment.setTabLayout(mainActivity.getTabLayout());
            mainActivity.switchContent(personalityQuestionsFragment);
        });
    }

	public void signupSucceeded() {
		mainActivity.showSignupSucceeded();
	}
	
	public void signupFailed() {
		mainActivity.showSignupFailed();
	}
	
	public void loginFailed() {
		mainActivity.hideDialog();
		mainActivity.showLoginFailed();
	}
	
	public void setNetwork(Network network) {
		this.network = network;
	}
	
	public Network getNetwork() {
		return network;
	}

	public UserProfile getLoggedInUser() {
		return loggedInUser;
	}

	public void setLoggedInUser(UserProfile user) {
		loggedInUser = user;

		new SecurePreferences(getApplicationContext())
				.edit()
				.putString("userEmail", loggedInUser.email)
				.putString("userPassword", loggedInUser.getPassword())
				.apply();

		if (getAPI() != null) {

		    Runnable runnable = () -> {
                try {
                    Call<JsonObject> call = getAPI().getInstitutionNewsFeed(loggedInUser.institutionId);
                    Response<JsonObject> response = call.execute();

                    if (response.isSuccessful()) {
                        JsonObject body = response.body();

                        if (body != null) {
                            long newsFeedId = body.get("nodeId").getAsLong();

                            loggedInUser.setNewsFeedId(newsFeedId);
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            };

            Thread thread = new Thread(runnable);
		    thread.start();
        }
	}

	public void logOut() {
		loggedInUser = null;

		new SecurePreferences(getApplicationContext())
				.edit()
				.remove("userPassword")
				.apply();


		getMainActivity().showLogin();
	}

	public void setTrackingOff(boolean trackingOff) {
		loggedInUser.setTrackingOff(trackingOff);
	}

	public void setOptedOutOfDataCollection(boolean optedOutOfDataCollection) {
		loggedInUser.setOptedOutOfDataCollection(optedOutOfDataCollection);
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	public void login() {
	    createHttpClient();
        createAPI();

		network = new Network(this, username, password);
		network.login();
	}

	private void createHttpClient() {
        OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder()
                .addInterceptor(new AuthenticationInterceptor(username, password));

        if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BASIC);

            //For more detailed debug logging, uncomment the following line:
            //logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            httpClientBuilder.addInterceptor(logging);
        }

        httpClient = httpClientBuilder.build();
    }

	private void createAPI() {
		Retrofit retrofit = new Retrofit.Builder()
				.client(httpClient)
				.baseUrl("http://54.79.70.241:8080/dbInterface/api/")
				.addConverterFactory(new UnwrapConverterFactory(gsonConverterFactory))
				.addConverterFactory(gsonConverterFactory)
				.build();

		apiService = retrofit.create(API.class);
	}

	public API getAPI() {
		return apiService;
	}
}
