package com.eulersbridge.isegoria.auth.verification
import android.arch.lifecycle.ViewModelProvider
import com.eulersbridge.isegoria.IsegoriaApp
import com.eulersbridge.isegoria.inject.ViewModelProviderFactory
import com.eulersbridge.isegoria.network.api.API
import dagger.Module
import dagger.Provides

@Module
class EmailVerificationModule {

    @Provides
    fun emailVerificationViewModel(app: IsegoriaApp, api: API): EmailVerificationViewModel
            = EmailVerificationViewModel(app,api)

    @Provides
    fun provideEmailVerificationViewModel(emailVerificationViewModel: EmailVerificationViewModel): ViewModelProvider.Factory
            = ViewModelProviderFactory(emailVerificationViewModel)

}