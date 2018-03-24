package com.eulersbridge.isegoria.auth.login

import android.arch.lifecycle.ViewModelProvider
import com.eulersbridge.isegoria.IsegoriaApp
import com.eulersbridge.isegoria.di.ViewModelProviderFactory
import com.eulersbridge.isegoria.network.NetworkService
import dagger.Module
import dagger.Provides

@Module
class LoginModule {

    @Provides
    fun loginViewModel(
        app: IsegoriaApp,
        networkService: NetworkService
    ): LoginViewModel = LoginViewModel(app, networkService.api)

    @Provides
    fun provideLoginViewModel(loginViewModel: LoginViewModel): ViewModelProvider.Factory
            = ViewModelProviderFactory(loginViewModel)

}