package com.eulersbridge.isegoria.auth

import android.arch.lifecycle.MutableLiveData
import android.os.AsyncTask
import com.eulersbridge.isegoria.R
import com.eulersbridge.isegoria.auth.signup.SignUpUser
import com.eulersbridge.isegoria.data.LoginState
import com.eulersbridge.isegoria.data.Repository
import com.eulersbridge.isegoria.network.api.model.Country
import com.eulersbridge.isegoria.util.BaseViewModel
import com.eulersbridge.isegoria.util.data.SingleLiveEvent
import com.eulersbridge.isegoria.util.extension.subscribeSuccess
import io.reactivex.rxkotlin.subscribeBy
import javax.inject.Inject


class AuthViewModel @Inject constructor(private val repository: Repository) : BaseViewModel() {

    private var countries: List<Country>? = null

    val authFinished = SingleLiveEvent<Any>()

    val signUpVisible = MutableLiveData<Boolean>()
    val signUpUser = MutableLiveData<SignUpUser>()
    val signUpConsentGiven = MutableLiveData<Boolean>()
    val signRequestComplete = MutableLiveData<Boolean>()
    val toasMessage = SingleLiveEvent<Int>()

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

    fun signUp() {
        if (countries == null) {
            signRequestComplete.postValue(false)
            return
        }

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

        AsyncTask.execute {
            repository.signUp(updatedUser)
                    .subscribeBy(
                            onComplete = {
                                signRequestComplete.postValue(true)
                            },
                            onError = {
                                signRequestComplete.postValue(false)
                            }
                    )
                    .addToDisposable()
        }
    }

    internal fun onResendVerification() {
        if (signUpUser.value != null) {
            repository.resendVerificationEmail(signUpUser.value!!.email)
                    .subscribeBy(
                            onComplete = {
                                toasMessage.postValue(R.string.email_verification_resend_toast_message)
                            },
                            onError = {
                                toasMessage.postValue(R.string.unknown_error_occurred)
                            }
                    )
                    .addToDisposable()
        }
    }

    internal fun onEmailVerified() {
        if (signUpUser.value != null) {
            repository.login(signUpUser.value!!.email, signUpUser.value!!.password, repository.getSavedApiBaseUrl()!!)
        }

    }
}