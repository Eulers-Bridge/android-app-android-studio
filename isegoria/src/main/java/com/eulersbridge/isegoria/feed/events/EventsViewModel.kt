package com.eulersbridge.isegoria.feed.events

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Transformations
import com.eulersbridge.isegoria.IsegoriaApp
import com.eulersbridge.isegoria.network.api.models.Event
import com.eulersbridge.isegoria.network.api.models.User
import com.eulersbridge.isegoria.util.data.RetrofitLiveData
import com.eulersbridge.isegoria.util.data.SingleLiveData

class EventsViewModel(application: Application) : AndroidViewModel(application) {

    private var eventsList: LiveData<List<Event>?>? = null

    fun getEvents() : LiveData<List<Event>?> {
        val app = getApplication<IsegoriaApp>()

        return Transformations.switchMap<User, List<Event>>(app.loggedInUser) { user ->
            if (user.institutionId != null) {
                eventsList = RetrofitLiveData(app.api.getEvents(user.institutionId!!))
                eventsList
            } else {
                SingleLiveData(null)
            }
        }
    }

    override fun onCleared() {
        (eventsList as? RetrofitLiveData)?.cancel()
    }
}
