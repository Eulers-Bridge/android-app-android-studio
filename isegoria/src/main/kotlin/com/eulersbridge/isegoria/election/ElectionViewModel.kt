package com.eulersbridge.isegoria.election

import android.arch.lifecycle.MutableLiveData
import com.eulersbridge.isegoria.data.Repository
import com.eulersbridge.isegoria.network.api.model.Election
import com.eulersbridge.isegoria.util.BaseViewModel
import com.eulersbridge.isegoria.util.data.SingleLiveEvent
import com.eulersbridge.isegoria.util.extension.subscribeSuccess
import com.eulersbridge.isegoria.util.extension.toLiveData
import io.reactivex.BackpressureStrategy
import io.reactivex.subjects.BehaviorSubject
import javax.inject.Inject

class ElectionViewModel @Inject constructor(repository: Repository) : BaseViewModel() {

    private val surveyPromptVisibleSubject = BehaviorSubject.createDefault<Boolean>(false)

    internal val surveyPromptVisible = surveyPromptVisibleSubject.toLiveData(BackpressureStrategy.LATEST)
    internal val surveyVisible = SingleLiveEvent<Any>()
    internal val election = MutableLiveData<Election?>()

    init {
        repository.getUser()
                .doOnSuccess { user ->
                    // If the user has not completed the questions, show a prompt for them
                      surveyPromptVisibleSubject.onNext(!user.hasPPSEQuestions)
                }
                .subscribe()
                .addToDisposable()


        repository.getLatestElection()
                .subscribeSuccess { election.postValue(it.value) }
                .addToDisposable()
    }

    internal fun onSurveyPromptNext() {
        surveyVisible.call()
        surveyPromptVisibleSubject.onNext(false)
    }
}
