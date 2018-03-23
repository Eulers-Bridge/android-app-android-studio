package com.eulersbridge.isegoria.auth

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.arch.lifecycle.ViewModel
import com.eulersbridge.isegoria.auth.signup.SignUpUser
import com.eulersbridge.isegoria.network.NetworkService
import com.eulersbridge.isegoria.network.api.models.Country
import com.eulersbridge.isegoria.onSuccess
import com.eulersbridge.isegoria.util.data.SingleLiveData
import javax.inject.Inject


class AuthViewModel @Inject constructor(
    private val networkService: NetworkService
) : ViewModel() {

    private val countriesData = MutableLiveData<List<Country>>()

    val signUpVisible = MutableLiveData<Boolean>()
    val signUpUser = MutableLiveData<SignUpUser>()
    val signUpConsentGiven = MutableLiveData<Boolean>()
    val verificationComplete = MutableLiveData<Boolean>()

    val userLoggedIn = MutableLiveData<Boolean>()

    init {
        networkService.api.getGeneralInfo().onSuccess {
            countriesData.value = it.countries
        }
    }

    fun onSignUpBackPressed() {
        signUpVisible.value = false
        signUpUser.value = null
    }

    fun signUp(): LiveData<Boolean> {
        val countries = countriesData.value ?: return SingleLiveData(false)

        // Not possible for signUpUser's value to be null,
        // as sign-up process is linear and gated.
        val updatedUser = signUpUser.value!!.copy()

        val institution = countries
            .flatMap { it.institutions.orEmpty()  }
            .singleOrNull { it.getName() == updatedUser.institutionName }

        institution?.id?.let {
            updatedUser.institutionId = it
            signUpUser.value = updatedUser
        }

        return Transformations.switchMap(networkService.signUp(updatedUser)) { success ->
            if (!success)
                signUpUser.value = null

            SingleLiveData(success)
        }
    }
}
