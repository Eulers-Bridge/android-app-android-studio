package com.eulersbridge.isegoria.auth.login

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import com.eulersbridge.isegoria.IsegoriaApp
import com.eulersbridge.isegoria.isNetworkAvailable
import com.eulersbridge.isegoria.isValidEmail
import com.eulersbridge.isegoria.util.data.SingleLiveData
import com.eulersbridge.isegoria.util.network.IgnoredCallback

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    internal val email = MutableLiveData<String>()
    internal val emailError = Transformations.switchMap(email) { SingleLiveData(!it.isValidEmail) }

    internal val password = MutableLiveData<String>()

    internal val passwordError = Transformations.switchMap(password) { SingleLiveData(!it.isNullOrBlank()) }

    internal val formEnabled = MutableLiveData<Boolean>()
    internal val networkError = MutableLiveData<Boolean>()

    internal val canShowPasswordResetDialog = MutableLiveData<Boolean>()

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
        app.loginVisible.value = false
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
                        SingleLiveData(true)

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
            app.api.requestPasswordReset(email!!).enqueue(IgnoredCallback())
            canShowPasswordResetDialog.value = true

            return true
        }

        return false
    }
}
