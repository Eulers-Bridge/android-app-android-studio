package com.eulersbridge.isegoria.feed.events;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Transformations;
import android.support.annotation.NonNull;

import com.eulersbridge.isegoria.IsegoriaApp;
import com.eulersbridge.isegoria.network.api.models.Event;
import com.eulersbridge.isegoria.util.data.SingleLiveData;
import com.eulersbridge.isegoria.util.data.RetrofitLiveData;

import java.util.List;

@SuppressWarnings("WeakerAccess")
public class EventsViewModel extends AndroidViewModel {

    private LiveData<List<Event>> eventsList;

    public EventsViewModel(@NonNull Application application) {
        super(application);
    }

    LiveData<List<Event>> getEvents() {
        IsegoriaApp isegoriaApp = getApplication();

        return Transformations.switchMap(isegoriaApp.loggedInUser, user -> {
            if (user != null && user.institutionId != null) {
                eventsList = new RetrofitLiveData<>(isegoriaApp.getAPI().getEvents(user.institutionId));
                return eventsList;
            }

            return new SingleLiveData<>(null);
        });
    }

    @Override
    protected void onCleared() {
        if (eventsList instanceof RetrofitLiveData)
            ((RetrofitLiveData) eventsList).cancel();
    }
}
