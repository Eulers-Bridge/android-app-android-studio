package com.eulersbridge.isegoria.auth.login

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.util.Log
import android.util.Patterns
import com.eulersbridge.isegoria.data.LoginState
import com.eulersbridge.isegoria.data.Repository
import com.eulersbridge.isegoria.network.api.API
import com.eulersbridge.isegoria.network.api.model.ClientInstitution
import com.eulersbridge.isegoria.network.api.model.Election
import com.eulersbridge.isegoria.network.api.model.Institution
import com.eulersbridge.isegoria.util.BaseViewModel
import com.eulersbridge.isegoria.util.data.SingleLiveData
import com.eulersbridge.isegoria.util.data.SingleLiveEvent
import com.eulersbridge.isegoria.util.extension.toBooleanSingle
import io.reactivex.Single
import io.reactivex.subjects.BehaviorSubject
import javax.inject.Inject

class LoginViewModel @Inject constructor(private val repository: Repository, private val api: API) : BaseViewModel() {

    enum class LoginError {
        NotAuthorised, UnkownFailure
    }

    private val email = MutableLiveData<String>()
    internal val emailError = Transformations.switchMap(email) { SingleLiveData(!it.isValidEmail) }

    private val password = MutableLiveData<String>()

    private val institutionSubject = BehaviorSubject.createDefault<List<ClientInstitution>>(listOf())
    internal val institutionLiveData = MutableLiveData<List<ClientInstitution?>>()

    internal val passwordError = Transformations.switchMap(password) { SingleLiveData(it.isNullOrBlank()) }

    internal val formEnabled = MutableLiveData<Boolean>()
    internal val loginError =  SingleLiveEvent<LoginError>()

    internal val canShowPasswordResetDialog = MutableLiveData<Boolean>()

    private val String?.isValidEmail
        get() = !isNullOrBlank() && Patterns.EMAIL_ADDRESS.matcher(this!!).matches()

    init {
        formEnabled.value = true
        canShowPasswordResetDialog.value = true

        // Pre-fill saved user email
        email.value = repository.getSavedEmail()

        repository.getLoginState().subscribe {
            when (it) {
                is LoginState.LoginFailure -> {
                    loginError.postValue(LoginError.UnkownFailure)
                    formEnabled.postValue(true)
                }
                is LoginState.LoginUnauthorised -> {
                    loginError.postValue(LoginError.NotAuthorised)
                    formEnabled.postValue(true)
                }
            }
        }.addToDisposable()
    }

    fun setEmail(value: String?) {
        this.email.value = value
    }

    fun setPassword(value: String?) {
        this.password.value = value
    }

    internal fun login() {
        if (emailError.value == false && passwordError.value == false) {
            // Email/password not null as validation checks for null

            formEnabled.value = false

            repository.login(email.value!!, password.value!!)
        }
    }

    internal fun getAllInstitutionUrlsAndNames() {
        repository.getInstitutionURLs()
                .doOnSuccess { institution ->
                    institutionLiveData.postValue(institution!!)
                }
                .subscribe()
                .addToDisposable()
    }

    internal fun requestPasswordRecoveryEmail(email: String?): Boolean {
        if (email.isValidEmail) {
            canShowPasswordResetDialog.value = false

            // If email is valid, it is non-null
            api.requestPasswordReset(email!!)
                    .toBooleanSingle()
                    .subscribe()
                    .addToDisposable()

            canShowPasswordResetDialog.value = true

            return true
        }

        return false
    }
}
