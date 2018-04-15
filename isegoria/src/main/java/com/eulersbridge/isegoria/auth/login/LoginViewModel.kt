package com.eulersbridge.isegoria.auth.login

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.arch.lifecycle.ViewModel
import android.util.Patterns
import com.eulersbridge.isegoria.IsegoriaApp
import com.eulersbridge.isegoria.isNetworkAvailable
import com.eulersbridge.isegoria.network.api.API
import com.eulersbridge.isegoria.toLiveData
import com.eulersbridge.isegoria.util.data.SingleLiveData
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import javax.inject.Inject

class LoginViewModel
@Inject constructor(
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

        email.value = app.savedUserEmail
        password.value = app.savedUserPassword
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }

    internal fun onExit() {
        app.hideLoginScreen()
    }

    internal fun login(): LiveData<Boolean> {
        formEnabled.value = false

        if (emailError.value == false && passwordError.value == false) {
            if (!app.isNetworkAvailable()) {
                networkError.value = true
                formEnabled.value = true

            } else {
                networkError.value = false

                // Email/password not null as validation checks for null
                return app.login(email.value!!, password.value!!).map { success ->
                    if (!success)
                        formEnabled.value = true

                    success
                }.toLiveData()
            }
        }

        formEnabled.value = true

        return SingleLiveData(false)
    }

    internal fun setNetworkErrorShown() {
        networkError.value = false
    }

    internal fun requestPasswordRecoveryEmail(email: String?): Boolean {
        if (email.isValidEmail) {
            canShowPasswordResetDialog.value = false
            // If email is valid, it is non-null
            api.requestPasswordReset(email!!).subscribe().addTo(compositeDisposable)
            canShowPasswordResetDialog.value = true

            return true
        }

        return false
    }
}
