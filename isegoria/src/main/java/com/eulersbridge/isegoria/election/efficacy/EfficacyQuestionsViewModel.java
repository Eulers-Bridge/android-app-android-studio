package com.eulersbridge.isegoria.election.efficacy;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.support.annotation.NonNull;

import com.eulersbridge.isegoria.IsegoriaApp;
import com.eulersbridge.isegoria.network.api.models.UserSelfEfficacy;
import com.eulersbridge.isegoria.util.data.SingleLiveData;
import com.eulersbridge.isegoria.util.data.RetrofitLiveData;

@SuppressWarnings("WeakerAccess")
public class EfficacyQuestionsViewModel extends AndroidViewModel {

    final MutableLiveData<Integer> score1 = new MutableLiveData<>();
    final MutableLiveData<Integer> score2 = new MutableLiveData<>();
    final MutableLiveData<Integer> score3 = new MutableLiveData<>();
    final MutableLiveData<Integer> score4 = new MutableLiveData<>();

    public EfficacyQuestionsViewModel(@NonNull Application application) {
        super(application);
    }

    LiveData<Boolean> addUserEfficacy() {
        if (score1.getValue() == null
                || score2.getValue() == null
                || score3.getValue() == null
                || score4.getValue() == null) {
            return new SingleLiveData<>(false);
        }

        IsegoriaApp isegoriaApp = getApplication();

        //noinspection ConstantConditions
        String userEmail = isegoriaApp.loggedInUser.getValue().email;

        UserSelfEfficacy answers = new UserSelfEfficacy(
                score1.getValue(), score2.getValue(), score3.getValue(), score4.getValue()
        );

        LiveData<Void> efficacyRequest = new RetrofitLiveData<>(isegoriaApp.getAPI().addUserEfficacy(userEmail, answers));
        return Transformations.switchMap(efficacyRequest, __ -> {
            isegoriaApp.onUserSelfEfficacyCompleted();

            return new SingleLiveData<>(true);
        });
    }

}
