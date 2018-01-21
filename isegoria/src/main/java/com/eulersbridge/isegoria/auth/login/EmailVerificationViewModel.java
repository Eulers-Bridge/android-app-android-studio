package com.eulersbridge.isegoria.auth.login;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Transformations;
import android.support.annotation.NonNull;

import com.eulersbridge.isegoria.IsegoriaApp;
import com.eulersbridge.isegoria.util.data.SingleLiveData;
import com.eulersbridge.isegoria.util.data.RetrofitLiveData;

@SuppressWarnings("WeakerAccess")
public class EmailVerificationViewModel extends AndroidViewModel {

    public EmailVerificationViewModel(@NonNull Application application) {
        super(application);
    }

    void userVerified() {
        IsegoriaApp isegoriaApp = getApplication();

        isegoriaApp.login(null, null);
    }

    LiveData<Boolean> resendVerification() {
        IsegoriaApp isegoriaApp = getApplication();

        return Transformations.switchMap(isegoriaApp.loggedInUser, user -> {
            if (user != null) {
                LiveData<Void> verificationRequest = new RetrofitLiveData<>(isegoriaApp.getAPI().sendVerificationEmail(user.email));
                return Transformations.switchMap(verificationRequest, __ -> new SingleLiveData<>(true));
            }

            return new SingleLiveData<>(false);
        });

    }
}
