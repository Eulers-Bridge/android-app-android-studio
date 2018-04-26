package com.eulersbridge.isegoria.auth.signup

import android.arch.lifecycle.ViewModelProvider
import com.eulersbridge.isegoria.data.Repository
import com.eulersbridge.isegoria.inject.ViewModelProviderFactory
import dagger.Module
import dagger.Provides

@Module
class SignUpModule {

    @Provides
    fun signUpViewModel(repository: Repository): SignUpViewModel = SignUpViewModel(repository)

    @Provides
    fun provideSignUpViewModel(signUpViewModel: SignUpViewModel): ViewModelProvider.Factory
            = ViewModelProviderFactory(signUpViewModel)

}