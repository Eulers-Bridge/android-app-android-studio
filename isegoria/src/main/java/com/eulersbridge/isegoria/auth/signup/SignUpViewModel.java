package com.eulersbridge.isegoria.auth.signup;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.eulersbridge.isegoria.IsegoriaApp;
import com.eulersbridge.isegoria.network.api.models.Country;
import com.eulersbridge.isegoria.network.api.models.Institution;
import com.eulersbridge.isegoria.network.api.responses.GeneralInfoResponse;
import com.eulersbridge.isegoria.util.data.SingleLiveData;
import com.eulersbridge.isegoria.util.data.RetrofitLiveData;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("WeakerAccess")
public class SignUpViewModel extends AndroidViewModel {
    private LiveData<List<Country>> countries;

    private final MutableLiveData<String> givenName = new MutableLiveData<>();
    private final MutableLiveData<String> familyName = new MutableLiveData<>();
    private final MutableLiveData<String> email = new MutableLiveData<>();
    private final MutableLiveData<String> password = new MutableLiveData<>();
    private final MutableLiveData<String> confirmPassword = new MutableLiveData<>();
    private final MutableLiveData<Country> selectedCountry = new MutableLiveData<>();
    private final MutableLiveData<Institution> selectedInstitution = new MutableLiveData<>();
    private final MutableLiveData<String> selectedBirthYear = new MutableLiveData<>();
    private final MutableLiveData<String> selectedGender = new MutableLiveData<>();

    void setGivenName(String givenName) {
        this.givenName.setValue(givenName);
    }

    void setFamilyName(String familyName) {
        this.familyName.setValue(familyName);
    }

    void setEmail(String email) {
        this.email.setValue(email);
    }

    void setPassword(String password) {
        this.password.setValue(password);
    }

    void setConfirmPassword(String confirmPassword) {
        this.confirmPassword.setValue(confirmPassword);
    }

    public SignUpViewModel(@NonNull Application application) {
        super(application);
    }

    LiveData<List<Country>> getCountries() {
        if (countries == null) {
            IsegoriaApp isegoriaApp = getApplication();

            LiveData<GeneralInfoResponse> generalInfo = new RetrofitLiveData<>(isegoriaApp.getAPI().getGeneralInfo());

            countries = Transformations.switchMap(generalInfo, info -> {
                if (info != null && info.countries.size() > 0)
                    return new SingleLiveData<>(info.countries);

                return new SingleLiveData<>(null);
            });
        }

        return countries;
    }

    @Nullable
    List<Institution> onCountrySelected(int index) {
        selectedInstitution.setValue(null);

        List<Country> countriesList = countries.getValue();
        if (countriesList != null) {
            Country country = countriesList.get(index);

            selectedCountry.setValue(country);

            if (country.institutions != null) {
                List<Institution> institutions = new ArrayList<>();
                institutions.addAll(country.institutions);

                return institutions;
            }
        }

        return null;
    }

    void onInstitutionSelected(int index) {
        List<Country> countriesList = countries.getValue();
        if (countriesList != null) {
            Country country = countriesList.get(index);

            Institution institution = country.institutions.get(index);
            selectedInstitution.setValue(institution);
        }
    }

    void onBirthYearSelected(@NonNull String birthYear) {
        selectedBirthYear.setValue(birthYear);
    }

    void onGenderSelected(@NonNull String gender) {
        selectedGender.setValue(gender);
    }


    @Nullable SignUpUser getSignUpUser() {
        String givenName = this.givenName.getValue();
        boolean givenNameValid = !TextUtils.isEmpty(givenName);

        String familyName = this.familyName.getValue();
        boolean familyNameValid = !TextUtils.isEmpty(familyName);

        String email = this.email.getValue();
        boolean emailValid = !TextUtils.isEmpty(email);

        String password = this.password.getValue();
        boolean passwordValid = !TextUtils.isEmpty(password);

        String confirmPassword = this.confirmPassword.getValue();
        boolean passwordsMatch = passwordValid && password.equals(confirmPassword);

        Country country = selectedCountry.getValue();
        boolean countryValid = country != null;

        Institution institution = selectedInstitution.getValue();
        boolean institutionValid = institution != null;

        String birthYear = selectedBirthYear.getValue();
        boolean birthYearValid = birthYear != null;

        String gender = selectedGender.getValue();
        boolean genderValid = gender != null;

        boolean allFieldsValid = givenNameValid && familyNameValid && emailValid
                && passwordValid && passwordsMatch && countryValid
                && institutionValid && birthYearValid && genderValid;

        if (allFieldsValid)
            return new SignUpUser(givenName, familyName, gender, country.name, birthYear,
                    email, password, institution.getName());

        return null;
    }

}
