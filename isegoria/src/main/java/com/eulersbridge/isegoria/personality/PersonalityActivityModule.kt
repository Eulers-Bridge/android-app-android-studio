package com.eulersbridge.isegoria.personality

import android.arch.lifecycle.ViewModelProvider
import com.eulersbridge.isegoria.IsegoriaApp
import com.eulersbridge.isegoria.inject.ViewModelProviderFactory
import com.eulersbridge.isegoria.network.NetworkService
import dagger.Module
import dagger.Provides

@Module
class PersonalityActivityModule {

    @Provides
    fun personalityViewModel(
        app: IsegoriaApp,
        networkService: NetworkService
    ): PersonalityViewModel = PersonalityViewModel(app.loggedInUser, networkService.api)

    @Provides
    fun providePersonalityViewModel(personalityViewModel: PersonalityViewModel): ViewModelProvider.Factory
            = ViewModelProviderFactory(personalityViewModel)

}