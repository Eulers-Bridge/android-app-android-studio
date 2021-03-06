package com.eulersbridge.isegoria.auth.login

import android.arch.lifecycle.ViewModelProvider
import com.eulersbridge.isegoria.data.Repository
import com.eulersbridge.isegoria.inject.ViewModelProviderFactory
import com.eulersbridge.isegoria.network.api.API
import dagger.Module
import dagger.Provides

@Module
class LoginModule {

    @Provides
    fun loginViewModel(repository: Repository, api: API): LoginViewModel = LoginViewModel(repository, api)

    @Provides
    fun provideLoginViewModel(loginViewModel: LoginViewModel): ViewModelProvider.Factory
            = ViewModelProviderFactory(loginViewModel)

}