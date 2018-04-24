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
import io.reactivex.rxkotlin.Observables
import io.reactivex.subjects.BehaviorSubject
import java.util.*
import javax.inject.Inject

class VoteViewModel @Inject constructor(private val repository: Repository) : BaseViewModel() {

    private var voteLocations: LiveData<List<VoteLocation>>? = null
    internal var electionData: LiveData<Election?>? = null
    private val selectedVoteLocationIndex = BehaviorSubject.createDefault(0)
    private val selectedVoteLocation = selectedVoteLocationIndex.map { Optional(voteLocations?.value?.get(it)) }
    private val dateTime = BehaviorSubject.createDefault(Optional<Calendar>())

    private val pledgeComplete = MutableLiveData<Boolean>()

    private val latestVoteReminder = MutableLiveData<VoteReminder>()

    internal val viewPagerIndex = MutableLiveData<Int>()

    init {
        viewPagerIndex.value = 0

        Observables.combineLatest(selectedVoteLocation, dateTime) { location, dateTime ->
            location.value != null && dateTime.value != null
        }.subscribe {
            if (it && pledgeComplete.value == false)
                viewPagerIndex.postValue(1)
        }.addToDisposable()
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
        selectedVoteLocationIndex.onNext(newIndex)
    }

    internal fun setDateTime(calendar: Calendar) {
        dateTime.onNext(Optional(calendar))
    }

    internal fun getDateTime(): Calendar? {
        return dateTime.value?.value
    }

    internal fun onVoteComplete() {
        viewPagerIndex.value = 1
    }

    internal fun getElection(): LiveData<Election?> {
        if (electionData != null)
            return electionData!!

        electionData = repository.getLatestElection().map {
            if (it.value != null) {
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = it.value.startVoting
                dateTime.onNext(Optional(calendar))
            }

            it
        }.toLiveData().map { it.value }

        return electionData!!
    }

    internal fun getVoteLocations(): LiveData<List<VoteLocation>> {
        return repository.getVoteLocations().toLiveData()
    }

    internal fun setPledgeComplete() {
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
    }

    internal fun getLatestVoteReminder(): LiveData<Boolean> {
        return repository.getUserVoteReminderExists().toLiveData()
    }
}
