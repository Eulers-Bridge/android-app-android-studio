package com.eulersbridge.isegoria.personality

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.arch.lifecycle.ViewModel
import com.eulersbridge.isegoria.network.NetworkService
import com.eulersbridge.isegoria.network.api.models.User
import com.eulersbridge.isegoria.network.api.models.UserPersonality
import com.eulersbridge.isegoria.util.data.RetrofitLiveData
import com.eulersbridge.isegoria.util.data.SingleLiveData
import javax.inject.Inject

class PersonalityViewModel
@Inject constructor(
    private val userData: LiveData<User>,
    private val networkService: NetworkService
) : ViewModel() {

    internal val questionsContinued = MutableLiveData<Boolean>()
    internal val questionsComplete = MutableLiveData<Boolean>()

    internal fun setUserCompletedQuestions(userPersonality: UserPersonality): LiveData<Boolean> {

        val user = userData.value

        return if (user == null) {
            SingleLiveData(false)
        } else {
            val request = RetrofitLiveData(networkService.api.addUserPersonality(user.email, userPersonality))

            Transformations.switchMap(request) { success ->
                if (success != null) {
                    questionsComplete.postValue(true)
                    return@switchMap SingleLiveData(true)
                }

                SingleLiveData(false)
            }
        }
    }
}
