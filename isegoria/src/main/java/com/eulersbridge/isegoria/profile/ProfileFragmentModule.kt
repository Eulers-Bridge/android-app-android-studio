package com.eulersbridge.isegoria.profile

import android.arch.lifecycle.ViewModelProvider
import com.eulersbridge.isegoria.IsegoriaApp
import com.eulersbridge.isegoria.di.ViewModelProviderFactory
import com.eulersbridge.isegoria.network.NetworkService
import dagger.Module
import dagger.Provides

@Module
class ProfileFragmentModule {

    @Provides
    fun profileViewModel(
        app: IsegoriaApp,
        networkService: NetworkService
    ): ProfileViewModel = ProfileViewModel(app, networkService.api)

    @Provides
    fun provideProfileViewModel(profileViewModel: ProfileViewModel): ViewModelProvider.Factory
            = ViewModelProviderFactory(profileViewModel)

}