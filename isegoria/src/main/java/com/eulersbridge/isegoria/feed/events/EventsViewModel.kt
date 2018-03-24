package com.eulersbridge.isegoria.feed.events

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Transformations
import android.arch.lifecycle.ViewModel
import com.eulersbridge.isegoria.network.api.API
import com.eulersbridge.isegoria.network.api.models.Event
import com.eulersbridge.isegoria.network.api.models.User
import com.eulersbridge.isegoria.util.data.RetrofitLiveData
import com.eulersbridge.isegoria.util.data.SingleLiveData
import javax.inject.Inject

class EventsViewModel
@Inject constructor(
    private val user: LiveData<User>,
    private val api: API
) : ViewModel() {

    private var eventsList: LiveData<List<Event>?>? = null

    fun getEvents() : LiveData<List<Event>?> {
        return Transformations.switchMap<User, List<Event>>(user) { user ->
            if (user.institutionId != null) {
                eventsList = RetrofitLiveData(api.getEvents(user.institutionId!!))
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
