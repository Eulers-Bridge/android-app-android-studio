package com.eulersbridge.isegoria.feed.events.detail

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import com.eulersbridge.isegoria.data.Repository
import com.eulersbridge.isegoria.network.api.model.Event
import com.eulersbridge.isegoria.network.api.model.Position
import com.eulersbridge.isegoria.network.api.model.Ticket
import com.eulersbridge.isegoria.util.extension.map
import com.eulersbridge.isegoria.util.extension.toLiveData
import javax.inject.Inject

class EventDetailViewModel
@Inject constructor (private val repository: Repository) : ViewModel() {

    internal val event = MutableLiveData<Event>()

    // Make event 1 hour long (add an hour in in milliseconds to event start)
    private val addToCalendarIntent: Intent?
        get() {
            val event = event.value ?: return null

            return Intent(Intent.ACTION_INSERT)
                    .setData(CalendarContract.Events.CONTENT_URI)
                    .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, event.createdDate)
                    .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, event.createdDate + 60 * 60 * 1000)
                    .putExtra(CalendarContract.Events.ALL_DAY, false)

                    .putExtra(CalendarContract.Events.TITLE, event.name)
                    .putExtra(CalendarContract.Events.DESCRIPTION, event.description)
                    .putExtra(CalendarContract.Events.EVENT_LOCATION, event.location)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

    internal fun getTicket(ticketId: Long): LiveData<Ticket?>
            = repository.getTicket(ticketId)
                .toLiveData()
                .map { it.value }

    internal fun getPosition(positionId: Long): LiveData<Position?>
            = repository.getPosition(positionId)
                .toLiveData()
                .map { it.value }

    internal fun addToCalendar(context: Context) {
        addToCalendarIntent?.let {
            if (it.resolveActivity(context.packageManager) != null)
                context.startActivity(it)
        }
    }
}
