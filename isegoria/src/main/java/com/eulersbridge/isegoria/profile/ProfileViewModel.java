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
import com.eulersbridge.isegoria.network.api.models.Contact;
import com.eulersbridge.isegoria.network.api.models.GenericUser;
import com.eulersbridge.isegoria.network.api.models.Institution;
import com.eulersbridge.isegoria.network.api.models.Photo;
import com.eulersbridge.isegoria.network.api.models.Task;
import com.eulersbridge.isegoria.network.api.models.User;
import com.eulersbridge.isegoria.network.api.responses.PhotosResponse;
import com.eulersbridge.isegoria.util.data.FixedData;
import com.eulersbridge.isegoria.util.data.RetrofitLiveData;
import com.eulersbridge.isegoria.util.network.SimpleCallback;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Response;

public class ProfileViewModel extends AndroidViewModel {

    final MutableLiveData<Integer> currentSectionIndex = new MutableLiveData<>();

    final MutableLiveData<GenericUser> user = new MutableLiveData<>();
    private final MutableLiveData<Integer> targetBadgeLevel = new MutableLiveData<>();

    private LiveData<List<Badge>> remainingBadges;
    private LiveData<List<Badge>> completedBadges;

    private LiveData<String> institutionName;

    private LiveData<List<Task>> tasks;
    private LiveData<List<Task>> remainingTasks;
    private LiveData<List<Task>> completedTasks;
    final MutableLiveData<Long> totalXp = new MutableLiveData<>();

    final MutableLiveData<Long> contactsCount = new MutableLiveData<>();
    final MutableLiveData<Long> totalTasksCount = new MutableLiveData<>();

    private LiveData<Photo> userPhoto;

    public ProfileViewModel(@NonNull Application application) {
        super(application);

        currentSectionIndex.setValue(0);
        targetBadgeLevel.setValue(0);
    }

    public void setTargetBadgeLevel(int targetBadgeLevel) {
        this.targetBadgeLevel.setValue(targetBadgeLevel);
    }

    void onSectionIndexChanged(int newIndex) {
        currentSectionIndex.setValue(newIndex);
    }

    void showTasksProgress() {
        currentSectionIndex.setValue(1);
    }

    void logOut() {
        IsegoriaApp isegoriaApp = getApplication();
        isegoriaApp.logOut();
    }

    void setUser(GenericUser user) {
        this.user.setValue(user);
    }

    void fetchUserStats() {
        User user = getUser();

        if (user == null)
            return;

        IsegoriaApp isegoriaApp = getApplication();

        isegoriaApp.getAPI().getContact(user.email).enqueue(new SimpleCallback<Contact>() {
            @Override
            protected void handleResponse(Response<Contact> response) {
                Contact contact = response.body();
                if (contact != null) {
                    contactsCount.setValue(contact.contactsCount);
                    totalTasksCount.setValue(contact.totalTasksCount);
                }
            }
        });
    }

    private @Nullable User getUser() {
        if (user.getValue() != null && user.getValue() instanceof User) {
            return (User) user.getValue();

        } else {
            return null;
        }
    }

    /**
     * @return A list of the badges the user has yet to complete, regardless of the badges'
     * or the user's level.
     */
    LiveData<List<Badge>> getRemainingBadges() {
        return getRemainingBadges(false);
    }

    /**
     * @param limitToLevel Whether to only include badges matching the user's target level.
     * @return A list of the badges the user has yet to complete, optionally filtered
     * by the user's target level.
     */
    public LiveData<List<Badge>> getRemainingBadges(boolean limitToLevel) {
        if (remainingBadges == null) {
            IsegoriaApp isegoriaApp = getApplication();

            User user = getUser();
            if (user != null)
                remainingBadges = new RetrofitLiveData<>(isegoriaApp.getAPI().getRemainingBadges(user.getId()));
        }

        if (limitToLevel) {
            return Transformations.switchMap(remainingBadges, badges -> {
                if (badges != null) {
                    List<Badge> filteredBadges = new ArrayList<>();

                    for (Badge badge : badges) {
                        // Target badge level initialised in constructor.
                        //noinspection ConstantConditions
                        if (badge.level == targetBadgeLevel.getValue())
                            filteredBadges.add(badge);
                    }

                    return new FixedData<>(filteredBadges);
                }

                return new FixedData<>(null);
            });

        } else {
            return remainingBadges;
        }
    }

