package com.eulersbridge.isegoria.auth

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModelProviders
import android.support.v4.app.FragmentActivity
import com.eulersbridge.isegoria.IsegoriaApp
import com.eulersbridge.isegoria.auth.signup.SignUpUser
import com.eulersbridge.isegoria.network.api.models.Country
import com.eulersbridge.isegoria.network.api.responses.GeneralInfoResponse
import com.eulersbridge.isegoria.util.data.SingleLiveData
import com.eulersbridge.isegoria.util.network.SimpleCallback
import retrofit2.Response


class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val countriesData = MutableLiveData<List<Country>>()

    val signUpVisible = MutableLiveData<Boolean>()
    val signUpUser = MutableLiveData<SignUpUser>()
    val signUpConsentGiven = MutableLiveData<Boolean>()

    val userLoggedIn = MutableLiveData<Boolean>()

    init {
        val app = application as IsegoriaApp

        app.api.getGeneralInfo().enqueue(object : SimpleCallback<GeneralInfoResponse>() {
            override fun handleResponse(response: Response<GeneralInfoResponse>) {
                countriesData.value = response.body()?.countries
            }
        })
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
            .flatMap { it.institutions!!  }
            .singleOrNull { it.getName() == updatedUser.institutionName }

        institution?.id.let {
            updatedUser.institutionId = it!!
            signUpUser.value = updatedUser
        }

        return IsegoriaApp.networkService.signUp(updatedUser)
    }

}
