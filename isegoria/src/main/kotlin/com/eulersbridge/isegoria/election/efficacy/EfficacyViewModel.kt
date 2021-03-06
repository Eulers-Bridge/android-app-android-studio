package com.eulersbridge.isegoria.election.efficacy

import android.arch.lifecycle.MutableLiveData
import com.eulersbridge.isegoria.data.Repository
import com.eulersbridge.isegoria.network.api.model.UserSelfEfficacy
import com.eulersbridge.isegoria.util.BaseViewModel
import com.eulersbridge.isegoria.util.data.SingleLiveEvent
import io.reactivex.Completable
import io.reactivex.rxkotlin.subscribeBy
import javax.inject.Inject

class EfficacyViewModel @Inject constructor(private val repository: Repository) : BaseViewModel() {

    internal val doneButtonEnabled = MutableLiveData<Boolean>()
    internal val efficacyComplete = SingleLiveEvent<Any>()

    internal val score1 = MutableLiveData<Int>()
    internal val score2 = MutableLiveData<Int>()
    internal val score3 = MutableLiveData<Int>()
    internal val score4 = MutableLiveData<Int>()

    init {
        doneButtonEnabled.value = true
    }

    internal fun onDone() {
        doneButtonEnabled.value = false

        addUserEfficacy()
                .subscribeBy(
                        onComplete = {
                            efficacyComplete.call()
                        },
                        onError = {
                            doneButtonEnabled.postValue(true)
                        }
                )
                .addToDisposable()
    }

    private fun addUserEfficacy(): Completable {
        val scores = listOfNotNull(score1.value, score2.value, score3.value, score4.value)
            .map { it.toFloat() }

        return if (scores.isEmpty()) {
            Completable.error(Exception("No scores"))

        } else {
            val answers = UserSelfEfficacy(scores[0], scores[1], scores[2], scores[3])
            repository.addUserSelfEfficacyAnswers(answers)
        }
    }

}
