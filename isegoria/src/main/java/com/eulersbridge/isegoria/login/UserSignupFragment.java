package com.eulersbridge.isegoria.login;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.eulersbridge.isegoria.Isegoria;
import com.eulersbridge.isegoria.MainActivity;
import com.eulersbridge.isegoria.utilities.TitledFragment;
import com.eulersbridge.isegoria.network.GeneralInfoResponse;
import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.models.Country;
import com.eulersbridge.isegoria.models.Institution;
import com.eulersbridge.isegoria.network.SimpleCallback;
import com.eulersbridge.isegoria.utilities.Utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import retrofit2.Response;

public class UserSignupFragment extends Fragment implements OnItemSelectedListener, TitledFragment {

	private List<Country> countries;
	private ArrayAdapter<String> countryAdapter;
	private ArrayAdapter<String> institutionAdapter;

    private Spinner countrySpinner;
	private Spinner institutionSpinner;

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.user_signup_fragment, container, false);

        countries = new ArrayList<>();

        MainActivity mainActivity = (MainActivity)getActivity();

        rootView.findViewById(R.id.signup_next_button).setOnClickListener(view -> {
            if (mainActivity != null) mainActivity.userSignUpNext();
        });

        countrySpinner = rootView.findViewById(R.id.signup_country);
        countrySpinner.setOnItemSelectedListener(this);
        countryAdapter = new ArrayAdapter<>(mainActivity, android.R.layout.simple_spinner_item);
        countryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        countrySpinner.setAdapter(countryAdapter);

        institutionSpinner = rootView.findViewById(R.id.signup_institution);
        institutionAdapter = new ArrayAdapter<>(mainActivity, android.R.layout.simple_spinner_item);
        institutionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        institutionSpinner.setAdapter(institutionAdapter);
        
        Spinner spinnerGender = rootView.findViewById(R.id.signup_gender);
		ArrayAdapter<String> spinnerGenderArrayAdapter = new ArrayAdapter<>(mainActivity, android.R.layout.simple_spinner_item, new String[]{ "Male", "Female" });
        spinnerGenderArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(spinnerGenderArrayAdapter);
        
        Spinner spinnerYearOfBirth = rootView.findViewById(R.id.signup_birth_year);
		ArrayAdapter<String> spinnerYearOfBirthArrayAdapter = new ArrayAdapter<>(mainActivity, android.R.layout.simple_spinner_item);

		int year = Calendar.getInstance().get(Calendar.YEAR);

        int cnt = 0;
        for(int i = (year - 100); i <= 2014; i++) {
        	spinnerYearOfBirthArrayAdapter.add(String.valueOf(i));

            if (i == 1990) spinnerYearOfBirth.setSelection(cnt);

            cnt++;
        }
        spinnerYearOfBirthArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerYearOfBirth.setAdapter(spinnerYearOfBirthArrayAdapter);

        countryAdapter.add(getString(R.string.user_sign_up_choose_country_hint));
        institutionAdapter.add(getString(R.string.user_sign_up_choose_institution_hint));

        Isegoria isegoria = (Isegoria)mainActivity.getApplication();
        isegoria.getAPI().getGeneralInfo().enqueue(new SimpleCallback<GeneralInfoResponse>() {
            @Override
            protected void handleResponse(Response<GeneralInfoResponse> response) {
                GeneralInfoResponse body = response.body();
                if (body != null && body.countries.size() > 0) {
                    setCountries(body.countries);
                }
            }
        });

        Utils.showKeyboard(mainActivity.getWindow());
		
		return rootView;
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

    public void onNothingSelected(AdapterView<?> parent) {

    }
}
