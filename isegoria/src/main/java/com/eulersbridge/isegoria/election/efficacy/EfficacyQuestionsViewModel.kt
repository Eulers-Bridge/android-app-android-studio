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
        if (score1.value == null
                || score2.value == null
                || score3.value == null
                || score4.value == null)
            return SingleLiveData(false)

        val app: IsegoriaApp = getApplication()

        val userEmail = app.loggedInUser.value!!.email
        val answers = UserSelfEfficacy(
                score1.value!!.toFloat(), score2.value!!.toFloat(), score3.value!!.toFloat(), score4.value!!.toFloat()
        )

        val efficacyRequest = RetrofitLiveData(app.api.addUserEfficacy(userEmail, answers))

        return Transformations.switchMap(efficacyRequest) {
            app.onUserSelfEfficacyCompleted()
            SingleLiveData(true)
        }
    }

}
