package com.eulersbridge.isegoria.vote

import android.content.Intent
import android.provider.CalendarContract
import com.eulersbridge.isegoria.data.Repository
import com.eulersbridge.isegoria.network.api.model.Election
import com.eulersbridge.isegoria.network.api.model.VoteLocation
import com.eulersbridge.isegoria.network.api.model.VoteReminder
import com.eulersbridge.isegoria.util.BaseViewModel
import com.eulersbridge.isegoria.util.data.SingleLiveEvent
import com.eulersbridge.isegoria.util.extension.toBooleanSingle
import com.eulersbridge.isegoria.util.extension.toLiveData
import io.reactivex.BackpressureStrategy
import io.reactivex.subjects.BehaviorSubject
import java.util.*
import javax.inject.Inject

class VoteViewModel @Inject constructor(private val repository: Repository) : BaseViewModel() {

    enum class PageIndex(val value: Int) {
        INITIAL(0),
        PLEDGE(1),
        DONE(2)
    }

    // rx subjects
    private val selectedDateTimeSubject = BehaviorSubject.create<Calendar>()
    private val selectedVoteLocationIndexSubject = BehaviorSubject.createDefault(0)

    private val voteLocationsSubject = BehaviorSubject.createDefault<List<VoteLocation>>(listOf())
    private val voteReminderSubject = BehaviorSubject.create<VoteReminder?>()
    private val pledgeCompleteSubject = BehaviorSubject.create<Boolean>()
    private val pageIndexSubject = BehaviorSubject.createDefault(PageIndex.INITIAL)
    private val electionSubject = BehaviorSubject.create<Election?>()

    // live data
    internal val voteLocations = voteLocationsSubject.toLiveData(BackpressureStrategy.LATEST)
    internal var election = electionSubject.toLiveData(BackpressureStrategy.LATEST)

    internal val pageIndex = pageIndexSubject.toLiveData(BackpressureStrategy.LATEST)

    internal val toastMessage =  SingleLiveEvent<String>()

    init {
        //create observers
        observeVoteReminderSubject()
        observeElectionSubject()

        // refresh values
        refreshElection()
        refreshVoteLocations()
        refreshVoteReminder()
    }

    fun getAddReminderToCalendarIntent(): Intent? {
        val (_, _, location, date) = voteReminderSubject.value ?: return null

        return Intent(Intent.ACTION_INSERT)
                .setData(CalendarContract.Events.CONTENT_URI)
                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, date)
                // Make event 1 hour long (add an hour in in milliseconds to start)
                .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, date + 60 * 60 * 1000)
                .putExtra(CalendarContract.Events.ALL_DAY, false)
                .putExtra(CalendarContract.Events.TITLE, "Voting for Candidate")
                .putExtra(CalendarContract.Events.DESCRIPTION, location)
                .putExtra(CalendarContract.Events.EVENT_LOCATION, location)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    internal fun getDateTime(): Calendar? {
        return selectedDateTimeSubject.value
    }

    internal fun onVoteLocationChanged(newIndex: Int) {
        selectedVoteLocationIndexSubject.onNext(newIndex)
    }

    internal fun onDateTimeChanged(calendar: Calendar) {
        selectedDateTimeSubject.onNext(calendar)
    }

    internal fun onInitialPageComplete() {
        if (selectedVoteLocationIndexSubject.hasValue() && selectedDateTimeSubject.hasValue()) {
            pageIndexSubject.onNext(PageIndex.PLEDGE)
        } else {
            toastMessage.postValue("Select a location and time")
        }
    }

    internal fun onVotePledgeComplete() {
        //Checks if a pledge has already been filled out before
        if (pledgeCompleteSubject.value != null && pledgeCompleteSubject.value == true) {
            toastMessage.postValue("Unexpected error")
            return
        }


        // Checks voteLocations have been fetched from API
        if (!voteLocationsSubject.hasValue()) {
            refreshVoteLocations()
            toastMessage.postValue("Error occurred, please try again soon")
            return
        }

        // Checks inputs have been selected
        if (!(selectedVoteLocationIndexSubject.hasValue() &&  selectedDateTimeSubject.hasValue())){
            toastMessage.postValue("Unexpected error")
            return
        }

        val currentElection = electionSubject.value
        val selectedVoteLocation =   voteLocationsSubject.value!!.getOrNull(selectedVoteLocationIndexSubject.value!!)
        val dateTimeCalendar = selectedDateTimeSubject.value

        if (currentElection != null && selectedVoteLocation != null && dateTimeCalendar != null) {
            repository.createUserVoteReminder(currentElection.id, selectedVoteLocation.name!!, dateTimeCalendar.timeInMillis)
                    .toBooleanSingle()
                    .doOnSuccess {success ->
                        if (success) {
                            pageIndexSubject.onNext(PageIndex.DONE)

                            refreshVoteReminder()
                        } else {
                            toastMessage.postValue("Unexpected error")
                        }
                    }
                    .doOnError {
                        toastMessage.postValue("Unexpected error")
                    }
                    .subscribe()
                    .addToDisposable()
        }
    }

    // observers

    private fun observeVoteReminderSubject() {
        voteReminderSubject.subscribe { pageIndexSubject.onNext(PageIndex.DONE) }.addToDisposable()
    }

    private fun observeElectionSubject() {
        electionSubject
                .subscribe { election ->
                    if (election != null) {
                        val defaultVotingTime = Calendar.getInstance()
                        defaultVotingTime.timeInMillis = election.startVoting
                        selectedDateTimeSubject.onNext(defaultVotingTime)
                    }
                }
                .addToDisposable()
    }

    // subject refreshers

    private fun refreshElection() {
        repository.getLatestElection()
                .doOnSuccess {
                    if (it.value != null)
                        electionSubject.onNext(it.value)
                }
                .doOnError {
                    toastMessage.postValue("Unexpected error")
                }
                .subscribe()
                .addToDisposable()
    }

    private fun refreshVoteLocations() {
        repository.getVoteLocations()
                .doOnSuccess {
                    voteLocationsSubject.onNext(it)
                }
                .doOnError {
                    toastMessage.postValue("Unexpected error")
                }
                .subscribe()
                .addToDisposable()
    }

    private fun refreshVoteReminder() {
        repository.getLatestUserVoteReminder()
                .doOnSuccess {
                    if (it.value != null)
                        voteReminderSubject.onNext(it.value)
                }
                .doOnError {
                    toastMessage.postValue("Unexpected error")
                }
                .subscribe()
                .addToDisposable()
    }
}
