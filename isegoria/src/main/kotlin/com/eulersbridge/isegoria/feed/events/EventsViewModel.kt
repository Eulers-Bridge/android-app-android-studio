package com.eulersbridge.isegoria.feed.events

import android.arch.lifecycle.MutableLiveData
import com.eulersbridge.isegoria.data.Repository
import com.eulersbridge.isegoria.network.api.model.Event
import com.eulersbridge.isegoria.util.BaseViewModel
import com.eulersbridge.isegoria.util.extension.subscribeSuccess
import com.eulersbridge.isegoria.util.extension.zipWithTimer
import javax.inject.Inject

class EventsViewModel @Inject constructor(private val repository: Repository) : BaseViewModel() {

    internal var isRefreshing = MutableLiveData<Boolean>()
    internal val events = MutableLiveData<List<Event>>()

    init {
        isRefreshing.value = false
        refresh()
    }

    internal fun refresh() {
        isRefreshing.postValue(true)

        zipWithTimer(repository.getEvents())
                .subscribeSuccess {
                    isRefreshing.postValue(false)
                    events.postValue(it)
                }.addToDisposable()
    }
}
