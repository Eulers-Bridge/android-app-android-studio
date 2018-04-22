package com.eulersbridge.isegoria.feed.events

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.eulersbridge.isegoria.data.Repository
import com.eulersbridge.isegoria.network.api.model.Event
import com.eulersbridge.isegoria.util.extension.subscribeSuccess
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import javax.inject.Inject

class EventsViewModel
@Inject constructor(
        private val repository: Repository
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()
    internal val events = MutableLiveData<List<Event>>()

    init {
        fetchEvents()
    }

    override fun onCleared() {
        compositeDisposable.dispose()
    }

    internal fun onRefresh() {
        fetchEvents()
    }

    private fun fetchEvents() {
        repository.getEvents().subscribeSuccess {
            events.postValue(it)
        }.addTo(compositeDisposable)
    }
}
