package com.eulersbridge.isegoria.personality;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.support.annotation.NonNull;

import com.eulersbridge.isegoria.IsegoriaApp;
import com.eulersbridge.isegoria.network.api.models.UserPersonality;
import com.eulersbridge.isegoria.util.data.RetrofitLiveData;
import com.eulersbridge.isegoria.util.data.FixedData;

@SuppressWarnings("WeakerAccess")
public class PersonalityViewModel extends AndroidViewModel {

    final MutableLiveData<Boolean> userSkippedQuestions = new MutableLiveData<>();
    final MutableLiveData<Boolean> userContinuedQuestions = new MutableLiveData<>();
    final MutableLiveData<Boolean> userCompletedQuestions = new MutableLiveData<>();

    public PersonalityViewModel(@NonNull Application application) {
        super(application);
    }

    void setUserSkippedQuestions() {
        userSkippedQuestions.setValue(true);
    }

    void setUserContinuedQuestions() {
        userContinuedQuestions.setValue(true);
    }

    LiveData<Boolean> setUserCompletedQuestions(@NonNull UserPersonality userPersonality) {

        IsegoriaApp isegoriaApp = getApplication();

        String userEmail = isegoriaApp.getLoggedInUser().email;

        LiveData<Void> request = new RetrofitLiveData<>(isegoriaApp.getAPI().addUserPersonality(userEmail, userPersonality));

        return Transformations.switchMap(request, success -> {
           if (success != null) {
               userCompletedQuestions.postValue(true);
               return new FixedData<>(true);
           }

           return new FixedData<>(false);
        });
    }
}
