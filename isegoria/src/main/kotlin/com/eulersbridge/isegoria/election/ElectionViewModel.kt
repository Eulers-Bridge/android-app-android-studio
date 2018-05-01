package com.eulersbridge.isegoria.election

import android.arch.lifecycle.MutableLiveData
import com.eulersbridge.isegoria.data.Repository
import com.eulersbridge.isegoria.network.api.model.Election
import com.eulersbridge.isegoria.util.BaseViewModel
import com.eulersbridge.isegoria.util.data.SingleLiveEvent
import com.eulersbridge.isegoria.util.extension.subscribeSuccess
import javax.inject.Inject

class ElectionViewModel @Inject constructor(repository: Repository) : BaseViewModel() {

    internal val surveyPromptVisible = MutableLiveData<Boolean>()
    internal val surveyVisible = SingleLiveEvent<Any>()
    internal val election = MutableLiveData<Election?>()

    init {
        // If the user has not completed the questions, show a prompt for them
        surveyPromptVisible.value = !repository.getUser().hasPPSEQuestions

        repository.getLatestElection()
                .subscribeSuccess {
                    election.postValue(it.value)
                }
                .addToDisposable()
    }

    internal fun onSurveyPromptNext() {
        surveyVisible.call()
        surveyPromptVisible.value = false
    }
}
