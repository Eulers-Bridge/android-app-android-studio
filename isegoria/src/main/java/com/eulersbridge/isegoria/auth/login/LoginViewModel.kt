package com.eulersbridge.isegoria.auth.login

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.arch.lifecycle.ViewModel
import android.util.Patterns
import com.eulersbridge.isegoria.IsegoriaApp
import com.eulersbridge.isegoria.data.LoginState
import com.eulersbridge.isegoria.data.Repository
import com.eulersbridge.isegoria.network.api.API
import com.eulersbridge.isegoria.util.data.SingleLiveData
import com.eulersbridge.isegoria.util.extension.isNetworkAvailable
import com.eulersbridge.isegoria.util.extension.toBooleanSingle
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import javax.inject.Inject

class LoginViewModel
@Inject constructor(
        private val repository: Repository,
        private val app: IsegoriaApp,
        private val api: API
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    internal val email = MutableLiveData<String>()
    internal val emailError = Transformations.switchMap(email) { SingleLiveData(!it.isValidEmail) }

    internal val password = MutableLiveData<String>()

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

        repository.loginState.subscribe {
            when (it) {
                is LoginState.LoginFailure -> {
                    formEnabled.postValue(true)
                }
            }
        }.addTo(compositeDisposable)
    }

    override fun onCleared() {
        compositeDisposable.dispose()
    }

    internal fun login() {
        formEnabled.value = false

        if (emailError.value == false && passwordError.value == false) {
            if (!app.isNetworkAvailable()) {
                networkError.value = true
                formEnabled.value = true

            } else {
                networkError.value = false

                // Email/password not null as validation checks for null
                repository.login(email.value!!, password.value!!)
            }
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
                    .addTo(compositeDisposable)
            canShowPasswordResetDialog.value = true

            return true
        }

        return false
    }
}
