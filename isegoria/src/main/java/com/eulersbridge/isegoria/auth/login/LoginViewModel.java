package com.eulersbridge.isegoria.auth.login;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.eulersbridge.isegoria.IsegoriaApp;
import com.eulersbridge.isegoria.util.Constants;
import com.eulersbridge.isegoria.util.Strings;
import com.eulersbridge.isegoria.util.Utils;
import com.eulersbridge.isegoria.util.data.SingleLiveData;
import com.eulersbridge.isegoria.util.network.IgnoredCallback;
import com.securepreferences.SecurePreferences;

@SuppressWarnings("WeakerAccess")
public class LoginViewModel extends AndroidViewModel {

    final MutableLiveData<String> email = new MutableLiveData<>();
    final LiveData<Boolean> emailError = Transformations.switchMap(email, emailStr ->
            new SingleLiveData<>(!Strings.isValidEmail(emailStr)));

    final MutableLiveData<String> password = new MutableLiveData<>();

    final LiveData<Boolean> passwordError = Transformations.switchMap(password, passwordStr ->
            new SingleLiveData<>(TextUtils.isEmpty(passwordStr)));

    final MutableLiveData<Boolean> formEnabled = new MutableLiveData<>();
    final MutableLiveData<Boolean> networkError = new MutableLiveData<>();

    final MutableLiveData<Boolean> canShowPasswordResetDialog = new MutableLiveData<>();

    public LoginViewModel(@NonNull Application application) {
        super(application);

        formEnabled.setValue(true);
        networkError.setValue(false);
        canShowPasswordResetDialog.setValue(true);

        SecurePreferences securePreferences = new SecurePreferences(getApplication());

        String storedEmail = securePreferences.getString(Constants.USER_EMAIL_KEY, null);
        if (storedEmail != null)
            email.setValue(storedEmail);

        String storedPassword = securePreferences.getString(Constants.USER_PASSWORD_KEY, null);
        if (storedPassword != null)
            password.setValue(storedPassword);
    }

    public void setEmail(String email) {
        this.email.setValue(email);
    }

    public void setPassword(String password) {
        this.password.setValue(password);
    }

    LiveData<Boolean> login() {
        formEnabled.setValue(false);

        if (emailError.getValue() != null && !emailError.getValue()
                && passwordError.getValue() != null && !passwordError.getValue()) {

            IsegoriaApp isegoriaApp = getApplication();

            if (!Utils.isNetworkAvailable(isegoriaApp)) {
                networkError.setValue(true);
                formEnabled.setValue(true);

            } else {
                networkError.setValue(false);

                String email = this.email.getValue();
                String password = this.password.getValue();

                // Not null as email & password validation checks test for null
                //noinspection ConstantConditions
                LiveData<Boolean> loginRequest = isegoriaApp.login(email, password);

                return Transformations.switchMap(loginRequest, success -> {
                    if (success != null && success)
                        return new SingleLiveData<>(true);

                    formEnabled.setValue(true);
                    return new SingleLiveData<>(false);
                });
            }
        }

        formEnabled.setValue(true);
        return new SingleLiveData<>(false);
    }

    void setNetworkErrorShown() {
        networkError.setValue(false);
    }

    boolean requestPasswordRecoveryEmail(@Nullable String email) {
        if (Strings.isValidEmail(email)) {
            IsegoriaApp isegoriaApp = getApplication();

            canShowPasswordResetDialog.setValue(false);

            isegoriaApp.getAPI().requestPasswordReset(email).enqueue(new IgnoredCallback<>());

            canShowPasswordResetDialog.setValue(true);

            return true;
        }

        return false;
    }
}
