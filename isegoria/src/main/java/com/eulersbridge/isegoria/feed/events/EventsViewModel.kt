package com.eulersbridge.isegoria.feed.events

import android.arch.lifecycle.MutableLiveData
import com.eulersbridge.isegoria.data.Repository
import com.eulersbridge.isegoria.network.api.model.Event
import com.eulersbridge.isegoria.util.BaseViewModel
import com.eulersbridge.isegoria.util.extension.subscribeSuccess
import javax.inject.Inject

class EventsViewModel @Inject constructor(private val repository: Repository) : BaseViewModel() {

    internal val events = MutableLiveData<List<Event>>()

    init {
        fetchEvents()
    }

    internal fun onRefresh() {
        fetchEvents()
    }

    private fun fetchEvents() {
        repository.getEvents().subscribeSuccess {
            events.postValue(it)
        }.addToDisposable()
    }
}
