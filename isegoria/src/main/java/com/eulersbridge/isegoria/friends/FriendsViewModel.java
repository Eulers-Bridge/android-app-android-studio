package com.eulersbridge.isegoria.friends;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.eulersbridge.isegoria.IsegoriaApp;
import com.eulersbridge.isegoria.network.api.models.Contact;
import com.eulersbridge.isegoria.network.api.models.FriendRequest;
import com.eulersbridge.isegoria.network.api.models.Institution;
import com.eulersbridge.isegoria.network.api.models.User;
import com.eulersbridge.isegoria.util.Strings;
import com.eulersbridge.isegoria.util.data.SingleLiveData;
import com.eulersbridge.isegoria.util.data.RetrofitLiveData;

import java.util.List;

@SuppressWarnings("WeakerAccess")
public class FriendsViewModel extends AndroidViewModel {

    private LiveData<List<User>> searchResults;
    private LiveData<List<FriendRequest>> sentFriendRequests;
    private LiveData<List<FriendRequest>> receivedFriendRequests;
    private LiveData<List<Contact>> friends;

    final MutableLiveData<Boolean> searchSectionVisible = new MutableLiveData<>();
    final MutableLiveData<Boolean> sentRequestsVisible = new MutableLiveData<>();
    final MutableLiveData<Boolean> receivedRequestsVisible = new MutableLiveData<>();
    final MutableLiveData<Boolean> friendsVisible = new MutableLiveData<>();

    public FriendsViewModel(@NonNull Application application) {
        super(application);

        searchSectionVisible.setValue(false);
        sentRequestsVisible.setValue(false);
        receivedRequestsVisible.setValue(false);
        friendsVisible.setValue(true);
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
        cancelIfPossible(friends);
        cancelIfPossible(sentFriendRequests);
        cancelIfPossible(receivedFriendRequests);
        cancelIfPossible(searchResults);
    }

    private void showSearch() {
        searchSectionVisible.setValue(true);
        sentRequestsVisible.setValue(false);
        receivedRequestsVisible.setValue(false);
        friendsVisible.setValue(false);
    }

    void hideSearch() {
        searchSectionVisible.setValue(false);

        boolean haveSentRequests = sentFriendRequests.getValue() != null
                && sentFriendRequests.getValue().size() > 0;

        sentRequestsVisible.setValue(haveSentRequests);

        boolean haveReceivedRequests = receivedFriendRequests.getValue() != null
                && receivedFriendRequests.getValue().size() > 0;

        receivedRequestsVisible.setValue(haveReceivedRequests);

        friendsVisible.setValue(true);
    }

    LiveData<List<User>> onSearchQueryChanged(String query) {
        showSearch();

        if (!TextUtils.isEmpty(query) && query.length() > 2) {
            IsegoriaApp isegoriaApp = getApplication();

            searchResults = new RetrofitLiveData<>(isegoriaApp.getAPI().searchForUsers(query));

        } else {
            searchResults = new SingleLiveData<>(null);
        }

        return searchResults;
    }

    LiveData<List<Contact>> getFriends() {
        if (friends == null) {
            IsegoriaApp isegoriaApp = getApplication();
            friends = new RetrofitLiveData<>(isegoriaApp.getAPI().getFriends());
        }

        return friends;
    }

    LiveData<List<FriendRequest>> getSentFriendRequests() {
        if (sentFriendRequests == null) {
            IsegoriaApp isegoriaApp = getApplication();

            return Transformations.switchMap(isegoriaApp.loggedInUser, user -> {
                if (user != null) {

                    LiveData<List<FriendRequest>> requests = new RetrofitLiveData<>(isegoriaApp.getAPI().getFriendRequestsSent(user.getId()));
                    sentFriendRequests = Transformations.switchMap(requests, sentFriendRequests -> {
                        sentRequestsVisible.setValue(sentFriendRequests != null && sentFriendRequests.size() > 0);

                        return new SingleLiveData<>(sentFriendRequests);
                    });

                    return sentFriendRequests;
                }

                return new SingleLiveData<>(null);
            });
        }

        return sentFriendRequests;
    }

    LiveData<List<FriendRequest>> getReceivedFriendRequests() {
        if (receivedFriendRequests == null) {
            IsegoriaApp isegoriaApp = getApplication();

            return Transformations.switchMap(isegoriaApp.loggedInUser, user -> {
                if (user != null) {

                    LiveData<List<FriendRequest>> requests = new RetrofitLiveData<>(isegoriaApp.getAPI().getFriendRequestsReceived(user.getId()));
                    receivedFriendRequests = Transformations.switchMap(requests, sentFriendRequests -> {
                        receivedRequestsVisible.setValue(sentFriendRequests != null && sentFriendRequests.size() > 0);

                        return new SingleLiveData<>(sentFriendRequests);
                    });

                    return receivedFriendRequests;
                }

                return new SingleLiveData<>(null);
            });
        }

        return receivedFriendRequests;
    }

    LiveData<Boolean> addFriend(@NonNull String newFriendEmail) {
        if (Strings.isValidEmail(newFriendEmail)) {

            IsegoriaApp isegoriaApp = getApplication();

            return Transformations.switchMap(isegoriaApp.loggedInUser, user -> {
                LiveData<Void> friendRequest = new RetrofitLiveData<>(isegoriaApp.getAPI().addFriend(user.email, newFriendEmail));
                return Transformations.switchMap(friendRequest, __ -> new SingleLiveData<>(true));
            });
        }

        return new SingleLiveData<>(false);
    }

    LiveData<Boolean> acceptFriendRequest(long requestId) {
        IsegoriaApp isegoriaApp = getApplication();

        LiveData<Void> friendRequest = new RetrofitLiveData<>(isegoriaApp.getAPI().acceptFriendRequest(requestId));
        return Transformations.switchMap(friendRequest, __ -> {
            getReceivedFriendRequests();
            getFriends();

            return new SingleLiveData<>(true);
        });
    }

    LiveData<Boolean> rejectFriendRequest(long requestId) {
        IsegoriaApp isegoriaApp = getApplication();

        LiveData<Void> friendRequest = new RetrofitLiveData<>(isegoriaApp.getAPI().rejectFriendRequest(requestId));
        return Transformations.switchMap(friendRequest, __ -> {
            getReceivedFriendRequests();
            getFriends();

            return new SingleLiveData<>(true);
        });
    }

    LiveData<Institution> getInstitution(long institutionId) {
        IsegoriaApp isegoriaApp = getApplication();

        return new RetrofitLiveData<>(isegoriaApp.getAPI().getInstitution(institutionId));
    }

}
