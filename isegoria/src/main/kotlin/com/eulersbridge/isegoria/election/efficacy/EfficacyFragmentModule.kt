package com.eulersbridge.isegoria.election.efficacy

import android.arch.lifecycle.ViewModelProvider
import com.eulersbridge.isegoria.data.Repository
import com.eulersbridge.isegoria.inject.ViewModelProviderFactory
import dagger.Module
import dagger.Provides

@Module
class EfficacyFragmentModule {

    @Provides
    fun efficacyViewModel(repository: Repository): EfficacyViewModel = EfficacyViewModel(repository)

    @Provides
    fun provideEfficacyViewModel(efficacyViewModel: EfficacyViewModel): ViewModelProvider.Factory
            = ViewModelProviderFactory(efficacyViewModel)

}