package com.eulersbridge.isegoria.auth.verification
import android.arch.lifecycle.ViewModelProvider
import com.eulersbridge.isegoria.AppRouter
import com.eulersbridge.isegoria.Repository
import com.eulersbridge.isegoria.inject.ViewModelProviderFactory
import dagger.Module
import dagger.Provides

@Module
class EmailVerificationModule {

    @Provides
    fun emailVerificationViewModel(appRouter: AppRouter, repository: Repository): EmailVerificationViewModel
            = EmailVerificationViewModel(appRouter, repository)

    @Provides
    fun provideEmailVerificationViewModel(emailVerificationViewModel: EmailVerificationViewModel): ViewModelProvider.Factory
            = ViewModelProviderFactory(emailVerificationViewModel)

}