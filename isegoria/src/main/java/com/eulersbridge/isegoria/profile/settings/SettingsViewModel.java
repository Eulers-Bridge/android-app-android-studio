package com.eulersbridge.isegoria.profile.settings;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Transformations;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.eulersbridge.isegoria.IsegoriaApp;
import com.eulersbridge.isegoria.network.api.models.Photo;
import com.eulersbridge.isegoria.network.api.models.User;
import com.eulersbridge.isegoria.network.api.models.UserSettings;
import com.eulersbridge.isegoria.network.api.responses.PhotosResponse;
import com.eulersbridge.isegoria.util.data.RetrofitLiveData;
import com.eulersbridge.isegoria.util.data.FixedData;

import java.io.File;

@SuppressWarnings("WeakerAccess")
public class SettingsViewModel extends AndroidViewModel {

    private LiveData<Photo> userPhoto;

    public SettingsViewModel(@NonNull Application application) {
        super(application);
    }

    User getUser() {
        IsegoriaApp isegoriaApp = getApplication();
        return isegoriaApp.getLoggedInUser();
    }

    LiveData<Boolean> setTrackingOff(boolean trackingOff) {
        IsegoriaApp isegoriaApp = getApplication();

        isegoriaApp.setTrackingOff(trackingOff);

        User user = isegoriaApp.getLoggedInUser();
        UserSettings userSettings = new UserSettings(trackingOff, user.isOptedOutOfDataCollection);

        LiveData<Void> updateUserDetails = new RetrofitLiveData<>(isegoriaApp.getAPI().updateUserDetails(user.email, userSettings));
        return Transformations.switchMap(updateUserDetails,
                success -> new FixedData<>(success != null));
    }

    LiveData<Boolean> setOptedOutOfDataCollection(boolean optOutDataCollection) {
        IsegoriaApp isegoriaApp = getApplication();

        isegoriaApp.setOptedOutOfDataCollection(optOutDataCollection);

        User user = isegoriaApp.getLoggedInUser();
        UserSettings userSettings = new UserSettings(user.trackingOff, optOutDataCollection);

        LiveData<Void> updateUserDetails = new RetrofitLiveData<>(isegoriaApp.getAPI().updateUserDetails(user.email, userSettings));
        return Transformations.switchMap(updateUserDetails,
                success -> new FixedData<>(success != null));
    }

    LiveData<Photo> getUserPhoto() {
        if (userPhoto == null) {
            IsegoriaApp isegoriaApp = getApplication();
            User user = isegoriaApp.getLoggedInUser();
            LiveData<PhotosResponse> photosRequest = new RetrofitLiveData<>(isegoriaApp.getAPI().getPhotos(user.email));

            userPhoto = Transformations.switchMap(photosRequest, photosResponse -> {
                if (photosResponse != null && photosResponse.totalPhotos > 0)
                    return new FixedData<>(photosResponse.photos.get(0));

                return new FixedData<>(null);
            });
        }

        return userPhoto;
    }

    void updateUserPhoto(Uri imageUri) {
        File file =  new File(imageUri.getPath());

        IsegoriaApp isegoriaApp = getApplication();
        isegoriaApp.getNetworkService().s3Upload(file);
    }

    @Override
    protected void onCleared() {
        if (userPhoto != null && userPhoto instanceof RetrofitLiveData)
            ((RetrofitLiveData) userPhoto).cancel();
    }
}
