package com.eulersbridge.isegoria.auth.signup

import android.arch.lifecycle.ViewModelProvider
import com.eulersbridge.isegoria.di.ViewModelProviderFactory
import com.eulersbridge.isegoria.network.NetworkService
import dagger.Module
import dagger.Provides

@Module
class SignUpModule {

    @Provides
    fun signUpViewModel(
        networkService: NetworkService
    ): SignUpViewModel = SignUpViewModel(networkService.api)

    @Provides
    fun provideSignUpViewModel(signUpViewModel: SignUpViewModel): ViewModelProvider.Factory
            = ViewModelProviderFactory(signUpViewModel)

}