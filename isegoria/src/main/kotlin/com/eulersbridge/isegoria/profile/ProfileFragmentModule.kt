package com.eulersbridge.isegoria.profile

import android.arch.lifecycle.ViewModelProvider
import com.eulersbridge.isegoria.AppRouter
import com.eulersbridge.isegoria.data.Repository
import com.eulersbridge.isegoria.inject.ViewModelProviderFactory
import dagger.Module
import dagger.Provides

@Module
class ProfileFragmentModule {

    @Provides
    fun profileViewModel(repository: Repository, appRouter: AppRouter): ProfileViewModel
            = ProfileViewModel(repository, appRouter)

    @Provides
    fun provideProfileViewModel(profileViewModel: ProfileViewModel): ViewModelProvider.Factory
            = ViewModelProviderFactory(profileViewModel)

}