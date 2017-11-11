package com.eulersbridge.isegoria.login;

import android.app.Activity;
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
import com.eulersbridge.isegoria.utilities.TitledFragment;
import com.eulersbridge.isegoria.network.GeneralInfoResponse;
import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.models.Country;
import com.eulersbridge.isegoria.models.Institution;
import com.eulersbridge.isegoria.network.SimpleCallback;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import retrofit2.Response;

public class UserSignupFragment extends Fragment implements OnItemSelectedListener, TitledFragment {
	private List<Country> countries;
	private ArrayAdapter<String> spinnerArrayAdapter;
	private ArrayAdapter<String> spinnerInstitutionArrayAdapter;

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.user_signup_fragment, container, false);

        countries = new ArrayList<>();

		Isegoria isegoria = (Isegoria) getActivity().getApplication();

        isegoria.getAPI().getGeneralInfo().enqueue(new SimpleCallback<GeneralInfoResponse>() {
            @Override
            protected void handleResponse(Response<GeneralInfoResponse> response) {
                GeneralInfoResponse body = response.body();
                if (body != null && body.countries.size() > 0) {
                    setCountries(body.countries);
                }
            }
        });
        
        Spinner spinner = rootView.findViewById(R.id.country);
        spinner.setOnItemSelectedListener(this);
        spinnerArrayAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerArrayAdapter);
 
        Spinner spinnerInstitution = rootView.findViewById(R.id.institution);
        spinnerInstitutionArrayAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item);
        spinnerInstitutionArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerInstitution.setAdapter(spinnerInstitutionArrayAdapter);
        
        Spinner spinnerGender = rootView.findViewById(R.id.gender);
		ArrayAdapter<String> spinnerGenderArrayAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, new String[]{ "Male", "Female" });
        spinnerGenderArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(spinnerGenderArrayAdapter);
        
        Spinner spinnerYearOfBirth = rootView.findViewById(R.id.yearOfBirth);
		ArrayAdapter<String> spinnerYearOfBirthArrayAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item);

		int year = Calendar.getInstance().get(Calendar.YEAR);

        int cnt = 0;
        for(int i = (year - 100); i <= 2014; i++) {
        	spinnerYearOfBirthArrayAdapter.add(String.valueOf(i));

            if (i == 1990) spinnerYearOfBirth.setSelection(cnt);

            cnt++;
        }
        spinnerYearOfBirthArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerYearOfBirth.setAdapter(spinnerYearOfBirthArrayAdapter);
        
        Country countryPlaceholder = new Country(getString(R.string.user_sign_up_choose_country_hint));
        spinnerArrayAdapter.add(countryPlaceholder.name);
        spinnerInstitutionArrayAdapter.add(getString(R.string.user_sign_up_choose_institution_hint));
		
		return rootView;
	}

    @Override
    public String getTitle() {
        return null;
    }

    private void setCountries(List<Country> countries) {
		Activity activity = getActivity();
		if (activity != null) {
			activity.runOnUiThread(() -> {

			    for (Country country : countries) {
			        spinnerArrayAdapter.add(country.name);
                }

                UserSignupFragment.this.countries = countries;
            });
		}
	}
	
    public void onItemSelected(AdapterView<?> parent, View view, 
            int pos, long id) {
    	String selectedCountry = (String) parent.getSelectedItem();
    	spinnerInstitutionArrayAdapter.clear();

    	for (Country country : countries) {
            if (selectedCountry.equals(country.name)) {
                for (Institution institution : country.institutions) {
                    spinnerInstitutionArrayAdapter.add(institution.name);
                }
            }
        }
    }

    public void onNothingSelected(AdapterView<?> parent) {

    }
}
