package com.eulersbridge.isegoria;


import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
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
import com.eulersbridge.isegoria.login.UserConsentAgreementFragment;
import com.eulersbridge.isegoria.login.UserSignupFragment;
import com.eulersbridge.isegoria.models.CountryInfo;
import com.eulersbridge.isegoria.models.InstitutionInfo;
import com.eulersbridge.isegoria.poll.PollFragment;
import com.eulersbridge.isegoria.profile.ProfileViewPagerFragment;
import com.eulersbridge.isegoria.profile.UserSettingsFragment;
import com.eulersbridge.isegoria.vote.VoteFragmentDone;
import com.eulersbridge.isegoria.vote.VoteFragmentPledge;
import com.eulersbridge.isegoria.vote.VoteViewPagerFragment;
import com.securepreferences.SecurePreferences;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

	private Fragment mContent;
	private Isegoria application;
	public ProgressDialog dialog;

	private TextView toolbarTitleTextView;
	private DrawerLayout drawerLayout;
	private ActionBarDrawerToggle drawerToggle;
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
		
		if (savedInstanceState != null)
			mContent = getSupportFragmentManager().getFragment(savedInstanceState, "mContent");

		if (mContent == null) mContent = new FeedFragment();

		setupToolbarAndNavigation();

		setNavigationDrawerEnabled(false);

		String userEmail = new SecurePreferences(this).getString("userEmail", null);
		String userPassword = new SecurePreferences(this).getString("userPassword", null);

		if (userEmail != null && userPassword != null) {
			login(userEmail, userPassword);

		} else {
			switchContent(new LoginScreenFragment());
		}
        //switchContent(new PersonalityQuestionsFragment());
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

		drawerLayout = findViewById(R.id.drawer_layout);

		NavigationView navigationView = findViewById(R.id.navigation);
		if (navigationView != null) {
			navigationView.setNavigationItemSelectedListener(this);
		}

		//Set icon to open/close drawer and provide identifiers for open/close states (not important)
		drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
		drawerLayout.addDrawerListener(drawerToggle);
		drawerToggle.syncState();
		drawerToggle.setToolbarNavigationClickListener(view -> {
            onBackPressed();
            setShowNavigationBackButton(false);
        });

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

	void setNavigationDrawerEnabled(boolean enabled) {
		drawerLayout.setDrawerLockMode(enabled? DrawerLayout.LOCK_MODE_UNLOCKED : DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
		drawerToggle.setDrawerIndicatorEnabled(enabled);
	}

	public TabLayout getTabLayout() {
		return tabLayout;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		return false;
	}

	@Override
	public boolean onNavigationItemSelected(@NonNull MenuItem item) {
		if (drawerLayout.isDrawerOpen(GravityCompat.START))
			drawerLayout.closeDrawer(GravityCompat.START);

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
					final ElectionMasterFragment electionFragment = new ElectionMasterFragment();
					electionFragment.setTabLayout(tabLayout);

					switchContent(electionFragment);
					break;

				case R.id.navigation_poll:
					final PollFragment pollFragment = new PollFragment();
					pollFragment.setTabLayout(tabLayout);

					switchContent(pollFragment);
					break;

				case R.id.navigation_vote:
					VoteViewPagerFragment voteFragment = new VoteViewPagerFragment();
					voteFragment.setTabLayout(tabLayout);

					switchContent(voteFragment);
					break;

				case R.id.navigation_friends:
					FindAddContactFragment friendsFragment = new FindAddContactFragment();
					friendsFragment.setTabLayout(tabLayout);

					switchContent(friendsFragment);
					break;

				case R.id.navigation_profile:

					final ProfileViewPagerFragment profileFragment = new ProfileViewPagerFragment();
					profileFragment.setTabLayout(tabLayout);

					switchContent(profileFragment);
					break;

				case R.id.navigation_settings:
					final UserSettingsFragment userSettingsFragment = new UserSettingsFragment();
					userSettingsFragment.setTabLayout(tabLayout);

					switchContent(userSettingsFragment);
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
		application.setUsername(email);
		application.setPassword(password);
		
		application.login();
		
		dialog = ProgressDialog.show(this, "", "Loading. Please wait...", true);
	}
	
	public void hideDialog() {
		if (dialog != null) dialog.dismiss();
	}
	
	public void showLoginFailed() {

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
	
	public void showSignupSucceeded() {
		setShowNavigationBackButton(false);
		setToolbarVisible(true);

		AlertDialog alertDialog = new AlertDialog.Builder(application.getMainActivity()).create();
		alertDialog.setTitle("Isegoria");
		alertDialog.setMessage("Signup Succeeded");
		alertDialog.setButton(Dialog.BUTTON_POSITIVE, "OK", (dialog, which) -> {

            });
		alertDialog.show();
	}
	
	public void showSignupFailed() {
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
			String institutionID = "";
			ArrayList<CountryInfo> countryObjects = application.getCountryObjects();
			for(int i=0; i<countryObjects.size(); i++) {
				CountryInfo currentCountryInfo = countryObjects.get(i);
				
				for(int j=0; j<currentCountryInfo.getInstitutions().size(); j++) {
					InstitutionInfo currentInstituionInfo = currentCountryInfo.getInstitutions().get(j);
					
					if(currentInstituionInfo.getInstitution().equals(institution)) {
						institutionID = currentInstituionInfo.getId();
					}
				}
			}
			
			application.getNetwork().signup(firstName, lastName, gender, country, yearOfBirth, email, password, confirmPassword, institutionID);
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
		getSupportFragmentManager().putFragment(outState, "mContent", mContent);
	}
	
	public void switchContent(Fragment fragment) {
		switchContent(fragment, true);
	}

	private void switchContent(Fragment fragment, boolean popBackStack) {
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
	}
}