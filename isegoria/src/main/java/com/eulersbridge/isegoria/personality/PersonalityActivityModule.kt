package com.eulersbridge.isegoria.personality

import android.arch.lifecycle.ViewModelProvider
import com.eulersbridge.isegoria.IsegoriaApp
import com.eulersbridge.isegoria.inject.ViewModelProviderFactory
import com.eulersbridge.isegoria.network.api.API
import dagger.Module
import dagger.Provides

@Module
class PersonalityActivityModule {

    @Provides
    fun personalityViewModel(
        app: IsegoriaApp,
        api: API
    ): PersonalityViewModel = PersonalityViewModel(app.loggedInUser, api)

    @Provides
    fun providePersonalityViewModel(personalityViewModel: PersonalityViewModel): ViewModelProvider.Factory
            = ViewModelProviderFactory(personalityViewModel)

}