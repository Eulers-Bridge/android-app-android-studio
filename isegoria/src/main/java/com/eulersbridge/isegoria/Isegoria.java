package com.eulersbridge.isegoria;

import android.app.Application;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationManagerCompat;

import com.eulersbridge.isegoria.feed.FeedFragment;
import com.eulersbridge.isegoria.login.EmailVerificationFragment;
import com.eulersbridge.isegoria.login.PersonalityQuestionsFragment;
import com.eulersbridge.isegoria.models.User;
import com.eulersbridge.isegoria.network.API;
import com.eulersbridge.isegoria.network.NetworkService;
import com.eulersbridge.isegoria.network.NewsFeedResponse;
import com.securepreferences.SecurePreferences;

import retrofit2.Call;
import retrofit2.Response;

public class Isegoria extends Application {

	private MainActivity mainActivity;
	private NetworkService network;

	private User loggedInUser;

    public MainActivity getMainActivity() {
		return mainActivity;
	}
	
	public void setMainActivity(MainActivity mainActivity) {
		this.mainActivity = mainActivity;
	}

	public void setFeedFragment() {
		mainActivity.runOnUiThread(() -> {
            mainActivity.hideDialog();

            mainActivity.setNavigationEnabled(true);
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
	
	public @NonNull NetworkService getNetworkService() {
        if (network == null) {
            network = new NetworkService(this, null, null);
        }

		return network;
	}

	public API getAPI() {
	    return network.getAPI();
    }

	public User getLoggedInUser() {
		return loggedInUser;
	}

	public void setLoggedInUser(User user, String password) {
		loggedInUser = user;

		new SecurePreferences(getApplicationContext())
				.edit()
				.putString("userEmail", loggedInUser.email)
				.putString("userPassword", password)
				.apply();

        Runnable runnable = () -> {
            try {
                Call<NewsFeedResponse> call = network.getAPI().getInstitutionNewsFeed(loggedInUser.institutionId);
                Response<NewsFeedResponse> response = call.execute();

                if (response.isSuccessful()) {
                    NewsFeedResponse body = response.body();

                    if (body != null) {
                        loggedInUser.setNewsFeedId(body.newsFeedId);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        };

        Thread thread = new Thread(runnable);
        thread.start();
	}

	public void login(@Nullable String email, @Nullable String password) {
        getNetworkService().login(email, password);
    }

	public void logOut() {
		loggedInUser = null;

        network.setEmail(null);
        network.setPassword(null);

		new SecurePreferences(getApplicationContext())
				.edit()
				.remove("userPassword")
				.apply();

		// Remove any notifications that are still visible
        NotificationManagerCompat manager = NotificationManagerCompat.from(getApplicationContext());
        manager.cancelAll();

		getMainActivity().showLogin();
	}

	public void setTrackingOff(boolean trackingOff) {
		loggedInUser.setTrackingOff(trackingOff);
	}

	public void setOptedOutOfDataCollection(boolean optedOutOfDataCollection) {
		loggedInUser.setOptedOutOfDataCollection(optedOutOfDataCollection);
	}
}
