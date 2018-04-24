package com.eulersbridge.isegoria.election

import android.arch.lifecycle.MutableLiveData
import com.eulersbridge.isegoria.data.Repository
import com.eulersbridge.isegoria.network.api.model.Election
import com.eulersbridge.isegoria.util.BaseViewModel
import com.eulersbridge.isegoria.util.extension.subscribeSuccess
import javax.inject.Inject

class ElectionViewModel @Inject constructor(private val repository: Repository) : BaseViewModel() {

    internal val election = MutableLiveData<Election?>()

    init {
        repository.getLatestElection().subscribeSuccess {
            election.postValue(it.value)
        }.addToDisposable()
    }

    internal fun userCompletedEfficacyQuestions()
        = repository.getUser().hasPPSEQuestions
}
