package com.eulersbridge.isegoria;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
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
import com.eulersbridge.isegoria.util.Strings;
import com.eulersbridge.isegoria.util.transformation.BlurTransformation;
import com.eulersbridge.isegoria.util.transformation.RoundedCornersTransformation;
import com.securepreferences.SecurePreferences;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class IsegoriaApp extends Application {

	private WeakReference<MainActivity> weakMainActivity;
	private NetworkService network;

	public final MutableLiveData<User> loggedInUser =  new MutableLiveData<>();

    @Override
    public void onCreate() {
        super.onCreate();

        float screenDensity = getResources().getDisplayMetrics().density;
        BlurTransformation.screenDensity = screenDensity;
        RoundedCornersTransformation.screenDensity = screenDensity;

        createNotificationChannels();
    }
	
	public void setMainActivity(MainActivity mainActivity) {
        weakMainActivity = new WeakReference<>(mainActivity);
	}

    private void createNotificationChannels() {
        // Notification channels are only supported on Android O+
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

            //Build a simple map of channel names (Strings) to their importance level (Integer)
            HashMap<String, Integer> channels = new HashMap<>();
            channels.put(Constants.NOTIFICATION_CHANNEL_FRIENDS,
                    NotificationManager.IMPORTANCE_DEFAULT);
            channels.put(Constants.NOTIFICATION_CHANNEL_VOTE_REMINDERS,
                    NotificationManager.IMPORTANCE_DEFAULT);

            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            if (notificationManager != null) {
			    /* Loop through the map, creating notification channels based on the names/importances
			        in the map. `createNotificationChannel` is no-op if the channels have already
			        been created from a previous launch.
			     */
                for (Map.Entry<String, Integer> entry : channels.entrySet()) {
                    String channelName = entry.getKey();
                    String channelId = Strings.notificationChannelIDFromName(channelName);
                    int importance = entry.getValue();

                    NotificationChannel notificationChannel =
                            new NotificationChannel(channelId, channelName, importance);
                    notificationChannel.setShowBadge(true);
                    notificationManager.createNotificationChannel(notificationChannel);
                }
            }
        }
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

    public void showVerification() {
        MainActivity mainActivity = weakMainActivity.get();

        if (mainActivity != null)
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

	public void updateLoggedInUser(@NonNull User updatedUser) {
        loggedInUser.setValue(updatedUser);
    }

	public void setLoggedInUser(@NonNull User user, @NonNull String password) {

        loggedInUser.setValue(user);

        new SecurePreferences(getApplicationContext())
                .edit()
                .putString(Constants.USER_EMAIL_KEY, user.email)
                .putString(Constants.USER_PASSWORD_KEY, password)
                .apply();

        setupAppShortcuts();
	}

	public LiveData<Boolean> login(@NonNull String email, @NonNull String password) {
        return getNetworkService().login(email, password);
    }

	public void logOut() {
		loggedInUser.setValue(null);

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

        MainActivity mainActivity = weakMainActivity.get();

        if (mainActivity != null)
            mainActivity.showLogin();
	}

	public void setTrackingOff(boolean trackingOff) {
        User user = loggedInUser.getValue();
        if (user != null) {
            user.setTrackingOff(trackingOff);
            loggedInUser.setValue(user);
        }
	}

	public void setOptedOutOfDataCollection(boolean optedOutOfDataCollection) {
        User user = loggedInUser.getValue();
        if (user != null) {
            user.setOptedOutOfDataCollection(optedOutOfDataCollection);
            loggedInUser.setValue(user);
        }
	}

	public void onUserSelfEfficacyCompleted() {
        User user = loggedInUser.getValue();
        if (user != null) {
            user.setPPSEQuestionsCompleted();
            loggedInUser.setValue(user);
        }
	}
}
