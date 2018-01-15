package com.eulersbridge.isegoria.vote;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.support.annotation.NonNull;

import com.eulersbridge.isegoria.IsegoriaApp;
import com.eulersbridge.isegoria.network.api.models.Election;
import com.eulersbridge.isegoria.network.api.models.User;
import com.eulersbridge.isegoria.network.api.models.VoteLocation;
import com.eulersbridge.isegoria.network.api.models.VoteReminder;
import com.eulersbridge.isegoria.util.data.RetrofitLiveData;
import com.eulersbridge.isegoria.util.data.FixedData;

import java.util.Calendar;
import java.util.List;

@SuppressWarnings("WeakerAccess")
public class VoteViewModel extends AndroidViewModel {

    private LiveData<List<VoteLocation>> voteLocations;
    private LiveData<Election> election;

    private final MutableLiveData<Integer> selectedVoteLocationIndex = new MutableLiveData<>();
    private final LiveData<VoteLocation> selectedVoteLocation = Transformations.switchMap(selectedVoteLocationIndex,
            index -> {
            if (voteLocations != null && voteLocations.getValue() != null && index >= 0) {
                return new FixedData<>(voteLocations.getValue().get(index));
            }

            return new FixedData<>(null);
        });
    final MutableLiveData<Calendar> dateTime = new MutableLiveData<>();

    final MediatorLiveData<Boolean> locationAndDateComplete = new MediatorLiveData<>();
    final MutableLiveData<Boolean> pledgeComplete = new MutableLiveData<>();

    public VoteViewModel(@NonNull Application application) {
        super(application);

        selectedVoteLocationIndex.setValue(0);

        locationAndDateComplete.addSource(selectedVoteLocation, location -> {
            final boolean complete = location != null && dateTime.getValue() != null;
            locationAndDateComplete.setValue(complete);
        });

        locationAndDateComplete.addSource(dateTime, newDateTime -> {
            final boolean complete = newDateTime != null && selectedVoteLocation.getValue() != null;
            locationAndDateComplete.setValue(complete);
        });
    }

    void onVoteLocationChanged(int newIndex) {
        selectedVoteLocationIndex.setValue(newIndex);
    }

    LiveData<Election> getElection() {
        if (election != null)
            return election;

        IsegoriaApp isegoriaApp = getApplication();
        User user = isegoriaApp.getLoggedInUser();

        if (user != null && user.institutionId != null) {
            LiveData<List<Election>> electionsList = new RetrofitLiveData<>(isegoriaApp.getAPI().getElections(user.institutionId));

            election = Transformations.switchMap(electionsList, elections -> {
                if (elections != null && elections.size() > 0) {
                    Election election = elections.get(0);

                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(election.startVotingTimestamp);
                    dateTime.setValue(calendar);

                    return new FixedData<>(election);

                } else {
                    return new FixedData<>(null);
                }
            });

            return election;
        }

        return new FixedData<>(null);
    }

    LiveData<List<VoteLocation>> getVoteLocations() {
        if (voteLocations != null)
            return voteLocations;

        IsegoriaApp isegoriaApp = getApplication();
        User user = isegoriaApp.getLoggedInUser();

        if (user != null && user.institutionId != null) {
            voteLocations = new RetrofitLiveData<>(isegoriaApp.getAPI().getVoteLocations(user.institutionId));
            return voteLocations;
        }

        return new FixedData<>(null);
    }

    LiveData<Boolean> setPledgeComplete() {
        if (pledgeComplete.getValue() != null && pledgeComplete.getValue()) {
            return pledgeComplete;
        }

        IsegoriaApp isegoriaApp = getApplication();
        User user = isegoriaApp.getLoggedInUser();

        final Election election = this.election.getValue();
        final VoteLocation voteLocation = selectedVoteLocation.getValue();
        final Calendar dateTimeCalendar = dateTime.getValue();

        if (election != null && voteLocation != null && dateTimeCalendar != null) {
            // Create a reminder
            VoteReminder reminder = new VoteReminder(user.email, election.id, voteLocation.name,
                    dateTimeCalendar.getTimeInMillis());

            // Add the vote reminder
            LiveData<Void> reminderRequest = new RetrofitLiveData<>(isegoriaApp.getAPI().addVoteReminder(user.email, reminder));

            return Transformations.switchMap(reminderRequest, __ -> {
                pledgeComplete.setValue(true);
                return new FixedData<>(true);
            });
        }

        return new FixedData<>(false);
    }
}
