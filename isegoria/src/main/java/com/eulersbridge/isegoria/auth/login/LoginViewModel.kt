package com.eulersbridge.isegoria.auth.login

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.util.Patterns
import com.eulersbridge.isegoria.IsegoriaApp
import com.eulersbridge.isegoria.enqueue
import com.eulersbridge.isegoria.isNetworkAvailable
import com.eulersbridge.isegoria.util.data.SingleLiveData

class LoginViewModel(application: Application) : AndroidViewModel(application) {

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

        val app = application as IsegoriaApp

        app.savedUserEmail?.let { email.value = it }
        app.savedUserPassword?.let { password.value = it }
    }

    private val app: IsegoriaApp by lazy {
        getApplication<IsegoriaApp>()
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

                // Not null as email & password validation checks test for null
                val loginRequest = app.login(email.value!!, password.value!!)

                return Transformations.switchMap(loginRequest) { success ->
                    if (success == true)
                        return@switchMap SingleLiveData(true)

                    formEnabled.value = true
                    SingleLiveData(false)
                }
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
            app.api.requestPasswordReset(email!!).enqueue()
            canShowPasswordResetDialog.value = true

            return true
        }

        return false
    }
}
