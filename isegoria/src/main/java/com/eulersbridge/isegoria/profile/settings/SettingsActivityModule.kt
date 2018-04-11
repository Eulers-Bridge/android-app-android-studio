package com.eulersbridge.isegoria.profile.settings

import android.arch.lifecycle.ViewModelProvider
import com.eulersbridge.isegoria.IsegoriaApp
import com.eulersbridge.isegoria.inject.ViewModelProviderFactory
import com.eulersbridge.isegoria.network.NetworkService
import com.eulersbridge.isegoria.network.api.API
import dagger.Module
import dagger.Provides

@Module
class SettingsActivityModule {

    @Provides
    fun settingsViewModel(
        app: IsegoriaApp,
        api: API,
        networkService: NetworkService
    ): SettingsViewModel = SettingsViewModel(app, api, networkService)

    @Provides
    fun provideSettingsViewModel(settingsViewModel: SettingsViewModel): ViewModelProvider.Factory
            = ViewModelProviderFactory(settingsViewModel)

}