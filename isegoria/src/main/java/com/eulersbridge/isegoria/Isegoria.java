package com.eulersbridge.isegoria;

import android.app.Application;

import com.eulersbridge.isegoria.feed.FeedFragment;
import com.eulersbridge.isegoria.login.EmailVerificationFragment;
import com.eulersbridge.isegoria.login.PersonalityQuestionsFragment;
import com.eulersbridge.isegoria.models.CountryInfo;
import com.eulersbridge.isegoria.models.User;
import com.securepreferences.SecurePreferences;

import java.util.ArrayList;

public class Isegoria extends Application {

	private MainActivity mainActivity;
	private Network network;

	private boolean isLoggedIn = false;
	private User loggedInUser;

	private String username = "";
	private String password = "";

	private ArrayList<CountryInfo> countryObjects;
	
	public Isegoria() {
		super();
	}
	
	public MainActivity getMainActivity() {
		return mainActivity;
	}
	
	public void setMainActivity(MainActivity mainActivity) {
		this.mainActivity = mainActivity;
	}
	
	public ArrayList<CountryInfo> getCountryObjects() {
		return countryObjects;
	}

	public void setCountryObjects(ArrayList<CountryInfo> countryObjects) {
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

	public boolean isLoggedIn() {
		return isLoggedIn;
	}

	public void setLoggedIn(boolean loggedIn) {
		isLoggedIn = loggedIn;
	}

	public User getLoggedInUser() {
		return loggedInUser;
	}

	public void setLoggedInUser(User user) {
		loggedInUser = user;

		new SecurePreferences(getApplicationContext())
				.edit()
				.putString("userEmail", loggedInUser.getEmail())
				.putString("userPassword", loggedInUser.getPassword())
				.apply();
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
		network = new Network(this, username, password);
		network.login();
	}
}
