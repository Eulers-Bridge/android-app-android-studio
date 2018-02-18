package com.eulersbridge.isegoria.auth.signup;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Spinner;

import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.auth.AuthViewModel;
import com.eulersbridge.isegoria.network.api.models.Country;
import com.eulersbridge.isegoria.network.api.models.Institution;
import com.eulersbridge.isegoria.util.Utils;
import com.eulersbridge.isegoria.util.data.SimpleTextWatcher;

import java.util.Calendar;
import java.util.List;

public class SignUpFragment extends Fragment {

    private Button signUpButton;
    private ImageView backButton;

    private EditText givenNameField;
    private EditText familyNameField;
    private EditText emailField;
    private EditText newPasswordField;
    private EditText confirmNewPasswordField;
    private EditText genderField;

    private Spinner countrySpinner;
    private Spinner institutionSpinner;
    private Spinner yearOfBirthSpinner;

	private ArrayAdapter<Country> countryAdapter;
	private ArrayAdapter<Institution> institutionAdapter;

	private SignUpViewModel viewModel;
	private AuthViewModel authViewModel;

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.sign_up_fragment, container, false);

		if (getActivity() != null)
		    authViewModel = ViewModelProviders.of(getActivity()).get(AuthViewModel.class);

		viewModel = ViewModelProviders.of(this).get(SignUpViewModel.class);

        final ScrollView scrollContainer = rootView.findViewById(R.id.sign_up_container);

        boolean hasTranslucentStatusBar = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
        if (hasTranslucentStatusBar) {
            int additionalTopPadding = Math.round(22 * getResources().getDisplayMetrics().density);

            scrollContainer.setPadding(
                    scrollContainer.getPaddingLeft(),
                    scrollContainer.getPaddingTop() + additionalTopPadding,
                    scrollContainer.getPaddingRight(),
                    scrollContainer.getPaddingBottom());
        }

        backButton = rootView.findViewById(R.id.sign_up_back_button);

        signUpButton = rootView.findViewById(R.id.sign_up_next_button);

        givenNameField = rootView.findViewById(R.id.sign_up_given_name);
        familyNameField = rootView.findViewById(R.id.sign_up_family_name);
        emailField = rootView.findViewById(R.id.sign_up_email);
        newPasswordField = rootView.findViewById(R.id.sign_up_new_password);
        confirmNewPasswordField = rootView.findViewById(R.id.sign_up_confirm_new_password);
        genderField = rootView.findViewById(R.id.sign_up_gender);

        AppCompatActivity activity = (AppCompatActivity)getActivity();

        countrySpinner = rootView.findViewById(R.id.sign_up_country);

        countryAdapter = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_item);
        countryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        countrySpinner.setAdapter(countryAdapter);

        institutionSpinner = rootView.findViewById(R.id.sign_up_institution);
        institutionAdapter = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_item);
        institutionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        institutionSpinner.setAdapter(institutionAdapter);
        
        yearOfBirthSpinner = rootView.findViewById(R.id.sign_up_birth_year);
		ArrayAdapter<String> spinnerYearOfBirthArrayAdapter = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_item);

		final int year = Calendar.getInstance().get(Calendar.YEAR);

        // Allow users to be in the age range 12 to 100
        for (int i = (year - 100); i <= (year - 12); i++) {
        	spinnerYearOfBirthArrayAdapter.add(String.valueOf(i));

            if (i == 1990) yearOfBirthSpinner.setSelection(i);
        }
        spinnerYearOfBirthArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        yearOfBirthSpinner.setAdapter(spinnerYearOfBirthArrayAdapter);

        setupViewListeners();

        viewModel.getCountries().observe(this, countries -> {
            if (countries != null)
                setCountries(countries);
        });

        Utils.showKeyboard(activity.getWindow());
		
		return rootView;
	}

    private void setupViewListeners() {
        backButton.setOnClickListener(view -> {
            if (getActivity() != null) {
                authViewModel.onSignUpBackPressed();
                getActivity().onBackPressed();
            }
        });

        signUpButton.setOnClickListener(view -> {
            backButton.setEnabled(false);
            signUpButton.setEnabled(false);

            SignUpUser newUser = viewModel.getSignUpUser();
            if (newUser == null) {
                backButton.setEnabled(true);
                signUpButton.setEnabled(true);

            } else {
                authViewModel.signUpUser.setValue(newUser);
            }
        });

        givenNameField.addTextChangedListener(new SimpleTextWatcher(value ->
            viewModel.setGivenName(value.toString())
        ));
        familyNameField.addTextChangedListener(new SimpleTextWatcher(value ->
            viewModel.setFamilyName(value.toString())
        ));
        emailField.addTextChangedListener(new SimpleTextWatcher(value ->
            viewModel.setEmail(value.toString())
        ));
        newPasswordField.addTextChangedListener(new SimpleTextWatcher(value ->
            viewModel.setPassword(value.toString())
        ));
        confirmNewPasswordField.addTextChangedListener(new SimpleTextWatcher(value ->
            viewModel.setConfirmPassword(value.toString())
        ));
        genderField.addTextChangedListener(new SimpleTextWatcher(value ->
            viewModel.setGender(value.toString())
        ));

        countrySpinner.setOnItemSelectedListener(new SimpleOnItemSelectedListener() {
            @Override
            void onItemSelected(int position) {
                institutionSpinner.setEnabled(false);

                List<Institution> institutions = viewModel.onCountrySelected(position);
                if (institutions != null) {
                    institutionAdapter.clear();
                    institutionAdapter.addAll(institutions);
                }

                institutionSpinner.setEnabled(true);
            }
        });

        institutionSpinner.setOnItemSelectedListener(new SimpleOnItemSelectedListener() {
            @Override
            void onItemSelected(int position) {
                viewModel.onInstitutionSelected(position);
            }
        });

        yearOfBirthSpinner.setOnItemSelectedListener(new SimpleOnItemSelectedListener() {
            @Override
            void onItemSelected(int position) {
                String birthYear = (String) yearOfBirthSpinner.getSelectedItem();
                viewModel.onBirthYearSelected(birthYear);
            }
        });
    }

    private void setCountries(List<Country> newCountries) {
        countrySpinner.setEnabled(false);

        if (newCountries != null) {
            countryAdapter.clear();
            countryAdapter.addAll(newCountries);
        }

        countrySpinner.setEnabled(true);
	}
}
