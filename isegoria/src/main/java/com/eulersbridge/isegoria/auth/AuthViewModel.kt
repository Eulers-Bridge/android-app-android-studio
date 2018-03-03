package com.eulersbridge.isegoria.auth

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import com.eulersbridge.isegoria.IsegoriaApp
import com.eulersbridge.isegoria.auth.signup.SignUpUser
import com.eulersbridge.isegoria.network.api.models.Country
import com.eulersbridge.isegoria.onSuccess
import com.eulersbridge.isegoria.util.data.SingleLiveData


class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val countriesData = MutableLiveData<List<Country>>()

    val signUpVisible = MutableLiveData<Boolean>()
    val signUpUser = MutableLiveData<SignUpUser>()
    val signUpConsentGiven = MutableLiveData<Boolean>()
    val verificationComplete = MutableLiveData<Boolean>()

    val userLoggedIn = MutableLiveData<Boolean>()

    init {
        val app = application as IsegoriaApp

        app.api.getGeneralInfo().onSuccess {
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

        return Transformations.switchMap(IsegoriaApp.networkService.signUp(updatedUser)) { success ->
            if (!success)
                signUpUser.value = null

            SingleLiveData(success)
        }
    }
}
