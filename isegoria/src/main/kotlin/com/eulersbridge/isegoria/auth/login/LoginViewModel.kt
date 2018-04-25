package com.eulersbridge.isegoria.auth.login

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.util.Patterns
import com.eulersbridge.isegoria.data.LoginState
import com.eulersbridge.isegoria.data.Repository
import com.eulersbridge.isegoria.network.api.API
import com.eulersbridge.isegoria.util.BaseViewModel
import com.eulersbridge.isegoria.util.data.SingleLiveData
import com.eulersbridge.isegoria.util.extension.toBooleanSingle
import javax.inject.Inject

class LoginViewModel
@Inject constructor(
        private val repository: Repository,
        private val api: API
) : BaseViewModel() {

    private val email = MutableLiveData<String>()
    internal val emailError = Transformations.switchMap(email) { SingleLiveData(!it.isValidEmail) }

    private val password = MutableLiveData<String>()

    internal val passwordError = Transformations.switchMap(password) { SingleLiveData(it.isNullOrBlank()) }

    internal val formEnabled = MutableLiveData<Boolean>()
    internal val networkError = MutableLiveData<Boolean>()

    internal val canShowPasswordResetDialog = MutableLiveData<Boolean>()

    private val String?.isValidEmail
        get() = !isNullOrBlank() && Patterns.EMAIL_ADDRESS.matcher(this!!).matches()

    init {
        formEnabled.value = true
        networkError.value = false
        canShowPasswordResetDialog.value = true

        email.value = repository.getSavedEmail()
        email.value = repository.getSavedPassword()

        repository.getLoginState().subscribe {
            when (it) {
                is LoginState.LoggingIn -> {
                    networkError.postValue(false)
                }
                is LoginState.LoginFailure -> {
                    networkError.postValue(true)
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
        formEnabled.value = false

        if (emailError.value == false && passwordError.value == false) {
            // Email/password not null as validation checks for null
            repository.login(email.value!!, password.value!!)
        }

        formEnabled.value = true
    }

    internal fun setNetworkErrorShown() {
        networkError.value = false
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
