package com.eulersbridge.isegoria.feed.events

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Transformations
import android.arch.lifecycle.ViewModel
import com.eulersbridge.isegoria.network.api.API
import com.eulersbridge.isegoria.network.api.models.Event
import com.eulersbridge.isegoria.network.api.models.User
import com.eulersbridge.isegoria.toLiveData
import com.eulersbridge.isegoria.util.data.SingleLiveData
import javax.inject.Inject

class EventsViewModel
@Inject constructor(
    private val user: LiveData<User>,
    private val api: API
) : ViewModel() {

    fun getEvents() : LiveData<List<Event>?> {
        return Transformations.switchMap<User, List<Event>>(user) { user ->

            user.institutionId?.let {
                api.getEvents(user.institutionId!!).toLiveData()

            } ?: SingleLiveData<List<Event>?>(null)
        }
    }
}
