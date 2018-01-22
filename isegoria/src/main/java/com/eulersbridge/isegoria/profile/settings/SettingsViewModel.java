package com.eulersbridge.isegoria.profile.settings;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.eulersbridge.isegoria.IsegoriaApp;
import com.eulersbridge.isegoria.network.api.models.Photo;
import com.eulersbridge.isegoria.network.api.models.User;
import com.eulersbridge.isegoria.network.api.models.UserSettings;
import com.eulersbridge.isegoria.network.api.responses.PhotosResponse;
import com.eulersbridge.isegoria.util.data.SingleLiveData;
import com.eulersbridge.isegoria.util.data.RetrofitLiveData;

import java.io.File;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@SuppressWarnings("WeakerAccess")
public class SettingsViewModel extends AndroidViewModel {

    private LiveData<Photo> userPhoto;

    final MutableLiveData<Boolean> optOutDataCollectionSwitchChecked = new MutableLiveData<>();
    final MutableLiveData<Boolean> optOutDataCollectionSwitchEnabled = new MutableLiveData<>();

    final MutableLiveData<Boolean> doNotTrackSwitchChecked = new MutableLiveData<>();
    final MutableLiveData<Boolean> doNotTrackSwitchEnabled = new MutableLiveData<>();

    public SettingsViewModel(@NonNull Application application) {
        super(application);

        optOutDataCollectionSwitchEnabled.setValue(false);
        doNotTrackSwitchEnabled.setValue(false);

        IsegoriaApp isegoriaApp = (IsegoriaApp) application;
        User user = isegoriaApp.loggedInUser.getValue();
        if (user != null) {
            optOutDataCollectionSwitchChecked.setValue(user.isOptedOutOfDataCollection);
            optOutDataCollectionSwitchEnabled.setValue(true);

            doNotTrackSwitchChecked.setValue(user.trackingOff);
            doNotTrackSwitchEnabled.setValue(true);
        }
    }

    void onOptOutDataCollectionChange(boolean isChecked) {
        optOutDataCollectionSwitchEnabled.setValue(false);

        IsegoriaApp isegoriaApp = getApplication();
        User user = isegoriaApp.loggedInUser.getValue();

        if (user != null) {
            UserSettings userSettings = new UserSettings(user.trackingOff, isChecked);

            isegoriaApp.getAPI().updateUserDetails(user.email, userSettings).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        optOutDataCollectionSwitchChecked.setValue(isChecked);
                        optOutDataCollectionSwitchEnabled.setValue(true);

                        isegoriaApp.setOptedOutOfDataCollection(isChecked);
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    optOutDataCollectionSwitchChecked.setValue(!isChecked);
                    optOutDataCollectionSwitchEnabled.setValue(true);
                }
            });
        }
    }

    void onTrackingChange(boolean isChecked) {
        doNotTrackSwitchEnabled.setValue(false);

        IsegoriaApp isegoriaApp = getApplication();
        User user = isegoriaApp.loggedInUser.getValue();

        if (user != null) {
            UserSettings userSettings = new UserSettings(isChecked, user.isOptedOutOfDataCollection);

            isegoriaApp.getAPI().updateUserDetails(user.email, userSettings).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        doNotTrackSwitchChecked.setValue(isChecked);

                        isegoriaApp.setTrackingOff(isChecked);

                    } else {
                        doNotTrackSwitchChecked.setValue(!isChecked);
                    }

                    doNotTrackSwitchEnabled.setValue(true);
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    // Restore to previous checked state
                    doNotTrackSwitchChecked.setValue(!isChecked);
                    doNotTrackSwitchEnabled.setValue(true);
                }
            });
        }
    }

    LiveData<String> getUserProfilePhotoURL() {
        IsegoriaApp isegoriaApp = getApplication();

        return Transformations.switchMap(isegoriaApp.loggedInUser, user ->
            new SingleLiveData<>(user == null? null : user.profilePhotoURL)
        );
    }

    LiveData<Photo> getUserPhoto() {
        if (userPhoto == null) {
            IsegoriaApp isegoriaApp = getApplication();
            User user = isegoriaApp.loggedInUser.getValue();

            if (user != null) {
                LiveData<PhotosResponse> photosRequest = new RetrofitLiveData<>(isegoriaApp.getAPI().getPhotos(user.email));

                userPhoto = Transformations.switchMap(photosRequest, photosResponse -> {
                    if (photosResponse != null && photosResponse.totalPhotos > 0)
                        return new SingleLiveData<>(photosResponse.photos.get(0));

                    return new SingleLiveData<>(null);
                });
            }
        }

        return userPhoto;
    }

    LiveData<Boolean> updateUserPhoto(Uri imageUri) {
        File file =  new File(imageUri.getPath());

        return IsegoriaApp.networkService.uploadNewUserPhoto(file);
    }

    @Override
    protected void onCleared() {
        if (userPhoto != null && userPhoto instanceof RetrofitLiveData)
            ((RetrofitLiveData) userPhoto).cancel();
    }
}
