package com.eulersbridge.isegoria.personality

import android.arch.lifecycle.ViewModelProvider
import com.eulersbridge.isegoria.data.Repository
import com.eulersbridge.isegoria.inject.ViewModelProviderFactory
import dagger.Module
import dagger.Provides

@Module
class PersonalityActivityModule {

    @Provides
    fun personalityViewModel(repository: Repository): PersonalityViewModel = PersonalityViewModel(repository)

    @Provides
    fun providePersonalityViewModel(personalityViewModel: PersonalityViewModel): ViewModelProvider.Factory
            = ViewModelProviderFactory(personalityViewModel)

}