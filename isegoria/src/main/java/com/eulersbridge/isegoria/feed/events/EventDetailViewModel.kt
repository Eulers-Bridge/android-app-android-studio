package com.eulersbridge.isegoria.feed.events

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.content.Intent
import android.provider.CalendarContract

import com.eulersbridge.isegoria.IsegoriaApp
import com.eulersbridge.isegoria.network.api.models.Event
import com.eulersbridge.isegoria.network.api.models.Position
import com.eulersbridge.isegoria.network.api.models.Ticket
import com.eulersbridge.isegoria.util.data.RetrofitLiveData

class EventDetailViewModel(application: Application) : AndroidViewModel(application) {

    internal val event = MutableLiveData<Event>()

    // Make event 1 hour long (add an hour in in milliseconds to event start)
    internal val addToCalendarIntent: Intent?
        get() {
            val event = event.value ?: return null

            return Intent(Intent.ACTION_INSERT)
                    .setData(CalendarContract.Events.CONTENT_URI)
                    .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, event.date)
                    .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, event.date + 60 * 60 * 1000)
                    .putExtra(CalendarContract.Events.ALL_DAY, false)

                    .putExtra(CalendarContract.Events.TITLE, event.name)
                    .putExtra(CalendarContract.Events.DESCRIPTION, event.description)
                    .putExtra(CalendarContract.Events.EVENT_LOCATION, event.location)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

    private val app: IsegoriaApp by lazy {
        getApplication<IsegoriaApp>()
    }

    internal fun getTicket(ticketId: Long): LiveData<Ticket>
            = RetrofitLiveData(app.api.getTicket(ticketId))

    internal fun getPosition(positionId: Long): LiveData<Position>
            = RetrofitLiveData(app.api.getPosition(positionId))

}
