package com.eulersbridge.isegoria;

import android.app.Application;
import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationManagerCompat;

import com.eulersbridge.isegoria.network.NetworkService;
import com.eulersbridge.isegoria.network.api.API;
import com.eulersbridge.isegoria.network.api.models.User;
import com.eulersbridge.isegoria.util.Constants;
import com.securepreferences.SecurePreferences;

import java.util.Arrays;

public class IsegoriaApp extends Application {

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

                ShortcutInfo election = new ShortcutInfo.Builder(this, Constants.SHORTCUT_ACTION_ELECTION)
                        .setShortLabel(getString(R.string.shortcut_view_latest_election_label_short))
                        .setLongLabel(getString(R.string.shortcut_view_latest_election_label_long))
                        .setIcon(Icon.createWithResource(this, R.drawable.electionblue))
                        .setRank(1)
                        .setIntent(new Intent(this, MainActivity.class)
                                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                        .setAction(Constants.SHORTCUT_ACTION_ELECTION)
                        )
                        .build();

                ShortcutInfo friends = new ShortcutInfo.Builder(this, Constants.SHORTCUT_ACTION_FRIENDS)
                        .setShortLabel(getString(R.string.shortcut_add_friend_label_short))
                        .setLongLabel(getString(R.string.shortcut_add_friend_label_long))
                        .setIcon(Icon.createWithResource(this, R.drawable.friends))
                        .setRank(2)
                        .setIntent(new Intent(this, MainActivity.class)
                                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                        .setAction(Constants.SHORTCUT_ACTION_FRIENDS)
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

    public void showVerification() {
        mainActivity.showVerification();
    }
	
	public @NonNull NetworkService getNetworkService() {
        if (network == null)
            network = new NetworkService(this);

		return network;
	}

	public API getAPI() {
	    return getNetworkService().getAPI();
    }

	public User getLoggedInUser() {
		return loggedInUser;
	}

	public void updateLoggedInUser(@NonNull User updatedUser) {
        loggedInUser = updatedUser;
    }

	public void setLoggedInUser(@NonNull User user, @NonNull String password) {

		loggedInUser = user;

        new SecurePreferences(getApplicationContext())
                .edit()
                .putString(Constants.USER_EMAIL_KEY, loggedInUser.email)
                .putString(Constants.USER_PASSWORD_KEY, password)
                .apply();

        setupAppShortcuts();
	}

	public void login(@NonNull String email, @NonNull String password) {
        getNetworkService().login(email, password);
    }

	public void logOut() {
		loggedInUser = null;

        network.setEmail(null);
        network.setPassword(null);

		new SecurePreferences(getApplicationContext())
				.edit()
				.remove(Constants.USER_PASSWORD_KEY)
				.apply();

		// Remove any notifications that are still visible
        NotificationManagerCompat manager = NotificationManagerCompat.from(getApplicationContext());
        manager.cancelAll();

        // Remove all app long-press shortcuts
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N_MR1) {
            ShortcutManager shortcutManager = getSystemService(ShortcutManager.class);

            if (shortcutManager != null)
                shortcutManager.removeAllDynamicShortcuts();
        }

		getMainActivity().showLogin();
	}

	public void setTrackingOff(boolean trackingOff) {
		loggedInUser.setTrackingOff(trackingOff);
	}

	public void setOptedOutOfDataCollection(boolean optedOutOfDataCollection) {
		loggedInUser.setOptedOutOfDataCollection(optedOutOfDataCollection);
	}

	public void onUserSelfEfficacyCompleted() {
        loggedInUser.setPPSEQuestionsCompleted();
	}
}
