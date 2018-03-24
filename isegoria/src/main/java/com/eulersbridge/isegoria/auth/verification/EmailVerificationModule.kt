package com.eulersbridge.isegoria.auth.verification
import android.arch.lifecycle.ViewModelProvider
import com.eulersbridge.isegoria.IsegoriaApp
import com.eulersbridge.isegoria.di.ViewModelProviderFactory
import com.eulersbridge.isegoria.network.NetworkService
import dagger.Module
import dagger.Provides

@Module
class EmailVerificationModule {

    @Provides
    fun emailVerificationViewModel(
        app: IsegoriaApp,
        networkService: NetworkService
    ): EmailVerificationViewModel =
        EmailVerificationViewModel(
            app,
            networkService.api
        )

    @Provides
    fun provideEmailVerificationViewModel(emailVerificationViewModel: EmailVerificationViewModel): ViewModelProvider.Factory
            = ViewModelProviderFactory(emailVerificationViewModel)

}