package com.eulersbridge.isegoria.auth.verification

import android.arch.lifecycle.MutableLiveData
import com.eulersbridge.isegoria.AppRouter
import com.eulersbridge.isegoria.auth.login.LoginViewModel
import com.eulersbridge.isegoria.data.LoginState
import com.eulersbridge.isegoria.data.Repository
import com.eulersbridge.isegoria.util.BaseViewModel
import com.eulersbridge.isegoria.util.data.SingleLiveEvent
import javax.inject.Inject

class EmailVerificationViewModel
@Inject constructor (
    private val appRouter: AppRouter,
    private val repository: Repository
) : BaseViewModel() {

    internal val resendVerificationButtonEnabled = MutableLiveData<Boolean>()
    internal val completeButtonEnabled = MutableLiveData<Boolean>()
    internal val loginError =  SingleLiveEvent<LoginViewModel.LoginError>()

    init {
        completeButtonEnabled.value = true
        resendVerificationButtonEnabled.value = true

        repository.getLoginState().subscribe {
            when (it) {
                is LoginState.LoginFailure -> {
                    loginError.postValue(LoginViewModel.LoginError.UnknownFailure)
                }
                is LoginState.LoginUnauthorised -> {
                    loginError.postValue(LoginViewModel.LoginError.NotAuthorised)
                }
            }
        }.addToDisposable()
    }

    internal fun onDestroy() {
        appRouter.setUserVerificationScreenVisible(false)
    }
}