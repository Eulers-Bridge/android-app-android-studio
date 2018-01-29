package com.eulersbridge.isegoria.vote

import android.app.Application
import android.arch.lifecycle.*
import android.content.Intent
import android.provider.CalendarContract
import com.eulersbridge.isegoria.IsegoriaApp
import com.eulersbridge.isegoria.network.api.models.Election
import com.eulersbridge.isegoria.network.api.models.VoteLocation
import com.eulersbridge.isegoria.network.api.models.VoteReminder
import com.eulersbridge.isegoria.util.data.RetrofitLiveData
import com.eulersbridge.isegoria.util.data.SingleLiveData
import java.util.*

class VoteViewModel(application: Application) : AndroidViewModel(application) {

    private var voteLocations: LiveData<List<VoteLocation>?>? = null
    internal var electionData: LiveData<Election?>? = null

    private val selectedVoteLocationIndex = MutableLiveData<Int>()

    private val selectedVoteLocation =
        Transformations.switchMap(selectedVoteLocationIndex) { index ->
            SingleLiveData(voteLocations?.value?.get(index))
        }

    internal val dateTime = MutableLiveData<Calendar>()

    internal val locationAndDateComplete = MediatorLiveData<Boolean>()
    internal val pledgeComplete = MutableLiveData<Boolean>()

    private val latestVoteReminder = MutableLiveData<VoteReminder>()

    internal// Make event 1 hour long (add an hour in in milliseconds to start)
    val addVoteReminderToCalendarIntent: Intent?
        get() {
            val (_, _, location, date) = latestVoteReminder.value ?: return null

            return Intent(Intent.ACTION_INSERT)
                .setData(CalendarContract.Events.CONTENT_URI)
                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, date)
                .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, date + 60 * 60 * 1000)
                .putExtra(CalendarContract.Events.ALL_DAY, false)
                .putExtra(CalendarContract.Events.TITLE, "Voting for Candidate")
                .putExtra(CalendarContract.Events.DESCRIPTION, location)
                .putExtra(CalendarContract.Events.EVENT_LOCATION, location)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

    init {
        selectedVoteLocationIndex.value = 0

        locationAndDateComplete.addSource(selectedVoteLocation) { location ->
            val complete = location != null && dateTime.value != null
            locationAndDateComplete.value = complete
        }

        locationAndDateComplete.addSource(dateTime) { newDateTime ->
            val complete = newDateTime != null && selectedVoteLocation.value != null
            locationAndDateComplete.value = complete
        }
    }

    internal fun onVoteLocationChanged(newIndex: Int) {
        selectedVoteLocationIndex.value = newIndex
    }

    internal fun getElection(): LiveData<Election?> {
        if (electionData != null)
            return electionData!!

        val app = getApplication<IsegoriaApp>()
        val user = app.loggedInUser.value

        if (user?.institutionId != null) {
            val electionsList = RetrofitLiveData(app.api.getElections(user.institutionId!!))

            electionData = Transformations.switchMap(electionsList) { elections ->
                if (elections != null && elections.isNotEmpty()) {

                    val election = elections[0]

                    val calendar = Calendar.getInstance()
                    calendar.timeInMillis = election.startVoting
                    dateTime.value = calendar

                    SingleLiveData(election)
                }

                SingleLiveData<Election?>(null)
            }

            electionData
        }

        return SingleLiveData(null)
    }

    internal fun getVoteLocations(): LiveData<List<VoteLocation>?> {
        if (voteLocations != null)
            voteLocations

        val app = getApplication<IsegoriaApp>()
        val user = app.loggedInUser.value

        if (user?.institutionId != null) {
            voteLocations = RetrofitLiveData(app.api.getVoteLocations(user.institutionId!!))
            voteLocations
        }

        return SingleLiveData(null)
    }

    internal fun setPledgeComplete(): LiveData<Boolean> {
        if (pledgeComplete.value != null && pledgeComplete.value!!)
            return pledgeComplete

        val app = getApplication<IsegoriaApp>()

        app.loggedInUser.value?.let { user ->
            val election = this.electionData!!.value
            val voteLocation = selectedVoteLocation.value
            val dateTimeCalendar = dateTime.value

            if (election != null && voteLocation != null && dateTimeCalendar != null) {
                // Create a reminder
                val reminder = VoteReminder(
                    user.email, election.id, voteLocation.name!!,
                    dateTimeCalendar.timeInMillis
                )

                // Add the vote reminder
                val reminderRequest =
                    RetrofitLiveData(app.api.addVoteReminder(user.email, reminder))

                return Transformations.switchMap(reminderRequest) {
                    pledgeComplete.value = true
                    SingleLiveData(true)
                }
            }
        }

        return SingleLiveData(false)
    }

    internal fun getLatestVoteReminder(): LiveData<Boolean> {
        val app = getApplication<IsegoriaApp>()

        app.loggedInUser.value?.let { user ->
            val remindersRequest = RetrofitLiveData(app.api.getVoteReminders(user.email))

            return Transformations.switchMap(remindersRequest) { reminders ->

                val latestReminder = reminders?.firstOrNull()

                if (latestReminder != null) {
                    latestVoteReminder.value = latestReminder
                    SingleLiveData(true)
                }

                SingleLiveData(false)
            }
        }

        return SingleLiveData(false)
    }
}
