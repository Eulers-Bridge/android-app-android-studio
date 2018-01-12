package com.eulersbridge.isegoria.auth.signup;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.eulersbridge.isegoria.GlideApp;
import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.network.api.models.Country;
import com.eulersbridge.isegoria.network.api.models.Institution;
import com.eulersbridge.isegoria.util.Utils;
import com.eulersbridge.isegoria.util.transformation.BlurTransformation;
import com.eulersbridge.isegoria.util.ui.TitledFragment;

import java.util.Calendar;
import java.util.List;

public class SignUpFragment extends Fragment implements TitledFragment {

    private SignUpListener listener;

    private Button signUpButton;
    private ImageView backButton;

    private TextView givenNameField;
    private TextView familyNameField;
    private TextView emailField;
    private TextView newPasswordField;
    private TextView confirmNewPasswordField;

    private Spinner countrySpinner;
    private Spinner institutionSpinner;
    private Spinner yearOfBirthSpinner;
    private Spinner genderSpinner;

	private ArrayAdapter<Country> countryAdapter;
	private ArrayAdapter<Institution> institutionAdapter;

	private SignUpViewModel viewModel;

	public interface SignUpListener {
	    void onSignUpNextClick(SignUpUser user);
    }

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.user_sign_up_fragment, container, false);

		viewModel = ViewModelProviders.of(this).get(SignUpViewModel.class);

        final ScrollView scrollContainer = rootView.findViewById(R.id.sign_up_container);

        GlideApp.with(this)
                .load(R.drawable.tumblr_static_aphc)
                //DiskCacheStrategy.RESOURCE causes the image to be blurred multiple times
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .transforms(new CenterCrop(), new BlurTransformation(getContext(),5))
                .priority(Priority.HIGH)
                .into(new SimpleTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, Transition<? super Drawable> transition) {
                        if (isAdded() && !isDetached())
                            scrollContainer.post(() -> scrollContainer.setBackground(resource));
                    }
                });

        backButton = rootView.findViewById(R.id.sign_up_back_button);

        signUpButton = rootView.findViewById(R.id.sign_up_next_button);

        givenNameField = rootView.findViewById(R.id.sign_up_given_name);
        familyNameField = rootView.findViewById(R.id.sign_up_family_name);
        emailField = rootView.findViewById(R.id.sign_up_email);
        newPasswordField = rootView.findViewById(R.id.sign_up_new_password);
        confirmNewPasswordField = rootView.findViewById(R.id.sign_up_confirm_new_password);

        AppCompatActivity activity = (AppCompatActivity)getActivity();

        countrySpinner = rootView.findViewById(R.id.sign_up_country);

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

	public void setListener(SignUpListener listener) {
	    this.listener = listener;
    }

    private void setupViewListeners() {
        backButton.setOnClickListener(view -> {
            if (getActivity() != null)
                getActivity().onBackPressed();
        });

        signUpButton.setOnClickListener(view -> {
            backButton.setEnabled(false);
            signUpButton.setEnabled(false);

            SignUpUser signUpUser = viewModel.getSignUpUser();
            if (signUpUser == null) {
                backButton.setEnabled(true);
                signUpButton.setEnabled(true);

            } else {
                listener.onSignUpNextClick(signUpUser);
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

        genderSpinner.setOnItemSelectedListener(new SimpleOnItemSelectedListener() {
            @Override
            void onItemSelected(int position) {
                String gender = (String) genderSpinner.getSelectedItem();
                viewModel.onGenderSelected(gender);
            }
        });
    }

    @Override
    public String getTitle(Context context) {
        return null;
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
