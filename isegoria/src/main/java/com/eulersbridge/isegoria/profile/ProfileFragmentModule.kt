package com.eulersbridge.isegoria.profile

import android.arch.lifecycle.ViewModelProvider
import com.eulersbridge.isegoria.IsegoriaApp
import com.eulersbridge.isegoria.inject.ViewModelProviderFactory
import com.eulersbridge.isegoria.network.api.API
import dagger.Module
import dagger.Provides

@Module
class ProfileFragmentModule {

    @Provides
    fun profileViewModel(app: IsegoriaApp, api: API): ProfileViewModel = ProfileViewModel(app, api)

    @Provides
    fun provideProfileViewModel(profileViewModel: ProfileViewModel): ViewModelProvider.Factory
            = ViewModelProviderFactory(profileViewModel)

}