package com.eulersbridge.isegoria.auth.verification

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Transformations
import android.arch.lifecycle.ViewModel
import com.eulersbridge.isegoria.IsegoriaApp
import com.eulersbridge.isegoria.network.api.API
import com.eulersbridge.isegoria.toBooleanSingle
import com.eulersbridge.isegoria.toLiveData
import com.eulersbridge.isegoria.util.data.SingleLiveData
import javax.inject.Inject

class EmailVerificationViewModel
@Inject constructor (
    private val app: IsegoriaApp,
    private val api: API
) : ViewModel() {

    internal fun onDestroy() {
        app.userVerificationVisible.value = false
    }

    internal fun userVerified(): LiveData<Boolean> = app.login().toLiveData()

    internal fun resendVerification(): LiveData<Boolean> {
        return Transformations.switchMap(app.loggedInUser) {
            return@switchMap if (it == null) {
                SingleLiveData(false)

            } else {
                api.sendVerificationEmail(it.email).toBooleanSingle().toLiveData()
            }
        }
    }
}