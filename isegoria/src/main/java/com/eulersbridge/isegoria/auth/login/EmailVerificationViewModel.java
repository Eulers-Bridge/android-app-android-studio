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

    void onExit() {
        IsegoriaApp app = getApplication();
        app.userVerificationVisible.setValue(false);
    }

    LiveData<Boolean> userVerified() {
        IsegoriaApp app = getApplication();
        return app.login();
    }

    LiveData<Boolean> resendVerification() {
        IsegoriaApp app = getApplication();

        return Transformations.switchMap(app.loggedInUser, user -> {
            if (user != null) {
                LiveData<Void> verificationRequest = new RetrofitLiveData<>(app.getAPI().sendVerificationEmail(user.email));
                return Transformations.switchMap(verificationRequest, __ -> new SingleLiveData<>(true));
            }

            return new SingleLiveData<>(false);
        });

    }
}
