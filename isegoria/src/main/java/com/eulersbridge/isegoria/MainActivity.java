package com.eulersbridge.isegoria;


import android.app.AlertDialog;
import android.app.Dialog;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;

import com.eulersbridge.isegoria.election.ElectionMasterFragment;
import com.eulersbridge.isegoria.feed.FeedFragment;
import com.eulersbridge.isegoria.login.LoginScreenFragment;
import com.eulersbridge.isegoria.login.PersonalityQuestionsFragment;
import com.eulersbridge.isegoria.login.UserConsentAgreementFragment;
import com.eulersbridge.isegoria.login.UserSignupFragment;
import com.eulersbridge.isegoria.models.Country;
import com.eulersbridge.isegoria.models.Institution;
import com.eulersbridge.isegoria.models.User;
import com.eulersbridge.isegoria.network.GeneralInfoResponse;
import com.eulersbridge.isegoria.network.SimpleCallback;
import com.eulersbridge.isegoria.poll.PollFragment;
import com.eulersbridge.isegoria.profile.ProfileViewPagerFragment;
import com.eulersbridge.isegoria.utilities.BottomNavigationViewHelper;
import com.eulersbridge.isegoria.vote.VoteFragmentDone;
import com.eulersbridge.isegoria.vote.VoteFragmentPledge;
import com.securepreferences.SecurePreferences;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

	private Fragment mContent;
	private Isegoria application;
	public ProgressDialog dialog;

    private BottomNavigationView navigationView;

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
        //switchContent(new PersonalityQuestionsFragment());
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

            BottomNavigationViewHelper.disableShiftMode(navigationView);
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
		if (getSupportActionBar() != null) {
			if (visible) {
				getSupportActionBar().show();
			} else {
				getSupportActionBar().hide();
			}
		}
	}

	private void setShowNavigationBackButton(boolean show) {
		getSupportActionBar().setDisplayHomeAsUpEnabled(show);
		getSupportActionBar().setDisplayShowHomeEnabled(show);
	}

	private void setNavigationEnabled(boolean enabled) {
        navigationView.setEnabled(enabled);
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
					final FeedFragment feedFragment = new FeedFragment();
					feedFragment.setTabLayout(tabLayout);

					switchContent(feedFragment);
					break;

				case R.id.navigation_election:
					showElection();
					break;

				case R.id.navigation_poll:
					final PollFragment pollFragment = new PollFragment();
					pollFragment.setTabLayout(tabLayout);

					switchContent(pollFragment);
					break;

				/*case R.id.navigation_vote:
					VoteViewPagerFragment voteFragment = new VoteViewPagerFragment();
					voteFragment.setTabLayout(tabLayout);

					switchContent(voteFragment);
					break;*/

				case R.id.navigation_profile:

					final ProfileViewPagerFragment profileFragment = new ProfileViewPagerFragment();
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
	
	public void signupClicked(View view) {
		setShowNavigationBackButton(true);

		switchContent(new UserSignupFragment(), false);
	}
	
	public void login(String email, String password) {
		application.login(email, password);
		
		//dialog = ProgressDialog.show(this, "", "Loading. Please wait...", true);
	}

	public void onLoginSuccess(User loggedInUser) {
        hideDialog();

        if (loggedInUser.hasPersonality) {

            setNavigationEnabled(true);
            setToolbarVisible(true);

            if (!handleAppShortcutIntent()) {
                FeedFragment feedFragment = new FeedFragment();
                feedFragment.setTabLayout(tabLayout);

                switchContent(feedFragment);
            }

        } else {
            PersonalityQuestionsFragment personalityQuestionsFragment = new PersonalityQuestionsFragment();
            personalityQuestionsFragment.setTabLayout(tabLayout);
            switchContent(personalityQuestionsFragment);
        }
    }
	
	public void hideDialog() {
		if (dialog != null) dialog.dismiss();
	}
	
	public void onLoginFailure() {

	    hideDialog();

		if (mContent.getClass() != LoginScreenFragment.class) {
			//Tried to login user based on stored email/password, but they since deleted account
			//or the login otherwise failed. Not currently showing the Login fragment so we need to.
			switchContent(new LoginScreenFragment());
		}

		AlertDialog alertDialog = new AlertDialog.Builder(application.getMainActivity()).create();
		alertDialog.setTitle("Isegoria");
		alertDialog.setMessage("Login Failed");
		alertDialog.setButton(Dialog.BUTTON_POSITIVE, "OK", (dialog, which) -> {

            });
		alertDialog.show();
	}
	
	public void onSignUpSuccess() {
		setShowNavigationBackButton(false);
		setToolbarVisible(true);

		AlertDialog alertDialog = new AlertDialog.Builder(application.getMainActivity()).create();
		alertDialog.setTitle("Isegoria");
		alertDialog.setMessage("Signup Succeeded");
		alertDialog.setButton(Dialog.BUTTON_POSITIVE, "OK", (dialog, which) -> {

            });
		alertDialog.show();
	}
	
	public void onSignUpFailure() {
		AlertDialog alertDialog = new AlertDialog.Builder(application.getMainActivity()).create();
		alertDialog.setTitle("Isegoria");
		alertDialog.setMessage("Signup Failed");
		alertDialog.setButton(Dialog.BUTTON_POSITIVE, "OK", (dialog, which) -> {

            });
		alertDialog.show();
	}
	
	public void userSignupNext(View view) {
		TextView firstNameField = findViewById(R.id.firstName);
		TextView lastNameField = findViewById(R.id.lastName);
		TextView universityEmailField = findViewById(R.id.universityEmail);
		TextView newPasswordField = findViewById(R.id.newPassword);
		TextView confirmNewPasswordField = findViewById(R.id.confirmNewPassword);
		Spinner countryField = findViewById(R.id.country);
		Spinner institutionField = findViewById(R.id.institution);
		Spinner yearOfBirthField = findViewById(R.id.yearOfBirth);
		Spinner genderField = findViewById(R.id.gender);
		
		firstName = firstNameField.getText().toString();
		lastName = lastNameField.getText().toString(); 
		email = universityEmailField.getText().toString();
		password = newPasswordField.getText().toString(); 
		confirmPassword = confirmNewPasswordField.getText().toString(); 
		country = countryField.getSelectedItem().toString();
		institution = institutionField.getSelectedItem().toString();
		yearOfBirth = yearOfBirthField.getSelectedItem().toString();
		gender = genderField.getSelectedItem().toString();
		
		if(firstName.equals("") || lastName.equals("") || email.equals("") || password.equals("") || password.equals("") || confirmPassword.equals("")
				|| country.equals("") || institution.equals("") || yearOfBirth.equals("") || gender.equals("")) {
			
		}
		else {
			UserConsentAgreementFragment userConsentAgreementFragment = new UserConsentAgreementFragment();
			switchContent(userConsentAgreementFragment);
		}
	}
	
	public void userConsentNext(View view) {
		if(firstName.equals("") || lastName.equals("") || email.equals("") || password.equals("") || password.equals("") || confirmPassword.equals("")
				|| country.equals("") || institution.equals("") || yearOfBirth.equals("") || gender.equals("")) {
			
		}
		else {
			application.getAPI().getGeneralInfo().enqueue(new SimpleCallback<GeneralInfoResponse>() {
				@Override
				protected void handleResponse(Response<GeneralInfoResponse> response) {
					GeneralInfoResponse body = response.body();
					if (body != null && body.countries.size() > 0) {

						long institutionId = -1;

						for (Country country : body.countries) {
							for (Institution institution : country.institutions) {
								if (institution.name.equals(institution)) {
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

    public void voteNext(View v) {
        switchContent(new VoteFragmentPledge());
    }

    public void voteDone(View v) {
        switchContent(new VoteFragmentDone());
    }

	@Override
	public void onSaveInstanceState(Bundle outState){
		super.onSaveInstanceState(outState);

		if (mContent.isAdded()) {
			getSupportFragmentManager().putFragment(outState, "mContent", mContent);
		}
	}

	@UiThread
	public void switchContent(Fragment fragment) {
		switchContent(fragment, true);
	}

	@UiThread
	private void switchContent(Fragment fragment, boolean popBackStack) {
	    runOnUiThread(() -> {
            mContent = fragment;

            if (popBackStack) {
                getSupportFragmentManager().popBackStack();
            }

            FragmentTransaction transaction = getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, fragment, fragment.getClass().toString());

            if (!popBackStack) {
                transaction.addToBackStack(null);
            }

            transaction.commit();
        });
	}
}