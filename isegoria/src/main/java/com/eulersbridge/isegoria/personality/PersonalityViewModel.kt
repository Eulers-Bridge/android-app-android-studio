package com.eulersbridge.isegoria.personality

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.eulersbridge.isegoria.data.Repository
import com.eulersbridge.isegoria.network.api.model.UserPersonality
import io.reactivex.rxkotlin.subscribeBy
import javax.inject.Inject

class PersonalityViewModel
@Inject constructor(private val repository: Repository) : ViewModel() {

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
        )
    }
}
