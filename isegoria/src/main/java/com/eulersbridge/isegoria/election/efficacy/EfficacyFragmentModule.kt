package com.eulersbridge.isegoria.election.efficacy

import android.arch.lifecycle.ViewModelProvider
import com.eulersbridge.isegoria.IsegoriaApp
import com.eulersbridge.isegoria.inject.ViewModelProviderFactory
import com.eulersbridge.isegoria.network.NetworkService
import dagger.Module
import dagger.Provides

@Module
class EfficacyFragmentModule {

    @Provides
    fun efficacyViewModel(
        app: IsegoriaApp,
        networkService: NetworkService
    ): EfficacyViewModel = EfficacyViewModel(app, networkService.api)

    @Provides
    fun provideEfficacyViewModel(efficacyViewModel: EfficacyViewModel): ViewModelProvider.Factory
            = ViewModelProviderFactory(efficacyViewModel)

}