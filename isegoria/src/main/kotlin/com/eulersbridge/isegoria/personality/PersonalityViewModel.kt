package com.eulersbridge.isegoria.personality

import android.arch.lifecycle.MutableLiveData
import com.eulersbridge.isegoria.data.Repository
import com.eulersbridge.isegoria.network.api.model.UserPersonality
import com.eulersbridge.isegoria.util.BaseViewModel
import io.reactivex.rxkotlin.subscribeBy
import javax.inject.Inject

class PersonalityViewModel @Inject constructor(private val repository: Repository) : BaseViewModel() {

    internal val doneButtonEnabled = MutableLiveData<Boolean>()
    internal val questionsContinued = MutableLiveData<Boolean>()
    internal val questionsComplete = MutableLiveData<Boolean>()

    init {
        doneButtonEnabled.value = true
    }

    internal fun setUserCompletedQuestions(userPersonality: UserPersonality) {
        doneButtonEnabled.value = false

        repository.addUserPersonality(userPersonality).subscribeBy(
                onComplete = {
                    questionsComplete.postValue(true)
                },
                onError = {

                }
        ).addToDisposable()
    }
}
