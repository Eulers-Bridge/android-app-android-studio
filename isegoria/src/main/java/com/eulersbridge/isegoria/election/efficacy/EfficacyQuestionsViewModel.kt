package com.eulersbridge.isegoria.election.efficacy

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import com.eulersbridge.isegoria.IsegoriaApp
import com.eulersbridge.isegoria.network.api.models.UserSelfEfficacy
import com.eulersbridge.isegoria.util.data.RetrofitLiveData
import com.eulersbridge.isegoria.util.data.SingleLiveData

class EfficacyQuestionsViewModel(application: Application) : AndroidViewModel(application) {

    internal val score1 = MutableLiveData<Int>()
    internal val score2 = MutableLiveData<Int>()
    internal val score3 = MutableLiveData<Int>()
    internal val score4 = MutableLiveData<Int>()

    internal fun addUserEfficacy(): LiveData<Boolean> {

        val scores = listOfNotNull(score1.value, score2.value, score3.value, score4.value)
            .map { it.toFloat() }

        if (scores.isEmpty()) {
            return SingleLiveData(false)

        } else {
            val app: IsegoriaApp = getApplication()

            val userEmail = app.loggedInUser.value!!.email
            val answers = UserSelfEfficacy(scores[0], scores[1], scores[2], scores[3])

            val efficacyRequest = RetrofitLiveData(app.api.addUserEfficacy(userEmail, answers))

            return Transformations.switchMap(efficacyRequest) {
                app.onUserSelfEfficacyCompleted()
                SingleLiveData(true)
            }
        }
    }

}
