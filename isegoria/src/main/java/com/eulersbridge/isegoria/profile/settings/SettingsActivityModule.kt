package com.eulersbridge.isegoria.profile.settings

import android.arch.lifecycle.ViewModelProvider
import com.eulersbridge.isegoria.IsegoriaApp
import com.eulersbridge.isegoria.di.ViewModelProviderFactory
import com.eulersbridge.isegoria.network.NetworkService
import dagger.Module
import dagger.Provides

@Module
class SettingsActivityModule {

    @Provides
    fun settingsViewModel(
        app: IsegoriaApp,
        networkService: NetworkService
    ): SettingsViewModel = SettingsViewModel(app, networkService)

    @Provides
    fun provideSettingsViewModel(settingsViewModel: SettingsViewModel): ViewModelProvider.Factory
            = ViewModelProviderFactory(settingsViewModel)

}