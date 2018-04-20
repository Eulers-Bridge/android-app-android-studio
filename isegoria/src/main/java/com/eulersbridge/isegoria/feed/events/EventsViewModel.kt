package com.eulersbridge.isegoria.feed.events

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModel
import com.eulersbridge.isegoria.Repository
import com.eulersbridge.isegoria.network.api.model.Event
import com.eulersbridge.isegoria.util.extension.toLiveData
import javax.inject.Inject

class EventsViewModel
@Inject constructor(
        private val repository: Repository
) : ViewModel() {

    fun getEvents() : LiveData<List<Event>> {
        return repository.getEvents().toLiveData()
    }
}
