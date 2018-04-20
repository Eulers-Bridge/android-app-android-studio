package com.eulersbridge.isegoria.auth

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.eulersbridge.isegoria.LoginState
import com.eulersbridge.isegoria.Repository
import com.eulersbridge.isegoria.auth.signup.SignUpUser
import com.eulersbridge.isegoria.network.NetworkService
import com.eulersbridge.isegoria.network.api.API
import com.eulersbridge.isegoria.network.api.model.Country
import com.eulersbridge.isegoria.util.data.SingleLiveData
import com.eulersbridge.isegoria.util.extension.subscribeSuccess
import com.eulersbridge.isegoria.util.extension.toLiveData
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import javax.inject.Inject


class AuthViewModel @Inject constructor(
        repository: Repository,
        api: API,
        private val networkService: NetworkService
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    private var countries: List<Country>? = null

    val authFinished = MutableLiveData<Boolean>()

    val signUpVisible = MutableLiveData<Boolean>()
    val signUpUser = MutableLiveData<SignUpUser>()
    val signUpConsentGiven = MutableLiveData<Boolean>()
    val verificationComplete = MutableLiveData<Boolean>()

    val userLoggedIn = MutableLiveData<Boolean>()

    init {
        api.getGeneralInfo().subscribeSuccess {
            countries = it.countries
        }

        repository.loginState
                .filter { it is LoginState.LoggedIn }
                .subscribe {
                    authFinished.postValue(true)
                }
                .addTo(compositeDisposable)
    }

    override fun onCleared() {
        compositeDisposable.dispose()
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

        return networkService.signUp(updatedUser)
                .doOnSuccess { success ->
                    if (!success)
                        signUpUser.postValue(null)
                }
                .toLiveData()
    }
}
