package com.eulersbridge.isegoria.auth.verification

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.eulersbridge.isegoria.AppRouter
import com.eulersbridge.isegoria.data.Repository
import io.reactivex.rxkotlin.subscribeBy
import javax.inject.Inject

class EmailVerificationViewModel
@Inject constructor (
    private val appRouter: AppRouter,
    private val repository: Repository
) : ViewModel() {

    internal val resendVerificationButtonEnabled = MutableLiveData<Boolean>()
    internal val completeButtonEnabled = MutableLiveData<Boolean>()

    init {
        completeButtonEnabled.value = true
        resendVerificationButtonEnabled.value = true
    }

    internal fun onDestroy() {
        appRouter.setUserVerificationScreenVisible(false)
    }

    internal fun onVerificationComplete() {

    }

    internal fun onResendVerification() {
        resendVerificationButtonEnabled.value = false

        repository.resendVerificationEmail()
                .subscribeBy(
                        onComplete = {

                        },
                        onError = {
                            resendVerificationButtonEnabled.postValue(true)
                        }
                )
    }
}