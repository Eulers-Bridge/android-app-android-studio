package com.eulersbridge.isegoria;

import java.util.ArrayList;

import android.app.Application;

public class Isegoria extends Application {
	private MainActivity mainActivity;
	protected Network network;
	private boolean loggedIn = false;
	private String username = "";
	private String password = "";
	private FeedFragment feedFragment;
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
		mainActivity.runOnUiThread(new Runnable() {
		     @Override
		     public void run() {
		    	 mainActivity.hideDialog();
		    	 mainActivity.switchContent(new FeedFragment());
		     }
		});
	}
	
	public void signupSucceded() {
		mainActivity.showSignupSucceded();
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
		return loggedIn;
	}

	public void setLoggedIn(boolean loggedIn) {
		this.loggedIn = loggedIn;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	public void login() {
		network = new Network(this, username, password);
		network.login();
	}
}
