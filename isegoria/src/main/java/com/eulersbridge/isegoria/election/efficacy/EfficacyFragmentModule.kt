package com.eulersbridge.isegoria.election.efficacy

import android.arch.lifecycle.ViewModelProvider
import com.eulersbridge.isegoria.IsegoriaApp
import com.eulersbridge.isegoria.inject.ViewModelProviderFactory
import com.eulersbridge.isegoria.network.api.API
import dagger.Module
import dagger.Provides

@Module
class EfficacyFragmentModule {

    @Provides
    fun efficacyViewModel(
        app: IsegoriaApp,
        api: API
    ): EfficacyViewModel = EfficacyViewModel(app, api)

    @Provides
    fun provideEfficacyViewModel(efficacyViewModel: EfficacyViewModel): ViewModelProvider.Factory
            = ViewModelProviderFactory(efficacyViewModel)

}