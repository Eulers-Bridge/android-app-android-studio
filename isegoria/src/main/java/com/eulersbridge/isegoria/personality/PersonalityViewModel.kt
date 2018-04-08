package com.eulersbridge.isegoria.personality

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.arch.lifecycle.ViewModel
import com.eulersbridge.isegoria.network.NetworkService
import com.eulersbridge.isegoria.network.api.API
import com.eulersbridge.isegoria.network.api.models.User
import com.eulersbridge.isegoria.network.api.models.UserPersonality
import com.eulersbridge.isegoria.util.data.RetrofitLiveData
import com.eulersbridge.isegoria.util.data.SingleLiveData
import javax.inject.Inject

class PersonalityViewModel
@Inject constructor(
    private val userData: LiveData<User>,
    private val api: API
) : ViewModel() {

    internal val questionsContinued = MutableLiveData<Boolean>()
    internal val questionsComplete = MutableLiveData<Boolean>()

    internal fun setUserCompletedQuestions(userPersonality: UserPersonality): LiveData<Boolean> {

        val user = userData.value

        if (user == null) {
            return SingleLiveData(false)

        } else {
            val request = RetrofitLiveData(api.addUserPersonality(user.email, userPersonality))

            return Transformations.switchMap(request) { result ->
                if (result != null) {
                    questionsComplete.postValue(true)
                    return@switchMap SingleLiveData(true)
                }

                SingleLiveData(false)
            }
        }
    }
}
