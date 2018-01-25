package com.eulersbridge.isegoria.feed.events;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Intent;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.eulersbridge.isegoria.IsegoriaApp;
import com.eulersbridge.isegoria.network.api.models.Event;
import com.eulersbridge.isegoria.network.api.models.Position;
import com.eulersbridge.isegoria.network.api.models.Ticket;
import com.eulersbridge.isegoria.util.data.RetrofitLiveData;

@SuppressWarnings("WeakerAccess")
public class EventDetailViewModel extends AndroidViewModel {

    private final MutableLiveData<Event> eventData = new MutableLiveData<>();

    public EventDetailViewModel(@NonNull Application application) {
        super(application);
    }

    void setEvent(Event event) {
        this.eventData.setValue(event);
    }

    @Nullable Intent getAddToCalendarIntent() {
        Event event = eventData.getValue();
        if (event == null)
            return null;

        return new Intent(Intent.ACTION_INSERT)
                .setData(CalendarContract.Events.CONTENT_URI)
                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, event.date)

                // Make event 1 hour long (add an hour in in milliseconds to event start)
                .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, event.date + 60 * 60 * 1000)
                .putExtra(CalendarContract.Events.ALL_DAY, false)

                .putExtra(CalendarContract.Events.TITLE, event.name)
                .putExtra(CalendarContract.Events.DESCRIPTION, event.description)
                .putExtra(CalendarContract.Events.EVENT_LOCATION, event.location)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }

    LiveData<Ticket> getTicket(long ticketId) {
        IsegoriaApp app = getApplication();

        return new RetrofitLiveData<>(app.getAPI().getTicket(ticketId));
    }

    LiveData<Position> getPosition(long positionId) {
        IsegoriaApp app = getApplication();

        return new RetrofitLiveData<>(app.getAPI().getPosition(positionId));
    }

}
