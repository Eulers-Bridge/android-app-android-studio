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
import android.support.annotation.UiThread;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;

import com.eulersbridge.isegoria.election.ElectionMasterFragment;
import com.eulersbridge.isegoria.feed.FeedFragment;
import com.eulersbridge.isegoria.login.LoginScreenFragment;
import com.eulersbridge.isegoria.login.PersonalityQuestionsActivity;
import com.eulersbridge.isegoria.login.UserConsentAgreementFragment;
import com.eulersbridge.isegoria.login.UserSignupFragment;
import com.eulersbridge.isegoria.models.Country;
import com.eulersbridge.isegoria.models.Institution;
import com.eulersbridge.isegoria.models.User;
import com.eulersbridge.isegoria.network.GeneralInfoResponse;
import com.eulersbridge.isegoria.network.SimpleCallback;
import com.eulersbridge.isegoria.poll.PollFragment;
import com.eulersbridge.isegoria.profile.ProfileViewPagerFragment;
import com.eulersbridge.isegoria.utilities.TitledFragment;
import com.eulersbridge.isegoria.utilities.Utils;
import com.eulersbridge.isegoria.vote.VoteFragmentDone;
import com.eulersbridge.isegoria.vote.VoteFragmentPledge;
import com.eulersbridge.isegoria.vote.VoteViewPagerFragment;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.securepreferences.SecurePreferences;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

	private TitledFragment mContent;
	private Isegoria application;
	public ProgressDialog dialog;

    private BottomNavigationViewEx navigationView;

	private TextView toolbarTitleTextView;
	private TabLayout tabLayout;
	private @IdRes int currentNavigationId;
	
	private String firstName;
	private String lastName; 
	private String email;
	private String password; 
	private String confirmPassword; 
	private String country;
	private String institution;
	private String yearOfBirth;
	private String gender;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main_activity);

		application = (Isegoria) getApplicationContext();
		application.setMainActivity(this);

		setupNotificationChannels();

		setupToolbarAndNavigation();

		setNavigationEnabled(false);

		String userEmail = new SecurePreferences(this).getString("userEmail", null);
		String userPassword = new SecurePreferences(this).getString("userPassword", null);

		if (userEmail != null && userPassword != null) {
			login(userEmail, userPassword);

		} else {
			showLogin();
		}
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
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
			HashMap<CharSequence, Integer> channels = new HashMap<>();
			channels.put("Friends", NotificationManager.IMPORTANCE_DEFAULT);
			channels.put("Vote Reminder", NotificationManager.IMPORTANCE_DEFAULT);

			NotificationManager notificationManager =
					(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

			if (notificationManager != null) {
				for (Map.Entry<CharSequence, Integer> entry : channels.entrySet()) {
					CharSequence channelName = entry.getKey();
					String channelId = channelName.toString().toLowerCase().replace(" ","_");
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

	void setToolbarShowsTitle(boolean visible) {
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
		LoginScreenFragment loginScreenFragment = new LoginScreenFragment();
        loginScreenFragment.setTabLayout(tabLayout);
        switchContent(loginScreenFragment);

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
				case R.id.navigation_feed:
					FeedFragment feedFragment = new FeedFragment();
					feedFragment.setTabLayout(tabLayout);

					switchContent(feedFragment);
					break;

				case R.id.navigation_election:
					showElection();
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
	
	public void signUpClicked() {
		setShowNavigationBackButton(true);

		switchContent(new UserSignupFragment(), false);
	}

	private void setViewEnabled(@IdRes int viewId, boolean enabled) {
		View view = findViewById(viewId);
		if (view != null) view.setEnabled(enabled);
	}
	
	public void login(String email, String password) {
	    if (Utils.isNetworkAvailable(this)) {
            application.login(email, password);

            setViewEnabled(R.id.login_email, false);
            setViewEnabled(R.id.login_password, false);
            setViewEnabled(R.id.login_button, false);
            setViewEnabled(R.id.login_signup_button, false);

        } else {
            new AlertDialog.Builder(this)
                    .setTitle("Failed to login")
                    .setMessage("Check you have an active internet connection and try again soon.")
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
        }
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

		setViewEnabled(R.id.login_email, true);
		setViewEnabled(R.id.login_password, true);
		setViewEnabled(R.id.login_button, true);
		setViewEnabled(R.id.login_signup_button, true);

	    hideDialog();

		if (mContent.getClass() != LoginScreenFragment.class) {
			//Tried to login user based on stored email/password, but they since deleted account
			//or the login otherwise failed. Not currently showing the Login fragment so we need to.
			switchContent(new LoginScreenFragment());
		}

        new AlertDialog.Builder(this)
                .setTitle("Login failed")
                .setMessage("Check your email and password are correct, and you have an active internet connection.")
                .setPositiveButton(android.R.string.ok, null)
                .show();
	}
	
	public void onSignUpSuccess() {
		setShowNavigationBackButton(false);
		setToolbarVisible(true);

        new AlertDialog.Builder(this)
                .setTitle("Welcome to Isegoria!")
                .setPositiveButton(android.R.string.ok, null)
                .show();
	}
	
	public void onSignUpFailure() {
        new AlertDialog.Builder(this)
                .setTitle("Signup failed")
                .setMessage("Make sure you've entered your details correctly, and you have an active internet connection.")
                .setPositiveButton(android.R.string.ok, null)
                .show();
	}
	
	public void userSignUpNext() {
		TextView firstNameField = findViewById(R.id.signup_first_name);
		TextView lastNameField = findViewById(R.id.signup_last_name);
		TextView universityEmailField = findViewById(R.id.signup_email);
		TextView newPasswordField = findViewById(R.id.signup_new_password);
		TextView confirmNewPasswordField = findViewById(R.id.signup_confirm_new_password);
		Spinner countryField = findViewById(R.id.signup_country);
		Spinner institutionField = findViewById(R.id.signup_institution);
		Spinner yearOfBirthField = findViewById(R.id.signup_birth_year);
		Spinner genderField = findViewById(R.id.signup_gender);
		
		firstName = firstNameField.getText().toString();
		lastName = lastNameField.getText().toString(); 
		email = universityEmailField.getText().toString();
		password = newPasswordField.getText().toString(); 
		confirmPassword = confirmNewPasswordField.getText().toString(); 
		country = countryField.getSelectedItem().toString();

		if (institutionField.getSelectedItem() != null) {
            institution = institutionField.getSelectedItem().toString();
        } else {
            institution = null;
        }

        if (yearOfBirthField.getSelectedItem() != null) {
            yearOfBirth = yearOfBirthField.getSelectedItem().toString();
        } else {
            yearOfBirth = null;
        }

		gender = genderField.getSelectedItem().toString();
		
		if (TextUtils.isEmpty(firstName) || TextUtils.isEmpty(lastName) || TextUtils.isEmpty(email)
                || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword) || confirmPassword.equals("")
                || TextUtils.isEmpty(country) || TextUtils.isEmpty(institution)
                || TextUtils.isEmpty(yearOfBirth) || TextUtils.isEmpty(gender)) {
			
		} else {
			UserConsentAgreementFragment userConsentAgreementFragment = new UserConsentAgreementFragment();
			switchContent(userConsentAgreementFragment);
		}
	}
	
	public void userConsentNext() {
		if(firstName.equals("") || lastName.equals("") || email.equals("") || password.equals("") || password.equals("") || confirmPassword.equals("")
				|| country.equals("") || institution.equals("") || yearOfBirth.equals("") || gender.equals("")) {
			
		} else {
			application.getAPI().getGeneralInfo().enqueue(new SimpleCallback<GeneralInfoResponse>() {
				@Override
				protected void handleResponse(Response<GeneralInfoResponse> response) {
					GeneralInfoResponse body = response.body();
					if (body != null && body.countries.size() > 0) {

						long institutionId = -1;

						for (Country country : body.countries) {
							for (Institution institution : country.institutions) {
								if (institution.getName().equals(institution)) {
									institutionId = institution.id;
								}
							}
						}

						application.getNetworkService().signUp(firstName, lastName, gender, country, yearOfBirth, email, password, institutionId);
					}
				}
			});
		}
		
		switchContent(new LoginScreenFragment());
	}

    public void voteNext() {
        switchContent(new VoteFragmentPledge());
    }

    public void voteDone() {
        switchContent(new VoteFragmentDone());
    }

	@UiThread
	public void switchContent(TitledFragment fragment) {
		switchContent(fragment, true);
	}

	@UiThread
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

                postFragmentCommit(fragment.getTitle());

            } else {
                transaction.runOnCommit(() -> postFragmentCommit(fragment.getTitle())).commitAllowingStateLoss();
            }
        });
	}

	private void postFragmentCommit(String fragmentTitle) {
        if (!TextUtils.isEmpty(fragmentTitle)) setToolbarTitle(fragmentTitle);
    }
}