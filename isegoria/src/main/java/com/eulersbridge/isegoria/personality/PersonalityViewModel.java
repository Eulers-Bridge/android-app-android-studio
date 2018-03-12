package com.eulersbridge.isegoria.personality;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.support.annotation.NonNull;

import com.eulersbridge.isegoria.IsegoriaApp;
import com.eulersbridge.isegoria.network.api.models.User;
import com.eulersbridge.isegoria.network.api.models.UserPersonality;
import com.eulersbridge.isegoria.util.data.SingleLiveData;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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

        IsegoriaApp app = getApplication();

        User user = app.loggedInUser.getValue();

        if (user != null) {
            Call<Void> request = app.getAPI().addUserPersonality(user.email, userPersonality);

            LiveData<Boolean> requestData = new LiveData<Boolean>() {

                @Override
                protected void onActive() {
                    if (!request.isCanceled() && !request.isExecuted())
                        request.enqueue(new Callback<Void>() {
                            @Override
                            public void onResponse(Call<Void> call, Response<Void> response) {
                                if (response.isSuccessful()) {
                                    postValue(true);
                                } else {
                                    postValue(false);
                                }
                            }

                            @Override
                            public void onFailure(Call<Void> call, Throwable t) {
                                postValue(null);
                            }
                        });
                }
            };

            return Transformations.switchMap(requestData, success -> {
                if (success != null && success) {
                    userCompletedQuestions.postValue(true);
                    return new SingleLiveData<>(true);
                }

                userCompletedQuestions.postValue(false);
                return new SingleLiveData<>(false);
            });
        }

        return new SingleLiveData<>(false);
    }
}
