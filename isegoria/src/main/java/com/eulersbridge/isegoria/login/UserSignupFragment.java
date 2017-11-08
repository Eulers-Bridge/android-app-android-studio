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
import com.eulersbridge.isegoria.network.GeneralInfoResponse;
import com.eulersbridge.isegoria.network.Network;
import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.models.Country;
import com.eulersbridge.isegoria.models.Institution;
import com.eulersbridge.isegoria.network.SimpleCallback;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Response;

public class UserSignupFragment extends Fragment implements OnItemSelectedListener {
	private List<Country> countries;
	private ArrayAdapter<String> spinnerArrayAdapter;
	private ArrayAdapter<String> spinnerInstitutionArrayAdapter;

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.user_signup_fragment, container, false);

        countries = new ArrayList<>();

		Isegoria isegoria = (Isegoria) getActivity().getApplication();
		isegoria.setCountryObjects(countries);

        Network network = new Network(isegoria);
        isegoria.setNetwork(network);

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
		ArrayAdapter<String> spinnerGenderArrayAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item);
        spinnerGenderArrayAdapter.add("Male");
        spinnerGenderArrayAdapter.add("Female");
        spinnerGenderArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(spinnerGenderArrayAdapter);
        
        Spinner spinnerYearOfBirth = rootView.findViewById(R.id.yearOfBirth);
		ArrayAdapter<String> spinnerYearOfBirthArrayAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item);
        int cnt = 0;
        for(int i=1900; i<=2014; i++) {
        	spinnerYearOfBirthArrayAdapter.add(String.valueOf(i));

            if(i == 1990) {
                spinnerYearOfBirth.setSelection(cnt);
            }
            cnt = cnt + 1;
        }
        spinnerYearOfBirthArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerYearOfBirth.setAdapter(spinnerYearOfBirthArrayAdapter);
        
        Country countryPlaceholder = new Country("Select Country");
        spinnerArrayAdapter.add(countryPlaceholder.name);
        spinnerInstitutionArrayAdapter.add("Select Institution");
		
		return rootView;
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
