package com.eulersbridge.isegoria.auth.login;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Transformations;
import android.support.annotation.NonNull;

import com.eulersbridge.isegoria.IsegoriaApp;
import com.eulersbridge.isegoria.util.data.FixedData;
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
        String userEmail = isegoriaApp.getLoggedInUser().email;

        LiveData<Void> verificationRequest = new RetrofitLiveData<>(isegoriaApp.getAPI().sendVerificationEmail(userEmail));
        return Transformations.switchMap(verificationRequest, __ -> new FixedData<>(true));
    }
}
