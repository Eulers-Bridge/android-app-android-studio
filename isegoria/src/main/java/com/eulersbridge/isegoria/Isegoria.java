package com.eulersbridge.isegoria;

import android.app.Application;
import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationManagerCompat;

import com.eulersbridge.isegoria.login.EmailVerificationFragment;
import com.eulersbridge.isegoria.models.User;
import com.eulersbridge.isegoria.network.API;
import com.eulersbridge.isegoria.network.NetworkService;
import com.eulersbridge.isegoria.network.NewsFeedResponse;
import com.securepreferences.SecurePreferences;

import java.util.Arrays;

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

	private void setupAppShortcuts() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            ShortcutManager shortcutManager = getSystemService(ShortcutManager.class);

            if (shortcutManager != null) {

                ShortcutInfo election = new ShortcutInfo.Builder(this, Constant.SHORTCUT_ACTION_ELECTION)
                        .setShortLabel(getString(R.string.section_title_election))
                        .setLongLabel("View the latest election")
                        .setIcon(Icon.createWithResource(this, R.drawable.electionblue))
                        .setRank(1)
                        .setIntent(new Intent(this, MainActivity.class)
                                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                        .setAction(Constant.SHORTCUT_ACTION_ELECTION)
                        )
                        .build();

                ShortcutInfo friends = new ShortcutInfo.Builder(this, Constant.SHORTCUT_ACTION_FRIENDS)
                        .setShortLabel(getString(R.string.section_title_friends))
                        .setLongLabel("Add a friend")
                        .setIcon(Icon.createWithResource(this, R.drawable.friends))
                        .setRank(2)
                        .setIntent(new Intent(this, MainActivity.class)
                                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                        .setAction(Constant.SHORTCUT_ACTION_FRIENDS)
                        )
                        .build();

                shortcutManager.setDynamicShortcuts(Arrays.asList(election, friends));
            }
        }
    }

    public void onLoginSuccess() {
        mainActivity.onLoginSuccess(loggedInUser);
    }

    public void onLoginFailure() {
        mainActivity.onLoginFailure();
    }

    public void setVerification() {
        mainActivity.runOnUiThread(() -> {
            mainActivity.hideDialog();
            mainActivity.switchContent(new EmailVerificationFragment());
        });
    }

	public void onSignUpSuccess() {
		mainActivity.onSignUpSuccess();
	}
	
	public void onSignUpFailure() {
		mainActivity.onSignUpFailure();
	}
	
	public @NonNull NetworkService getNetworkService() {
        if (network == null) {
            network = new NetworkService(this);
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

        setupAppShortcuts();

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

        // Remove all app long-press shortcuts
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N_MR1) {
            ShortcutManager shortcutManager = getSystemService(ShortcutManager.class);
            if (shortcutManager != null) shortcutManager.removeAllDynamicShortcuts();
        }

		getMainActivity().showLogin();
	}

	public void setTrackingOff(boolean trackingOff) {
		loggedInUser.setTrackingOff(trackingOff);
	}

	public void setOptedOutOfDataCollection(boolean optedOutOfDataCollection) {
		loggedInUser.setOptedOutOfDataCollection(optedOutOfDataCollection);
	}
}
