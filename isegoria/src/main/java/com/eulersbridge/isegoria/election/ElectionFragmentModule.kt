package com.eulersbridge.isegoria.election

import android.arch.lifecycle.ViewModelProvider
import com.eulersbridge.isegoria.IsegoriaApp
import com.eulersbridge.isegoria.inject.ViewModelProviderFactory
import com.eulersbridge.isegoria.network.NetworkService
import dagger.Module
import dagger.Provides

@Module
class ElectionFragmentModule {

    @Provides
    fun electionViewModel(
        app: IsegoriaApp,
        networkService: NetworkService
    ): ElectionViewModel = ElectionViewModel(app.loggedInUser, networkService.api)

    @Provides
    fun provideElectionViewModel(electionViewModel: ElectionViewModel): ViewModelProvider.Factory
            = ViewModelProviderFactory(electionViewModel)

}