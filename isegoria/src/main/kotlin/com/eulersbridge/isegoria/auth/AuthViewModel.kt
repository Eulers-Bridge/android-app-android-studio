package com.eulersbridge.isegoria.auth

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import com.eulersbridge.isegoria.auth.signup.SignUpUser
import com.eulersbridge.isegoria.data.LoginState
import com.eulersbridge.isegoria.data.Repository
import com.eulersbridge.isegoria.network.api.model.Country
import com.eulersbridge.isegoria.util.BaseViewModel
import com.eulersbridge.isegoria.util.data.SingleLiveData
import com.eulersbridge.isegoria.util.data.SingleLiveEvent
import com.eulersbridge.isegoria.util.extension.subscribeSuccess
import com.eulersbridge.isegoria.util.extension.toBooleanSingle
import com.eulersbridge.isegoria.util.extension.toLiveData
import javax.inject.Inject


class AuthViewModel @Inject constructor(private val repository: Repository) : BaseViewModel() {

    private var countries: List<Country>? = null

    val authFinished = SingleLiveEvent<Any>()

    val signUpVisible = MutableLiveData<Boolean>()
    val signUpUser = MutableLiveData<SignUpUser>()
    val signUpConsentGiven = MutableLiveData<Boolean>()
    val verificationComplete = MutableLiveData<Boolean>()

    init {
        repository.getSignUpCountries()
                .subscribeSuccess { countries = it }
                .addToDisposable()

        repository.getLoginState()
                .filter { it is LoginState.LoggedIn }
                .subscribe { authFinished.call() }
                .addToDisposable()
    }

    fun onSignUpBackPressed() {
        signUpVisible.value = false
        signUpUser.value = null
    }

    fun signUp(): LiveData<Boolean> {
        if (countries == null)
            return SingleLiveData(false)

        // Not possible for signUpUser's value to be null,
        // as sign-up process is linear and gated.
        val updatedUser = signUpUser.value!!.copy()

        val institution = countries!!
                .flatMap { it.institutions.orEmpty()  }
                .singleOrNull { it.getName() == updatedUser.institutionName }

        institution?.id?.let {
            updatedUser.institutionId = it
            signUpUser.value = updatedUser
        }

        return repository.signUp(updatedUser)
                .doOnComplete{
                    signUpUser.postValue(null)
                }
                .toBooleanSingle()
                .toLiveData()
    }
}