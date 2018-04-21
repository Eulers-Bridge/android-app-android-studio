package com.eulersbridge.isegoria.profile.settings

import android.arch.lifecycle.ViewModelProvider
import com.eulersbridge.isegoria.data.Repository
import com.eulersbridge.isegoria.inject.ViewModelProviderFactory
import dagger.Module
import dagger.Provides

@Module
class SettingsActivityModule {

    @Provides
    fun settingsViewModel(repository: Repository): SettingsViewModel = SettingsViewModel(repository)

    @Provides
    fun provideSettingsViewModel(settingsViewModel: SettingsViewModel): ViewModelProvider.Factory
            = ViewModelProviderFactory(settingsViewModel)

}