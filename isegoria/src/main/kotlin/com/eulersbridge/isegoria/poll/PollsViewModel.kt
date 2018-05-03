package com.eulersbridge.isegoria.poll

import android.arch.lifecycle.MutableLiveData
import com.eulersbridge.isegoria.data.Repository
import com.eulersbridge.isegoria.network.api.model.Poll
import com.eulersbridge.isegoria.util.BaseViewModel
import com.eulersbridge.isegoria.util.extension.subscribeSuccess
import javax.inject.Inject

class PollsViewModel @Inject constructor(private val repository: Repository) : BaseViewModel() {

    internal val polls = MutableLiveData<List<Poll>>()

    init {
        fetchPolls()
    }

    private fun fetchPolls() {
        repository.getPolls()
                .subscribeSuccess {
                    polls.postValue(it)
                }
                .addToDisposable()
    }
}
