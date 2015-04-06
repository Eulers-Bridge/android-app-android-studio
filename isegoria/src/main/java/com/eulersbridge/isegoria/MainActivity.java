package com.eulersbridge.isegoria;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.MenuItem;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity {
	private SherlockFragment mContent;
	private Isegoria application;
	public ProgressDialog dialog;
	
	private String firstName;
	private String lastName; 
	private String email;
	private String password; 
	private String confirmPassword; 
	private String country;
	private String institution;
	private String yearOfBirth;
	private String gender;
	
	public MainActivity(){
		super(R.string.app_name);	
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		application = (Isegoria) getApplicationContext();
		application.setMainActivity(this);
		
		if (savedInstanceState != null)
			mContent = (SherlockFragment) getSupportFragmentManager().getFragment(savedInstanceState, "mContent");
		if (mContent == null)
			mContent = new MainView();
		
		setContentView(R.layout.content_frame);
		getSupportFragmentManager()
		.beginTransaction()
		.replace(R.id.content_frame, mContent).commit();
		
		setBehindContentView(R.layout.menu_frame);
		getSupportFragmentManager()
		.beginTransaction()
		.replace(R.id.menu_frame, new SlidingMenuItems()).commit();
		
		getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
		//setSlidingActionBarEnabled(true);
		getSlidingMenu().setSlidingEnabled(false);
		
		switchContent(new LoginScreenFragment());
        //switchContent(new PersonalityQuestionsFragment());
	}

    public boolean dispatchTouchEvent(MotionEvent ev) {
        FragmentManager fm = getSupportFragmentManager();
        List<Fragment> fragments = fm.getFragments();
        Fragment lastFragment = fragments.get(1);

        if(lastFragment instanceof PollFragment) {
            PollFragment pollFragment = (PollFragment) lastFragment;
            pollFragment.collapseBar(ev);
        }

        return super.dispatchTouchEvent(ev);
    }
	
	public Isegoria getIsegoriaApplication() {
		return application;
	}
	
	public void signupClicked(View view) {
		switchContent(new UserSignupFragment());
	}
	
	public void login(View v) {
		TextView userNameField = (TextView) findViewById(R.id.username);
		TextView passwordField = (TextView) findViewById(R.id.password);
		
		application.setUsername(userNameField.getText().toString());
		application.setPassword(passwordField.getText().toString());
		
		application.login();
		
		dialog = ProgressDialog.show(this, "", "Loading. Please wait...", true);
	}
	
	public void hideDialog() {
		dialog.dismiss();
	}
	
	public void showLoginFailed() {
		AlertDialog alertDialog = new AlertDialog.Builder(application.getMainActivity()).create();
		alertDialog.setTitle("Isegoria");
		alertDialog.setMessage("Login Failed");
		alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				      
				}
			});
		alertDialog.show();
	}
	
	public void showSignupSucceded() {
		AlertDialog alertDialog = new AlertDialog.Builder(application.getMainActivity()).create();
		alertDialog.setTitle("Isegoria");
		alertDialog.setMessage("Signup Succeeded");
		alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				      
				}
			});
		alertDialog.show();
	}
	
	public void showSignupFailed() {
		AlertDialog alertDialog = new AlertDialog.Builder(application.getMainActivity()).create();
		alertDialog.setTitle("Isegoria");
		alertDialog.setMessage("Signup Failed");
		alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				      
				}
			});
		alertDialog.show();
	}
	
	public void userSignupNext(View view) {
		TextView firstNameField = (TextView) findViewById(R.id.firstName);
		TextView lastNameField = (TextView) findViewById(R.id.lastName);
		TextView universityEmailField = (TextView) findViewById(R.id.universityEmail);
		TextView newPasswordField = (TextView) findViewById(R.id.newPassword);
		TextView confirmNewPasswordField = (TextView) findViewById(R.id.confirmNewPassword);
		Spinner countryField = (Spinner) findViewById(R.id.country);
		Spinner institutionField = (Spinner) findViewById(R.id.institution);
		Spinner yearOfBirthField = (Spinner) findViewById(R.id.yearOfBirth);
		Spinner genderField = (Spinner) findViewById(R.id.gender);
		
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
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		try {
			switch(item.getItemId()){
				case android.R.id.home:
					if(application.isLoggedIn()) {
						toggle();
					}
					return true;
			}
			
			switchContent(new UserSettingsFragment());
			
			return true;
		} catch(Exception e) {
			
		}
		
		return false;
	}
	
	public void switchContent(SherlockFragment fragment) {
			mContent = fragment;
			getSupportFragmentManager().popBackStack();
			getSupportFragmentManager()
				.beginTransaction()
				.replace(R.id.content_frame, fragment)
				.commit();
			getSlidingMenu().showContent();
	}
}