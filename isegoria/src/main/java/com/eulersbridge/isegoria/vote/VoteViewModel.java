package com.eulersbridge.isegoria.vote;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.content.Intent;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.eulersbridge.isegoria.IsegoriaApp;
import com.eulersbridge.isegoria.network.api.models.Election;
import com.eulersbridge.isegoria.network.api.models.User;
import com.eulersbridge.isegoria.network.api.models.VoteLocation;
import com.eulersbridge.isegoria.network.api.models.VoteReminder;
import com.eulersbridge.isegoria.util.data.SingleLiveData;
import com.eulersbridge.isegoria.util.data.RetrofitLiveData;

import java.util.Calendar;
import java.util.List;

@SuppressWarnings("WeakerAccess")
public class VoteViewModel extends AndroidViewModel {

    private LiveData<List<VoteLocation>> voteLocations;
    private LiveData<Election> election;

    private final MutableLiveData<Integer> selectedVoteLocationIndex = new MutableLiveData<>();

    private final LiveData<VoteLocation> selectedVoteLocation =
            Transformations.switchMap(selectedVoteLocationIndex, index -> {

                if (voteLocations != null && voteLocations.getValue() != null && index >= 0)
                    return new SingleLiveData<>(voteLocations.getValue().get(index));

            return new SingleLiveData<>(null);
        });

    final MutableLiveData<Calendar> dateTime = new MutableLiveData<>();

    final MediatorLiveData<Boolean> locationAndDateComplete = new MediatorLiveData<>();
    final MutableLiveData<Boolean> pledgeComplete = new MutableLiveData<>();

    private final MutableLiveData<VoteReminder> latestVoteReminder = new MutableLiveData<>();

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
        User user = isegoriaApp.loggedInUser.getValue();

        if (user != null && user.institutionId != null) {
            LiveData<List<Election>> electionsList = new RetrofitLiveData<>(isegoriaApp.getAPI().getElections(user.institutionId));

            election = Transformations.switchMap(electionsList, elections -> {
                if (elections != null && elections.size() > 0) {
                    Election election = elections.get(0);

                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(election.startVotingTimestamp);
                    dateTime.setValue(calendar);

                    return new SingleLiveData<>(election);

                } else {
                    return new SingleLiveData<>(null);
                }
            });

            return election;
        }

        return new SingleLiveData<>(null);
    }

    LiveData<List<VoteLocation>> getVoteLocations() {
        if (voteLocations != null)
            return voteLocations;

        IsegoriaApp isegoriaApp = getApplication();
        User user = isegoriaApp.loggedInUser.getValue();

        if (user != null && user.institutionId != null) {
            voteLocations = new RetrofitLiveData<>(isegoriaApp.getAPI().getVoteLocations(user.institutionId));
            return voteLocations;
        }

        return new SingleLiveData<>(null);
    }

    LiveData<Boolean> setPledgeComplete() {
        if (pledgeComplete.getValue() != null && pledgeComplete.getValue())
            return pledgeComplete;

        IsegoriaApp isegoriaApp = getApplication();
        User user = isegoriaApp.loggedInUser.getValue();

        if (user != null) {
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
                    return new SingleLiveData<>(true);
                });
            }
        }

        return new SingleLiveData<>(false);
    }

    LiveData<Boolean> getLatestVoteReminder() {
        IsegoriaApp isegoriaApp = getApplication();
        User user = isegoriaApp.loggedInUser.getValue();

        if (user != null) {

            LiveData<List<VoteReminder>> remindersRequest = new RetrofitLiveData<>(isegoriaApp.getAPI().getVoteReminders(user.email));

            return Transformations.switchMap(remindersRequest, reminders -> {
                if (reminders != null && reminders.size() > 0) {
                    latestVoteReminder.setValue(reminders.get(0));

                    return new SingleLiveData<>(true);
                }

                return new SingleLiveData<>(false);
            });
        }

        return new SingleLiveData<>(false);
    }

    @Nullable Intent getAddVoteReminderToCalendarIntent() {
        VoteReminder voteReminder = latestVoteReminder.getValue();
        if (voteReminder == null)
            return null;

        return new Intent(Intent.ACTION_INSERT)
                .setData(CalendarContract.Events.CONTENT_URI)
                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, voteReminder.date)

                // Make event 1 hour long (add an hour in in milliseconds to start)
                .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, voteReminder.date + 60 * 60 * 1000)
                .putExtra(CalendarContract.Events.ALL_DAY, false)

                .putExtra(CalendarContract.Events.TITLE, "Voting for Candidate")
                .putExtra(CalendarContract.Events.DESCRIPTION, voteReminder.location)
                .putExtra(CalendarContract.Events.EVENT_LOCATION, voteReminder.location)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }
}
