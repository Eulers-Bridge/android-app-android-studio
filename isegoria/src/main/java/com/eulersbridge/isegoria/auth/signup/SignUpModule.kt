package com.eulersbridge.isegoria.auth.signup

import android.arch.lifecycle.ViewModelProvider
import com.eulersbridge.isegoria.inject.ViewModelProviderFactory
import com.eulersbridge.isegoria.network.api.API
import dagger.Module
import dagger.Provides

@Module
class SignUpModule {

    @Provides
    fun signUpViewModel(
        api: API
    ): SignUpViewModel = SignUpViewModel(api)

    @Provides
    fun provideSignUpViewModel(signUpViewModel: SignUpViewModel): ViewModelProvider.Factory
            = ViewModelProviderFactory(signUpViewModel)

}