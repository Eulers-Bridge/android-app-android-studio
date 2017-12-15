package com.eulersbridge.isegoria.auth;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.eulersbridge.isegoria.Isegoria;
import com.eulersbridge.isegoria.common.TitledFragment;
import com.eulersbridge.isegoria.models.SignUpUser;
import com.eulersbridge.isegoria.network.GeneralInfoResponse;
import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.models.Country;
import com.eulersbridge.isegoria.models.Institution;
import com.eulersbridge.isegoria.network.SimpleCallback;
import com.eulersbridge.isegoria.common.Utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import retrofit2.Response;

public class SignUpFragment extends Fragment implements OnItemSelectedListener, TitledFragment {

    private SignUpListener listener;

    private TextView givenNameField;
    private TextView familyNameField;
    private TextView emailField;
    private TextView newPasswordField;
    private TextView confirmNewPasswordField;

    private Spinner countrySpinner;
    private Spinner institutionSpinner;
    private Spinner yearOfBirthSpinner;
    private Spinner genderSpinner;

	private List<Country> countries;
	private ArrayAdapter<String> countryAdapter;
	private ArrayAdapter<String> institutionAdapter;

	public interface SignUpListener {
	    void onSignUpNextClick(SignUpUser user);
    }

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.user_sign_up_fragment, container, false);

        countries = new ArrayList<>();

        rootView.findViewById(R.id.sign_up_next_button).setOnClickListener(view -> continueSignUp());

        AppCompatActivity activity = (AppCompatActivity)getActivity();

        givenNameField = rootView.findViewById(R.id.sign_up_given_name);
        familyNameField = rootView.findViewById(R.id.sign_up_family_name);
        emailField = rootView.findViewById(R.id.sign_up_email);
        newPasswordField = rootView.findViewById(R.id.sign_up_new_password);
        confirmNewPasswordField = rootView.findViewById(R.id.sign_up_confirm_new_password);

        countrySpinner = rootView.findViewById(R.id.sign_up_country);
        countrySpinner.setOnItemSelectedListener(this);
        countryAdapter = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_item);
        countryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        countrySpinner.setAdapter(countryAdapter);

        institutionSpinner = rootView.findViewById(R.id.sign_up_institution);
        institutionAdapter = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_item);
        institutionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        institutionSpinner.setAdapter(institutionAdapter);
        
        genderSpinner = rootView.findViewById(R.id.sign_up_gender);
		ArrayAdapter<String> spinnerGenderArrayAdapter = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_item, new String[]{ "Male", "Female" });
        spinnerGenderArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genderSpinner.setAdapter(spinnerGenderArrayAdapter);
        
        yearOfBirthSpinner = rootView.findViewById(R.id.sign_up_birth_year);
		ArrayAdapter<String> spinnerYearOfBirthArrayAdapter = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_item);

		int year = Calendar.getInstance().get(Calendar.YEAR);

        // Allow users to be in the age range 12 to 100
        for (int i = (year - 100); i <= (year - 12); i++) {
        	spinnerYearOfBirthArrayAdapter.add(String.valueOf(i));

            if (i == 1990) yearOfBirthSpinner.setSelection(i);
        }
        spinnerYearOfBirthArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        yearOfBirthSpinner.setAdapter(spinnerYearOfBirthArrayAdapter);

        countryAdapter.add(getString(R.string.user_sign_up_choose_country_hint));
        institutionAdapter.add(getString(R.string.user_sign_up_choose_institution_hint));

        Isegoria isegoria = (Isegoria)activity.getApplication();
        isegoria.getAPI().getGeneralInfo().enqueue(new SimpleCallback<GeneralInfoResponse>() {
            @Override
            protected void handleResponse(Response<GeneralInfoResponse> response) {
                GeneralInfoResponse body = response.body();
                if (body != null && body.countries.size() > 0) {
                    setCountries(body.countries);
                }
            }
        });

        Utils.showKeyboard(activity.getWindow());
		
		return rootView;
	}

	public void setListener(SignUpListener listener) {
	    this.listener = listener;
    }

	private void continueSignUp() {

	    String givenName = givenNameField.getText().toString();
	    boolean givenNameValid = !TextUtils.isEmpty(givenName);

	    String familyName = familyNameField.getText().toString();
        boolean familyNameValid = !TextUtils.isEmpty(familyName);

        String email = emailField.getText().toString();
        boolean emailValid = Utils.validEmail(email);

        String password = newPasswordField.getText().toString();
        // Make sure passwords are not empty, and are at least 8 characters long
        boolean passwordValid = !TextUtils.isEmpty(password) && password.length() >= 8;

        String confirmPassword = newPasswordField.getText().toString();
        boolean passwordsMatch = password.equals(confirmPassword);

        Object selectedCountryItem = countrySpinner.getSelectedItem();
        String country = selectedCountryItem == null? null : selectedCountryItem.toString();
        boolean countryValid = !TextUtils.isEmpty(country);

        Object selectedInstitutionItem = institutionSpinner.getSelectedItem();
        String institutionName = selectedInstitutionItem == null? null : selectedInstitutionItem.toString();
        boolean institutionValid = !TextUtils.isEmpty(institutionName);

        Object selectedYearOfBirthItem = yearOfBirthSpinner.getSelectedItem();
        String yearOfBirth = selectedYearOfBirthItem == null? null : selectedYearOfBirthItem.toString();
        boolean yearOfBirthValid = !TextUtils.isEmpty(yearOfBirth);

        String gender = genderSpinner.getSelectedItem().toString();
        boolean genderValid = !TextUtils.isEmpty(gender);

        boolean allFieldsValid = givenNameValid && familyNameValid && emailValid
                                    && passwordValid && passwordsMatch && countryValid
                                    && institutionValid && yearOfBirthValid && genderValid;

        if (allFieldsValid) {
            SignUpUser user = new SignUpUser(givenName, familyName, gender, country, yearOfBirth,
                    email, password, institutionName);
            if (listener != null) listener.onSignUpNextClick(user);
        }
    }

    @Override
    public String getTitle(Context context) {
        return null;
    }

    private void setCountries(List<Country> newCountries) {
		Activity activity = getActivity();
		if (activity != null) {
			activity.runOnUiThread(() -> {

                countrySpinner.setEnabled(false);

			    for (Country country : newCountries) {
			        countryAdapter.add(country.name);
                }

                countries = newCountries;

                countrySpinner.setEnabled(true);
            });
		}
	}
	
    public void onItemSelected(AdapterView<?> parent, View view,  int pos, long id) {
    	String selectedCountry = (String) parent.getSelectedItem();
    	institutionAdapter.clear();
        institutionSpinner.setEnabled(false);

    	for (Country country : countries) {
            if (selectedCountry.equals(country.name) && country.institutions != null) {
                for (Institution institution : country.institutions) {
                    institutionAdapter.add(institution.getName());
                }
            }
        }

        institutionSpinner.setEnabled(true);
    }

    public void onNothingSelected(AdapterView<?> parent) { }
}
