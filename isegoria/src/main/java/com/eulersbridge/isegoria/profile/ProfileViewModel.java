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
import com.eulersbridge.isegoria.util.data.SingleLiveData;
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

    void showFriends() {

        boolean isAnotherUser = user.getValue() != null
                && user.getValue() instanceof Contact;


        if (!isAnotherUser) {
            IsegoriaApp app = getApplication();
            app.friendsVisible.setValue(true);
        }
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
        IsegoriaApp app = getApplication();
        app.logOut();
    }

    void setUser(GenericUser user) {
        this.user.setValue(user);
    }

    void fetchUserStats() {
        User user = getUser();

        if (user == null)
            return;

        IsegoriaApp app = getApplication();

        app.getAPI().getContact(user.email).enqueue(new SimpleCallback<Contact>() {
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
            IsegoriaApp app = getApplication();

            User user = getUser();
            if (user != null)
                remainingBadges = new RetrofitLiveData<>(app.getAPI().getRemainingBadges(user.getId()));
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

                    return new SingleLiveData<>(filteredBadges);
                }

                return new SingleLiveData<>(null);
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
            IsegoriaApp app = getApplication();

            User user = getUser();
            if (user != null)
                completedBadges = new RetrofitLiveData<>(app.getAPI().getCompletedBadges(user.getId()));
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

                return new SingleLiveData<>(filteredBadges);
            }

            return new SingleLiveData<>(null);
        });
    }

    LiveData<String> getInstitutionName() {
        if (institutionName == null) {
            IsegoriaApp app = getApplication();

            User user = getUser();
            if (user != null && user.institutionId != null) {

                LiveData<Institution> institutionRequest
                        = new RetrofitLiveData<>(app.getAPI().getInstitution(user.institutionId));

                institutionName = Transformations.switchMap(institutionRequest, institution ->
                        new SingleLiveData<>(institution == null? null : institution.getName()));

            } else {
                return new SingleLiveData<>(null);
            }
        }

        return institutionName;
    }

    LiveData<List<Task>> getTasks() {
        if (tasks == null) {
            IsegoriaApp app = getApplication();
            tasks = new RetrofitLiveData<>(app.getAPI().getTasks());
        }

        return tasks;
    }

    LiveData<List<Task>> getRemainingTasks() {
        if (remainingTasks == null || remainingTasks.getValue() == null) {
            IsegoriaApp app = getApplication();

            return Transformations.switchMap(app.loggedInUser, user -> {
                if (user != null) {
                    remainingTasks = new RetrofitLiveData<>(app.getAPI().getRemainingTasks(user.getId()));
                    return remainingTasks;
                }

                return new SingleLiveData<>(null);
            });
        }

        return remainingTasks;
    }

    LiveData<List<Task>> getCompletedTasks() {
        if (completedTasks == null || completedTasks.getValue() == null) {
            IsegoriaApp app = getApplication();

            return Transformations.switchMap(app.loggedInUser, user -> {
                if (user == null)
                    return new SingleLiveData<>(null);

                LiveData<List<Task>> tasksRequest = new RetrofitLiveData<>(app.getAPI().getCompletedTasks(user.getId()));

                completedTasks = Transformations.switchMap(tasksRequest, tasksList -> {
                    if (tasksList != null) {
                        long newTotalXp = 0;

                        for (Task task : tasksList)
                            newTotalXp += task.xpValue;

                        totalXp.setValue(newTotalXp);
                    }

                    return new SingleLiveData<>(tasksList);
                });

                return completedTasks;
            });
        }

        return completedTasks;
    }

    LiveData<Photo> getUserPhoto() {
        if (userPhoto == null) {
            IsegoriaApp app = getApplication();

            return Transformations.switchMap(app.loggedInUser, user -> {
                if (user == null)
                    return new SingleLiveData<>(null);

                LiveData<PhotosResponse> photosRequest = new RetrofitLiveData<>(app.getAPI().getPhotos(user.email));

                userPhoto = Transformations.switchMap(photosRequest, photosResponse -> {
                    if (photosResponse != null && photosResponse.totalPhotos > 0)
                        return new SingleLiveData<>(photosResponse.photos.get(0));

                    return new SingleLiveData<>(null);
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