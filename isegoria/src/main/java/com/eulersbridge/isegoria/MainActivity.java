package com.eulersbridge.isegoria;


import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ShortcutManager;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.eulersbridge.isegoria.common.Constant;
import com.eulersbridge.isegoria.election.ElectionMasterFragment;
import com.eulersbridge.isegoria.feed.FeedFragment;
import com.eulersbridge.isegoria.auth.LoginFragment;
import com.eulersbridge.isegoria.auth.PersonalityQuestionsActivity;
import com.eulersbridge.isegoria.auth.ConsentAgreementFragment;
import com.eulersbridge.isegoria.auth.SignUpFragment;
import com.eulersbridge.isegoria.models.Country;
import com.eulersbridge.isegoria.models.Institution;
import com.eulersbridge.isegoria.models.SignUpUser;
import com.eulersbridge.isegoria.models.User;
import com.eulersbridge.isegoria.network.GeneralInfoResponse;
import com.eulersbridge.isegoria.network.SimpleCallback;
import com.eulersbridge.isegoria.poll.PollFragment;
import com.eulersbridge.isegoria.profile.FindAddContactFragment;
import com.eulersbridge.isegoria.profile.ProfileViewPagerFragment;
import com.eulersbridge.isegoria.common.TitledFragment;
import com.eulersbridge.isegoria.common.Utils;
import com.eulersbridge.isegoria.vote.VoteViewPagerFragment;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.securepreferences.SecurePreferences;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener,
        SignUpFragment.SignUpListener {

	private TitledFragment mContent;
	private Isegoria application;
	public ProgressDialog dialog;

	private CoordinatorLayout coordinatorLayout;

    private BottomNavigationViewEx navigationView;

	private TextView toolbarTitleTextView;
	private TabLayout tabLayout;
	private @IdRes int currentNavigationId;
	
	private SignUpUser signUpUser;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main_activity);

		application = (Isegoria) getApplicationContext();
		application.setMainActivity(this);

		setupNotificationChannels();

		setupToolbarAndNavigation();

		setNavigationEnabled(false);

		coordinatorLayout = findViewById(R.id.coordinator_layout);

		String userEmail = new SecurePreferences(this).getString(Constant.USER_EMAIL_KEY, null);
		String userPassword = new SecurePreferences(this).getString(Constant.USER_PASSWORD_KEY, null);

		if (userEmail != null && userPassword != null) {
			application.login(userEmail, userPassword);

            setViewEnabled(R.id.login_email, false);
            setViewEnabled(R.id.login_password, false);
            setViewEnabled(R.id.login_button, false);
            setViewEnabled(R.id.login_signup_button, false);

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
			channels.put(Constant.NOTIFICATION_CHANNEL_FRIENDS, NotificationManager.IMPORTANCE_DEFAULT);
			channels.put(Constant.NOTIFICATION_CHANNEL_VOTE_REMINDERS, NotificationManager.IMPORTANCE_DEFAULT);

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

					NotificationChannel notificationChannel = new NotificationChannel(channelId, channelName, importance);
					notificationChannel.setShowBadge(true);
					notificationManager.createNotificationChannel(notificationChannel);
				}
			}
		}
	}

	private void setupToolbarAndNavigation() {
		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		getSupportActionBar().setDisplayShowTitleEnabled(false);

		Typeface titleFont = Typeface.createFromAsset(getAssets(),
				"MuseoSansRounded-500.otf");
		toolbarTitleTextView = toolbar.findViewById(R.id.toolbar_title);
		toolbarTitleTextView.setTypeface(titleFont);

		navigationView = findViewById(R.id.navigation);
		if (navigationView != null) {
		    navigationView.setOnNavigationItemSelectedListener(this);

            navigationView.enableShiftingMode(false);
            navigationView.setTextVisibility(false);
		}

		tabLayout = findViewById(R.id.tabLayout);
	}

	public void setToolbarShowsTitle(boolean visible) {
		toolbarTitleTextView.setVisibility(visible? View.VISIBLE : View.GONE);
	}

	public void setToolbarTitle(String title) {
		toolbarTitleTextView.setText(title);
	}

	public void setToolbarVisible(boolean visible) {
		if (getSupportActionBar() == null) return;

		if (visible) {
			getSupportActionBar().show();
		} else {
			getSupportActionBar().hide();
		}
	}

	@Override
	public boolean onSupportNavigateUp() {
		onBackPressed();
		return true;
	}

	public void setShowNavigationBackButton(boolean show) {
		getSupportActionBar().setDisplayHomeAsUpEnabled(show);
		getSupportActionBar().setDisplayShowHomeEnabled(show);
	}

	private void setNavigationEnabled(boolean enabled) {
        navigationView.setVisibility(enabled? View.VISIBLE : View.GONE);
	}

	public TabLayout getTabLayout() {
		return tabLayout;
	}

	private void showElection() {
	    runOnUiThread(() -> {
            ElectionMasterFragment electionFragment = new ElectionMasterFragment();
            electionFragment.setTabLayout(tabLayout);

            switchContent(electionFragment);

            @IdRes int navigationId = R.id.navigation_election;
            currentNavigationId = navigationId;
            navigationView.setSelectedItemId(navigationId);
        });
    }

    public void showFriends() {
	    runOnUiThread(() -> {
            FindAddContactFragment friendsFragment = new FindAddContactFragment();
            friendsFragment.setTabLayout(tabLayout);

            mContent = friendsFragment;

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, friendsFragment)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .addToBackStack(null)
                    .commit();

            @IdRes int navigationId = R.id.navigation_profile;
            currentNavigationId = navigationId;
            navigationView.setSelectedItemId(navigationId);
        });
    }

	public void showLogin() {
		LoginFragment loginFragment = new LoginFragment();
        loginFragment.setTabLayout(tabLayout);
        switchContent(loginFragment);

        setNavigationEnabled(false);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		return false;
	}

	@Override
	public boolean onNavigationItemSelected(@NonNull MenuItem item) {

		if (item.isCheckable()) item.setChecked(true);

		final int newNavigationId = item.getItemId();

		if (currentNavigationId != newNavigationId) {

			switch(newNavigationId) {
                case R.id.navigation_election:
                    showElection();
                    break;

				case R.id.navigation_feed:
					FeedFragment feedFragment = new FeedFragment();
					feedFragment.setTabLayout(tabLayout);

					switchContent(feedFragment);
					break;



				case R.id.navigation_poll:
					PollFragment pollFragment = new PollFragment();
					pollFragment.setTabLayout(tabLayout);

					switchContent(pollFragment);
					break;

				case R.id.navigation_vote:
					final VoteViewPagerFragment voteFragment = new VoteViewPagerFragment();
					voteFragment.setTabLayout(tabLayout);

					switchContent(voteFragment);
					break;

				case R.id.navigation_profile:
					ProfileViewPagerFragment profileFragment = new ProfileViewPagerFragment();
					profileFragment.setTabLayout(tabLayout);

					switchContent(profileFragment);
					break;
			}

			currentNavigationId = newNavigationId;
		}

		return true;
	}
	
	public Isegoria getIsegoriaApplication() {
		return application;
	}

	// When the Sign Up button in the launch screen is tapped
	public void onSignUpClicked() {
		setShowNavigationBackButton(true);

		SignUpFragment signUpFragment = new SignUpFragment();
		signUpFragment.setListener(this);

		switchContent(signUpFragment, false);
	}

	private void setViewEnabled(@IdRes int viewId, boolean enabled) {
		View view = findViewById(viewId);
		if (view != null) view.setEnabled(enabled);
	}

	public void onLoginSuccess(User loggedInUser) {
        hideDialog();

        setNavigationEnabled(true);
        setToolbarVisible(true);

        if (!handleAppShortcutIntent()) {
            FeedFragment feedFragment = new FeedFragment();
            feedFragment.setTabLayout(tabLayout);

            navigationView.setSelectedItemId(R.id.navigation_feed);
        }

        if (!loggedInUser.hasPersonality) {
            startActivity(new Intent(this, PersonalityQuestionsActivity.class));
        }
    }
	
	public void hideDialog() {
		if (dialog != null) dialog.dismiss();
	}
	
	public void onLoginFailure() {
	    runOnUiThread(() -> {
            setViewEnabled(R.id.login_email, true);
            setViewEnabled(R.id.login_password, true);
            setViewEnabled(R.id.login_button, true);
            setViewEnabled(R.id.login_signup_button, true);

            hideDialog();

            if (mContent.getClass() != LoginFragment.class) {
                //Tried to login user based on stored email/password, but they since deleted account
                //or the login otherwise failed. Not currently showing the Login fragment so we need to.
                switchContent(new LoginFragment());
            }

            Snackbar.make(findViewById(R.id.coordinator_layout), getString(R.string.user_login_error_message), Snackbar.LENGTH_LONG)
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
        switchContent(consentAgreementFragment);
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
		
		switchContent(new LoginFragment());
	}

	public void switchContent(TitledFragment fragment) {
		switchContent(fragment, true);
	}

	private void switchContent(TitledFragment fragment, boolean popBackStack) {
        WeakReference<MainActivity> wrSelf = new WeakReference<>(this);

	    runOnUiThread(() -> {
	        if (wrSelf.get() == null || wrSelf.get().isFinishing()) return;

            mContent = fragment;

            if (popBackStack) {
                getSupportFragmentManager().popBackStackImmediate();
            }

            FragmentTransaction transaction = getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, (Fragment)fragment, fragment.getClass().toString());

            if (!popBackStack) {
                transaction.addToBackStack(null).commitAllowingStateLoss();

                getSupportFragmentManager().executePendingTransactions();

                postFragmentCommit(fragment.getTitle(this));

            } else {
                transaction.runOnCommit(() -> postFragmentCommit(fragment.getTitle(this))).commitAllowingStateLoss();
            }
        });
	}

	private void postFragmentCommit(String fragmentTitle) {
        if (!TextUtils.isEmpty(fragmentTitle)) setToolbarTitle(fragmentTitle);
    }
}