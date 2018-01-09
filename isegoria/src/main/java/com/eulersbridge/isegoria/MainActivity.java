package com.eulersbridge.isegoria;


import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ShortcutManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.eulersbridge.isegoria.auth.ConsentAgreementFragment;
import com.eulersbridge.isegoria.auth.EmailVerificationFragment;
import com.eulersbridge.isegoria.auth.LoginFragment;
import com.eulersbridge.isegoria.auth.PersonalityQuestionsActivity;
import com.eulersbridge.isegoria.auth.SignUpFragment;
import com.eulersbridge.isegoria.common.BlurTransformation;
import com.eulersbridge.isegoria.common.Constant;
import com.eulersbridge.isegoria.common.RoundedCornersTransformation;
import com.eulersbridge.isegoria.common.TitledFragment;
import com.eulersbridge.isegoria.common.Utils;
import com.eulersbridge.isegoria.election.ElectionMasterFragment;
import com.eulersbridge.isegoria.feed.FeedFragment;
import com.eulersbridge.isegoria.models.Country;
import com.eulersbridge.isegoria.models.Institution;
import com.eulersbridge.isegoria.models.SignUpUser;
import com.eulersbridge.isegoria.models.User;
import com.eulersbridge.isegoria.network.GeneralInfoResponse;
import com.eulersbridge.isegoria.network.SimpleCallback;
import com.eulersbridge.isegoria.poll.PollsFragment;
import com.eulersbridge.isegoria.profile.FriendsFragment;
import com.eulersbridge.isegoria.profile.ProfileViewPagerFragment;
import com.eulersbridge.isegoria.vote.VoteViewPagerFragment;
import com.google.firebase.FirebaseApp;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.securepreferences.SecurePreferences;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements
        BottomNavigationView.OnNavigationItemSelectedListener, SignUpFragment.SignUpListener, FragmentManager.OnBackStackChangedListener {

    public interface TabbedFragment {
        void setupTabLayout(TabLayout tabLayout);
    }

	private Deque<Fragment> tabFragments;
	private Isegoria application;
	public ProgressDialog dialog;

	private CoordinatorLayout coordinatorLayout;

    private BottomNavigationViewEx navigationView;

	private TabLayout tabLayout;
	
	private SignUpUser signUpUser;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);

		if (savedInstanceState == null) FirebaseApp.initializeApp(this);

		setContentView(R.layout.main_activity);

		application = (Isegoria) getApplicationContext();
		application.setMainActivity(this);

		setupNotificationChannels();

		setupToolbarAndNavigation();

		coordinatorLayout = findViewById(R.id.coordinator_layout);

		float screenDensity = getResources().getDisplayMetrics().density;
		BlurTransformation.screenDensity = screenDensity;
        RoundedCornersTransformation.screenDensity = screenDensity;

		String userEmail = new SecurePreferences(this)
                .getString(Constant.USER_EMAIL_KEY, null);
		String userPassword = new SecurePreferences(this)
                .getString(Constant.USER_PASSWORD_KEY, null);

		getSupportFragmentManager().addOnBackStackChangedListener(this);

		if (userEmail != null && userPassword != null) {
			application.login(userEmail, userPassword);

            setViewEnabled(R.id.login_email, false);
            setViewEnabled(R.id.login_password, false);
            setViewEnabled(R.id.login_button, false);
            setViewEnabled(R.id.login_signup_button, false);

            setToolbarVisible(true);

            // Add 3 empty tabs to flesh out the empty/not loaded feed fragment screen
            tabLayout.addTab(tabLayout.newTab());
            tabLayout.addTab(tabLayout.newTab());
            tabLayout.addTab(tabLayout.newTab());
            tabLayout.setVisibility(View.VISIBLE);

		} else {
			showLogin();
		}
	}

	public CoordinatorLayout getCoordinatorLayout() {
        return coordinatorLayout;
    }

    private boolean handleAppShortcutIntent() {
	    boolean handledShortcut = false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            Intent intent = getIntent();
            if (intent != null) {

                String action = getIntent().getAction();
                if (action != null) {
                    switch (action) {
                        case Constant.SHORTCUT_ACTION_ELECTION:
                            showElection();
                            handledShortcut = true;
                            break;

                        case Constant.SHORTCUT_ACTION_FRIENDS:
                            showFriends();
                            handledShortcut = true;
                            break;
                    }

                    if (handledShortcut) {
                        ShortcutManager shortcutManager = getSystemService(ShortcutManager.class);
                        if (shortcutManager != null) shortcutManager.reportShortcutUsed(action);
                    }
                }
            }
        }

        return handledShortcut;
    }

	private void setupNotificationChannels() {
	    // Notification channels are only supported on Android O+
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

		    //Build a simple map of channel names (Strings) to their importance level (Integer)
			HashMap<String, Integer> channels = new HashMap<>();
			channels.put(Constant.NOTIFICATION_CHANNEL_FRIENDS,
                    NotificationManager.IMPORTANCE_DEFAULT);
			channels.put(Constant.NOTIFICATION_CHANNEL_VOTE_REMINDERS,
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
					String channelId = Utils.notificationChannelIDFromName(channelName);
					int importance = entry.getValue();

					NotificationChannel notificationChannel =
                            new NotificationChannel(channelId, channelName, importance);
					notificationChannel.setShowBadge(true);
					notificationManager.createNotificationChannel(notificationChannel);
				}
			}
		}
	}

	private void setupToolbarAndNavigation() {
		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		navigationView = findViewById(R.id.navigation);
		if (navigationView != null) {
		    navigationView.setOnNavigationItemSelectedListener(this);

            navigationView.enableShiftingMode(false);
            navigationView.setTextVisibility(false);
		}

		tabLayout = findViewById(R.id.tab_layout);

		tabFragments = new ArrayDeque<>(4);
	}

	public void setToolbarShowsTitle(boolean visible) {
        if (getSupportActionBar() != null)
	        getSupportActionBar().setDisplayShowTitleEnabled(visible);
	}

	public void setToolbarTitle(String title) {
	    if (getSupportActionBar() != null)
	        getSupportActionBar().setTitle(title);
	}

	public void setToolbarVisible(boolean visible) {
		if (getSupportActionBar() == null)
		    return;

		if (visible) {
			getSupportActionBar().show();
		} else {
			getSupportActionBar().hide();
		}
	}

    @Override
    public void onBackPressed() {
        if (tabFragments.size() > 1) {
            // Only pop if more than 1 fragment on stack (i.e. always leave a root fragment)

            tabFragments.pop();
            getSupportFragmentManager().popBackStack();

        } else if (tabFragments.size() == 1) {
            // Return to launcher
            Intent launcherAction = new Intent(Intent.ACTION_MAIN);
            launcherAction.addCategory(Intent.CATEGORY_HOME);
            launcherAction.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(launcherAction);
        }
    }

    @Override
    public void onBackStackChanged() {
        boolean canGoBack = (getSupportFragmentManager().getBackStackEntryCount() > 0);

        if (getSupportActionBar() != null) {
            /* When a fragment is added to the stack, show the back button in the app bar,
               as the user can navigate back to a previous fragment. */
            getSupportActionBar().setDisplayHomeAsUpEnabled(canGoBack);
            getSupportActionBar().setDisplayShowHomeEnabled(canGoBack);
        }

        updateAppBarState();
    }

    @Override
	public boolean onSupportNavigateUp() {
	    if (tabFragments.size() > 0)
            tabFragments.pop();

		getSupportFragmentManager().popBackStack();

		return true;
	}

	public TabLayout getTabLayout() {
		return tabLayout;
	}

	private @Nullable Fragment getCurrentFragment() {
	    return tabFragments.size() == 0? null : tabFragments.peekFirst();
    }

	private void showElection() {
	    runOnUiThread(() -> {
            presentRootContent(new ElectionMasterFragment());

            @IdRes int id = R.id.navigation_election;

            if (navigationView.getSelectedItemId() != id)
                navigationView.setSelectedItemId(id);
        });
    }

    public void showFriends() {
        presentContent(new FriendsFragment());
    }

	public void showLogin() {
	    navigationView.setVisibility(View.GONE);
        presentRootContent(new LoginFragment());
	}

	public void setVerification() {
	    hideDialog();
	    presentRootContent(new EmailVerificationFragment());
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		return false;
	}

	@Override
	public boolean onNavigationItemSelected(@NonNull MenuItem item) {

		if (item.isCheckable()) item.setChecked(true);

		final int newNavigationId = item.getItemId();

        Fragment fragment;

        switch(newNavigationId) {
            case R.id.navigation_feed:
                fragment = new FeedFragment();
                break;

            case R.id.navigation_poll:
                fragment = new PollsFragment();
                break;

            case R.id.navigation_vote:
                fragment = new VoteViewPagerFragment();
                break;

            case R.id.navigation_profile:
                fragment = new ProfileViewPagerFragment();
                break;

            case R.id.navigation_election:
                showElection();
                // Fall through to return
            default:
                return true;
        }

        Fragment currentFragment = getCurrentFragment();

        if (currentFragment == null || !fragment.getClass().equals(currentFragment.getClass()))
            presentRootContent(fragment);

		return true;
	}
	
	public Isegoria getIsegoriaApplication() {
		return application;
	}

	// When the Sign Up button in the launch screen is tapped
	public void onSignUpClicked() {
        SignUpFragment signUpFragment = new SignUpFragment();
        signUpFragment.setListener(this);

        // Following code is same as `presentContent()`, but with a simple open transition
        tabFragments.push(signUpFragment);

        FragmentManager fragmentManager = getSupportFragmentManager();

        fragmentManager
                .beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.container, signUpFragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .addToBackStack(null)
                .commit();

        fragmentManager.executePendingTransactions();
	}

	private void setViewEnabled(@IdRes int viewId, boolean enabled) {
		View view = findViewById(viewId);
		if (view != null) view.setEnabled(enabled);
	}

	public void onLoginSuccess(User loggedInUser) {
        hideDialog();

        setToolbarVisible(true);
        navigationView.setVisibility(View.VISIBLE);
        navigationView.setEnabled(true);

        if (!handleAppShortcutIntent())
            navigationView.setSelectedItemId(R.id.navigation_feed);

        if (!loggedInUser.hasPersonality)
            startActivity(new Intent(this, PersonalityQuestionsActivity.class));
    }
	
	private void hideDialog() {
		if (dialog != null) dialog.dismiss();
	}
	
	public void onLoginFailure() {
	    runOnUiThread(() -> {
            setViewEnabled(R.id.login_email, true);
            setViewEnabled(R.id.login_password, true);
            setViewEnabled(R.id.login_button, true);
            setViewEnabled(R.id.login_signup_button, true);

            hideDialog();

            Fragment currentFragment = getCurrentFragment();

            if (currentFragment == null || currentFragment.getClass() != LoginFragment.class) {
                /* Tried to login user based on stored email/password, but they since deleted
                account or the login otherwise failed. Not currently showing the Login fragment,
                so we need to. */

				navigationView.setVisibility(View.GONE);
                setToolbarVisible(false);

                presentRootContent(new LoginFragment());
            }

            Snackbar.make(findViewById(R.id.coordinator_layout),
                    getString(R.string.user_login_error_message), Snackbar.LENGTH_LONG)
                    .setDuration(Constant.SNACKBAR_LENGTH_EXTENDED)
                    .show();
        });
    }
	
	private void onSignUpFailure() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.user_sign_up_error_title))
                .setMessage(getString(R.string.user_sign_up_error_message))
                .setPositiveButton(android.R.string.ok, null)
                .show();
	}

	@Override
	public void onSignUpNextClick(SignUpUser user) {
	    this.signUpUser = user;

        ConsentAgreementFragment consentAgreementFragment = new ConsentAgreementFragment();
        presentContent(consentAgreementFragment);
	}
	
	public void userConsentNext() {
        application.getAPI().getGeneralInfo().enqueue(new SimpleCallback<GeneralInfoResponse>() {
            @Override
            protected void handleResponse(Response<GeneralInfoResponse> response) {
                GeneralInfoResponse body = response.body();
                if (body != null && body.countries.size() > 0) {

                    long institutionId = -1;

                    for (Country country : body.countries) {
                        for (Institution countryInstitution : country.institutions) {
                            if (countryInstitution.getName().equals(signUpUser.institutionName)) {
                                institutionId = countryInstitution.id;
                            }
                        }
                    }

                    signUpUser.institutionId = institutionId;

                    if (!application.getNetworkService().signUp(signUpUser)) onSignUpFailure();
                }
            }
        });

        presentRootContent(new LoginFragment());
	}

	public void presentContent(@NonNull Fragment fragment) {
        tabFragments.push(fragment);

        FragmentManager fragmentManager = getSupportFragmentManager();

        fragmentManager
                .beginTransaction()
                .replace(R.id.container, fragment)
                .addToBackStack(null)
                .commit();

        fragmentManager.executePendingTransactions();
    }

	private void presentRootContent(@NonNull Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.popBackStack();

        tabFragments.clear();
        tabFragments.push(fragment);

        fragmentManager
                .beginTransaction()
                .replace(R.id.container, fragment)
                .commitNow(); // commit() + executePendingTransactions()

        updateAppBarState();
	}

	private void updateAppBarState() {
        final Fragment currentFragment = getCurrentFragment();

        if (currentFragment != null) {
            runOnUiThread(() -> {
                if (currentFragment instanceof TitledFragment) {
                    String fragmentTitle = ((TitledFragment) currentFragment).getTitle(MainActivity.this);

                    if (!TextUtils.isEmpty(fragmentTitle))
                        setToolbarTitle(fragmentTitle);
                }

                if (currentFragment instanceof TabbedFragment) {
                    tabLayout.clearOnTabSelectedListeners();
                    ((TabbedFragment) currentFragment).setupTabLayout(tabLayout);
                }
            });
        }
    }
}