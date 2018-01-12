package com.eulersbridge.isegoria.profile;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.eulersbridge.isegoria.IsegoriaApp;
import com.eulersbridge.isegoria.network.api.models.Badge;
import com.eulersbridge.isegoria.network.api.models.GenericUser;
import com.eulersbridge.isegoria.network.api.models.Institution;
import com.eulersbridge.isegoria.network.api.models.Photo;
import com.eulersbridge.isegoria.network.api.models.Task;
import com.eulersbridge.isegoria.network.api.models.User;
import com.eulersbridge.isegoria.network.api.responses.PhotosResponse;
import com.eulersbridge.isegoria.util.data.RetrofitLiveData;
import com.eulersbridge.isegoria.util.data.FixedData;

import java.util.List;

public class ProfileViewModel extends AndroidViewModel {

    final MutableLiveData<GenericUser> user = new MutableLiveData<>();

    private LiveData<List<Badge>> remainingBadges;
    private LiveData<List<Badge>> completedBadges;

    private LiveData<Institution> institution;

    private LiveData<List<Task>> tasks;
    private LiveData<List<Task>> remainingTasks;
    private LiveData<List<Task>> completedTasks;

    private LiveData<Photo> userPhoto;

    public ProfileViewModel(@NonNull Application application) {
        super(application);

        IsegoriaApp isegoriaApp = (IsegoriaApp)application;
        user.setValue(isegoriaApp.getLoggedInUser());
    }

    void setUser(GenericUser user) {
        this.user.setValue(user);
    }

    private @Nullable User getUser() {
        if (user.getValue() != null && user.getValue() instanceof User) {
            return (User) user.getValue();

        } else {
            return null;
        }
    }

    public LiveData<List<Badge>> getRemainingBadges() {
        if (remainingBadges == null) {
            IsegoriaApp isegoriaApp = getApplication();

            User user = getUser();
            if (user != null) {
                remainingBadges = new RetrofitLiveData<>(isegoriaApp.getAPI().getRemainingBadges(user.getId()));
            } else {
                return new FixedData<>(null);
            }
        }

        return remainingBadges;
    }

    public LiveData<List<Badge>> getCompletedBadges() {
        if (completedBadges == null) {
            IsegoriaApp isegoriaApp = getApplication();

            User user = getUser();
            if (user != null) {
                completedBadges = new RetrofitLiveData<>(isegoriaApp.getAPI().getCompletedBadges(user.getId()));
            } else {
                return new FixedData<>(null);
            }
        }

        return completedBadges;
    }

    LiveData<Institution> getInstitution() {
        if (institution == null) {
            IsegoriaApp isegoriaApp = getApplication();

            User user = getUser();
            if (user != null && user.institutionId != null) {
                institution = new RetrofitLiveData<>(isegoriaApp.getAPI().getInstitution(user.institutionId));
            } else {
                return new FixedData<>(null);
            }
        }

        return institution;
    }

    LiveData<List<Task>> getTasks() {
        if (tasks == null) {
            IsegoriaApp isegoriaApp = getApplication();
            tasks = new RetrofitLiveData<>(isegoriaApp.getAPI().getTasks());
        }

        return tasks;
    }

    LiveData<List<Task>> getRemainingTasks() {
        if (remainingTasks == null) {
            IsegoriaApp isegoriaApp = getApplication();
            User user = isegoriaApp.getLoggedInUser();
            remainingTasks = new RetrofitLiveData<>(isegoriaApp.getAPI().getRemainingTasks(user.getId()));
        }

        return remainingTasks;
    }

    LiveData<List<Task>> getCompletedTasks() {
        if (completedTasks == null) {
            IsegoriaApp isegoriaApp = getApplication();
            User user = isegoriaApp.getLoggedInUser();
            completedTasks = new RetrofitLiveData<>(isegoriaApp.getAPI().getCompletedTasks(user.getId()));
        }

        return completedTasks;
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

    /**
     * Convenience method to cancel a LiveData object if it exists and is a Retrofit API request.
     */
    private void cancelIfPossible(@Nullable LiveData liveData) {
        if (liveData != null && liveData instanceof RetrofitLiveData)
            ((RetrofitLiveData) liveData).cancel();
    }

    @Override
    protected void onCleared() {
        cancelIfPossible(remainingBadges);
        cancelIfPossible(completedBadges);
        cancelIfPossible(institution);
        cancelIfPossible(tasks);
        cancelIfPossible(remainingTasks);
        cancelIfPossible(completedTasks);
        cancelIfPossible(userPhoto);
    }
}
