package com.eulersbridge.isegoria.auth;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.support.annotation.NonNull;

import com.eulersbridge.isegoria.IsegoriaApp;
import com.eulersbridge.isegoria.auth.signup.SignUpUser;
import com.eulersbridge.isegoria.network.api.models.Country;
import com.eulersbridge.isegoria.network.api.models.Institution;
import com.eulersbridge.isegoria.network.api.responses.GeneralInfoResponse;
import com.eulersbridge.isegoria.util.data.SingleLiveData;
import com.eulersbridge.isegoria.util.network.SimpleCallback;

import java.util.List;

import retrofit2.Response;


public class AuthViewModel extends AndroidViewModel {

    private final MutableLiveData<List<Country>> countriesData = new MutableLiveData<>();

    final public MutableLiveData<Boolean> signUpVisible = new MutableLiveData<>();
    final public MutableLiveData<SignUpUser> signUpUser = new MutableLiveData<>();
    final MutableLiveData<Boolean> signUpConsentGiven =  new MutableLiveData<>();
    final public MutableLiveData<Boolean> verificationComplete = new MutableLiveData<>();

    final public MutableLiveData<Boolean> userLoggedIn = new MutableLiveData<>();

    public AuthViewModel(@NonNull Application application) {
        super(application);

        IsegoriaApp app = (IsegoriaApp) application;

        app.getAPI().getGeneralInfo().enqueue(new SimpleCallback<GeneralInfoResponse>() {
            @Override
            protected void handleResponse(Response<GeneralInfoResponse> response) {
                GeneralInfoResponse body = response.body();

                if (body != null && body.countries.size() > 0)
                    countriesData.setValue(body.countries);
            }
        });
    }

    public void onSignUpBackPressed() {
        signUpVisible.setValue(false);
        signUpUser.setValue(null);
    }

    public void setSignUpConsentGiven(boolean consentGiven) {
        signUpConsentGiven.setValue(consentGiven);
    }

    LiveData<Boolean> signUp() {
        List<Country> countries = countriesData.getValue();
        if (countries == null)
            return new SingleLiveData<>(false);

        // Not possible for signUpUser's value to be null,
        // as sign-up process is linear and gated.
        //noinspection ConstantConditions
        SignUpUser updatedUser = new SignUpUser(signUpUser.getValue());

        Long institutionId = null;

        for (Country country : countries)
            for (Institution countryInstitution : country.institutions)
                if (countryInstitution.getName().equals(updatedUser.institutionName))
                    institutionId = countryInstitution.id;

        if (institutionId != null) {
            updatedUser.institutionId = institutionId;
            signUpUser.setValue(updatedUser);
        }

        return Transformations.switchMap(IsegoriaApp.networkService.signUp(updatedUser), success -> {
            if (!success)
                signUpUser.setValue(null);

            return new SingleLiveData<>(success);
        });
    }

}