    /**
     * @return A list of the user's completed badges, *filtered by those matching their target
     * badge level*.
     */
    public LiveData<List<Badge>> getCompletedBadges() {
        if (completedBadges == null) {
            IsegoriaApp isegoriaApp = getApplication();

            User user = getUser();
            if (user != null)
                completedBadges = new RetrofitLiveData<>(isegoriaApp.getAPI().getCompletedBadges(user.getId()));
        }

        return Transformations.switchMap(completedBadges, badges -> {
            if (badges != null) {
                List<Badge> filteredBadges = new ArrayList<>();

                for (Badge badge : badges) {
                    // Target badge level initialised in constructor.
                    //noinspection ConstantConditions
                    if (badge.level == targetBadgeLevel.getValue())
                        filteredBadges.add(badge);
                }

                return new FixedData<>(filteredBadges);
            }

            return new FixedData<>(null);
        });
    }

    LiveData<String> getInstitutionName() {
        if (institutionName == null) {
            IsegoriaApp isegoriaApp = getApplication();

            User user = getUser();
            if (user != null && user.institutionId != null) {

                LiveData<Institution> institutionRequest
                        = new RetrofitLiveData<>(isegoriaApp.getAPI().getInstitution(user.institutionId));

                institutionName = Transformations.switchMap(institutionRequest, institution ->
                        new FixedData<>(institution == null? null : institution.getName()));

            } else {
                return new FixedData<>(null);
            }
        }

        return institutionName;
    }

    LiveData<List<Task>> getTasks() {
        if (tasks == null) {
            IsegoriaApp isegoriaApp = getApplication();
            tasks = new RetrofitLiveData<>(isegoriaApp.getAPI().getTasks());
        }

        return tasks;
    }

    LiveData<List<Task>> getRemainingTasks() {
        if (remainingTasks == null || remainingTasks.getValue() == null) {
            IsegoriaApp isegoriaApp = getApplication();

            return Transformations.switchMap(isegoriaApp.loggedInUser, user -> {
                if (user != null) {
                    remainingTasks = new RetrofitLiveData<>(isegoriaApp.getAPI().getRemainingTasks(user.getId()));
                    return remainingTasks;
                }

                return new FixedData<>(null);
            });
        }

        return remainingTasks;
    }

    LiveData<List<Task>> getCompletedTasks() {
        if (completedTasks == null || completedTasks.getValue() == null) {
            IsegoriaApp isegoriaApp = getApplication();

            return Transformations.switchMap(isegoriaApp.loggedInUser, user -> {
                if (user == null)
                    return new FixedData<>(null);

                LiveData<List<Task>> tasksRequest = new RetrofitLiveData<>(isegoriaApp.getAPI().getCompletedTasks(user.getId()));

                completedTasks = Transformations.switchMap(tasksRequest, tasksList -> {
                    if (tasksList != null) {
                        long newTotalXp = 0;

                        for (Task task : tasksList)
                            newTotalXp += task.xpValue;

                        totalXp.setValue(newTotalXp);
                    }

                    return new FixedData<>(tasksList);
                });

                return completedTasks;
            });
        }

        return completedTasks;
    }

    LiveData<Photo> getUserPhoto() {
        if (userPhoto == null) {
            IsegoriaApp isegoriaApp = getApplication();

            return Transformations.switchMap(isegoriaApp.loggedInUser, user -> {
                if (user == null)
                    return new FixedData<>(null);

                LiveData<PhotosResponse> photosRequest = new RetrofitLiveData<>(isegoriaApp.getAPI().getPhotos(user.email));

                userPhoto = Transformations.switchMap(photosRequest, photosResponse -> {
                    if (photosResponse != null && photosResponse.totalPhotos > 0)
                        return new FixedData<>(photosResponse.photos.get(0));

                    return new FixedData<>(null);
                });

                return userPhoto;
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
        cancelIfPossible(institutionName);
        cancelIfPossible(tasks);
        cancelIfPossible(remainingTasks);
        cancelIfPossible(completedTasks);
        cancelIfPossible(userPhoto);
    }
}
