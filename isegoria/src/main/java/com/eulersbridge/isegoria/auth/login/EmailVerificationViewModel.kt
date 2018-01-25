package com.eulersbridge.isegoria.auth.login

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Transformations
import com.eulersbridge.isegoria.IsegoriaApp
import com.eulersbridge.isegoria.util.data.RetrofitLiveData
import com.eulersbridge.isegoria.util.data.SingleLiveData

class EmailVerificationViewModel(application: Application) : AndroidViewModel(application) {

    val app: IsegoriaApp by lazy {
        getApplication<IsegoriaApp>()
    }

    internal fun onExit() {
        app.userVerificationVisible.value = false
    }

    internal fun userVerified(): LiveData<Boolean> {
        return app.login()
    }

    internal fun resendVerification(): LiveData<Boolean> {
        return Transformations.switchMap(app.loggedInUser) { user ->
            if (user != null) {
                val verificationRequest = RetrofitLiveData(app.api.sendVerificationEmail(user.email))
                Transformations.switchMap(verificationRequest) { SingleLiveData(true) }
            }

            SingleLiveData(false)
        }

    }
}