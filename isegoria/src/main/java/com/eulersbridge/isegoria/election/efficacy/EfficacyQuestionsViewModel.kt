package com.eulersbridge.isegoria.election.efficacy

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.arch.lifecycle.ViewModel
import com.eulersbridge.isegoria.IsegoriaApp
import com.eulersbridge.isegoria.network.NetworkService
import com.eulersbridge.isegoria.network.api.models.UserSelfEfficacy
import com.eulersbridge.isegoria.util.data.RetrofitLiveData
import com.eulersbridge.isegoria.util.data.SingleLiveData
import javax.inject.Inject

class EfficacyQuestionsViewModel
@Inject constructor(
    private val app: IsegoriaApp,
    private val networkService: NetworkService
) : ViewModel() {

    internal val score1 = MutableLiveData<Int>()
    internal val score2 = MutableLiveData<Int>()
    internal val score3 = MutableLiveData<Int>()
    internal val score4 = MutableLiveData<Int>()

    internal fun addUserEfficacy(): LiveData<Boolean> {

        val scores = listOfNotNull(score1.value, score2.value, score3.value, score4.value)
            .map { it.toFloat() }

        return if (scores.isEmpty()) {
            SingleLiveData(false)

        } else {
            val userEmail = app.loggedInUser.value!!.email
            val answers = UserSelfEfficacy(scores[0], scores[1], scores[2], scores[3])

            val efficacyRequest = RetrofitLiveData(networkService.api.addUserEfficacy(userEmail, answers))

            Transformations.switchMap(efficacyRequest) {
                app.onUserSelfEfficacyCompleted()
                SingleLiveData(true)
            }
        }
    }

}
