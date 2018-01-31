package com.eulersbridge.isegoria.personality

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import com.eulersbridge.isegoria.IsegoriaApp
import com.eulersbridge.isegoria.network.api.models.UserPersonality
import com.eulersbridge.isegoria.util.data.RetrofitLiveData
import com.eulersbridge.isegoria.util.data.SingleLiveData

class PersonalityViewModel(application: Application) : AndroidViewModel(application) {

    internal val userSkippedQuestions = MutableLiveData<Boolean>()
    internal val userContinuedQuestions = MutableLiveData<Boolean>()
    internal val userCompletedQuestions = MutableLiveData<Boolean>()

    internal fun setUserCompletedQuestions(userPersonality: UserPersonality): LiveData<Boolean> {

        val app = getApplication<IsegoriaApp>()

        val user = app.loggedInUser.value
        if (user != null) {
            val request = RetrofitLiveData(app.api.addUserPersonality(user.email, userPersonality))

            return Transformations.switchMap(request) { success ->
                if (success != null) {
                    userCompletedQuestions.postValue(true)
                    return@switchMap SingleLiveData(true)
                }

                SingleLiveData(false)
            }
        }

        return SingleLiveData(false)
    }
}
