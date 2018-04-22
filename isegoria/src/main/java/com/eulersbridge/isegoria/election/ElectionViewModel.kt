package com.eulersbridge.isegoria.election

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.eulersbridge.isegoria.data.Repository
import com.eulersbridge.isegoria.network.api.model.Election
import com.eulersbridge.isegoria.util.extension.subscribeSuccess
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import javax.inject.Inject

class ElectionViewModel
@Inject constructor(
        private val repository: Repository
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()
    internal val election = MutableLiveData<Election?>()

    init {
        repository.getLatestElection().subscribeSuccess {
            election.postValue(it.value)
        }.addTo(compositeDisposable)
    }

    override fun onCleared() {
        compositeDisposable.dispose()
    }

    internal fun userCompletedEfficacyQuestions()
        = repository.getUser().hasPPSEQuestions
}
