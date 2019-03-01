package com.eulersbridge.isegoria.vote

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.content.Intent
import android.provider.CalendarContract
import com.eulersbridge.isegoria.data.Repository
import com.eulersbridge.isegoria.network.api.model.Election
import com.eulersbridge.isegoria.network.api.model.VoteLocation
import com.eulersbridge.isegoria.network.api.model.VoteReminder
import com.eulersbridge.isegoria.util.BaseViewModel
import com.eulersbridge.isegoria.util.data.Optional
import com.eulersbridge.isegoria.util.extension.map
import com.eulersbridge.isegoria.util.extension.toBooleanSingle
import com.eulersbridge.isegoria.util.extension.toLiveData
import io.reactivex.BackpressureStrategy
import io.reactivex.rxkotlin.Observables
import io.reactivex.subjects.BehaviorSubject
import java.util.*
import javax.inject.Inject

class VoteViewModel @Inject constructor(private val repository: Repository) : BaseViewModel() {

    enum class PageIndex(val value: Int) {
        INTIAL(0),
        PLEDGE(1),
        DONE(2)
    }

    // rx subjects
    private val selectedDateTimeSubject = BehaviorSubject.createDefault(Optional<Calendar>())
    private val selectedVoteLocationIndexSubject = BehaviorSubject.createDefault(0)
    private val selectedVoteLocationSubject = selectedVoteLocationIndexSubject.map {
        Optional(  voteLocationsSubject.value?.getOrNull(it))
    }
    private val voteLocationsSubject = BehaviorSubject.createDefault<List<VoteLocation>>(listOf())
    private val voteReminderSubject = BehaviorSubject.create<VoteReminder?>()
    private val pledgeCompleteSubject = BehaviorSubject.create<Boolean>()
    private val viewPageIndexSubject = BehaviorSubject.createDefault(PageIndex.INTIAL)
    private val electionSubject = BehaviorSubject.create<Election?>()

    // live data
    internal val voteLocations = voteLocationsSubject.toLiveData(BackpressureStrategy.LATEST)
    internal var election = electionSubject.toLiveData(BackpressureStrategy.LATEST)

    private val pledgeComplete = pledgeCompleteSubject.toLiveData(BackpressureStrategy.LATEST)

    private val latestVoteReminder = voteReminderSubject.toLiveData(BackpressureStrategy.LATEST)

    internal val viewPagerIndex = MutableLiveData<PageIndex>()

    init {
//        Observables.combineLatest(selectedVoteLocationSubject, dateTimeSubject) { location, dateTime ->
//            location.value != null && dateTime.value != null
//        }.subscribe {
//            if (it && pledgeComplete.value == false)
//                viewPageIndexSubject.onNext(PageIndex.PLEDGE)
//        }.addToDisposable()

        //create observers
        observeVoteReminderSubject()

        // refresh values
        refreshElection()
        refreshVoteLocations()
        refreshVoteReminder()
    }

    fun getAddReminderToCalendarIntent(): Intent? {
        val (_, _, location, date) = latestVoteReminder.value ?: return null

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

    internal fun onVoteLocationChanged(newIndex: Int) {
        selectedVoteLocationIndexSubject.onNext(newIndex)
    }

    internal fun onDateAndTimeChanged(calendar: Calendar) {
        selectedDateTimeSubject.onNext(Optional(calendar))
    }

    internal fun onInitialPageComplete() {
        Observables.combineLatest(selectedVoteLocationSubject, selectedDateTimeSubject) { location, dateTime ->
                location.value != null && dateTime.value != null
        }.subscribe {pageComplete ->
                if (pageComplete)
                        viewPageIndexSubject.onNext(PageIndex.PLEDGE)
        }.addToDisposable()
    }

    internal fun onVotePledgeComplete() {
        //Checks if a pledge has already been filled out before
        if (pledgeComplete.value != null && pledgeComplete.value == true)
            return

        val election = this.electionData?.value
        val voteLocation = selectedVoteLocation.blockingFirst().value
        val dateTimeCalendar = dateTime.value?.value

        if (election != null && voteLocation != null && dateTimeCalendar != null) {
            repository.createUserVoteReminder(election.id, voteLocation.name!!, dateTimeCalendar.timeInMillis)
                    .toBooleanSingle()
                    .subscribe()
                    .addToDisposable()

            viewPagerIndex.value = 2
        }

        refreshVoteReminder()
    }





//        if (electionData != null)
//            return electionData!!
//
//
//
//
//
//        electionData = repository.getLatestElection().map {
//            if (it.value != null) {
//                val calendar = Calendar.getInstance()
//
//                calendar.timeInMillis = it.value.startVoting
//
//                dateTime.onNext(Optional(calendar))
//            }
//
//            it
//        }.toLiveData().map { it.value }
//
//        return electionData!!

    // observers

    private fun observeVoteReminderSubject() {
        voteReminderSubscribe.subscribe { viewPagerIndex.value = PageIndex.DONE }
    }


    // subject refreshers



    private fun refreshElection() {
        repository.getLatestElection().doOnSuccess {
            if (it.value != null)
                electionSubject.onNext(it.value)
        }
                .subscribe()
                .addToDisposable()
    }

    private fun refreshVoteLocations() {
        repository.getVoteLocations().doOnSuccess {
            voteLocationsSubject.onNext(it)
        }
                .subscribe()
                .addToDisposable()
    }

    internal fun refreshVoteReminder() {
        repository.getLatestUserVoteReminder()
                .doOnSuccess {
                    voteReminder.onNext(it)
                }
                .subscribe()
    }
}
