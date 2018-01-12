package com.eulersbridge.isegoria.feed.events;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import com.eulersbridge.isegoria.IsegoriaApp;
import com.eulersbridge.isegoria.network.api.models.Event;
import com.eulersbridge.isegoria.network.api.models.User;
import com.eulersbridge.isegoria.util.data.RetrofitLiveData;
import com.eulersbridge.isegoria.util.data.FixedData;

import java.util.List;

@SuppressWarnings("WeakerAccess")
public class EventsViewModel extends AndroidViewModel {

    private LiveData<List<Event>> eventsList;

    public EventsViewModel(@NonNull Application application) {
        super(application);
    }

    LiveData<List<Event>> getEvents() {
        IsegoriaApp isegoriaApp = getApplication();

        User user = isegoriaApp.getLoggedInUser();

        if (user != null && user.institutionId != null) {
            eventsList = new RetrofitLiveData<>(isegoriaApp.getAPI().getEvents(user.institutionId));
        } else {
            eventsList = new FixedData<>(null);
        }

        return eventsList;
    }

    @Override
    protected void onCleared() {
        if (eventsList instanceof RetrofitLiveData)
            ((RetrofitLiveData) eventsList).cancel();
    }
}
