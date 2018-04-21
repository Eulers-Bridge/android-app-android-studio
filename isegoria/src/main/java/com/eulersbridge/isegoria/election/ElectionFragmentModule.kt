package com.eulersbridge.isegoria.election

import android.arch.lifecycle.ViewModelProvider
import com.eulersbridge.isegoria.data.Repository
import com.eulersbridge.isegoria.inject.ViewModelProviderFactory
import dagger.Module
import dagger.Provides

@Module
class ElectionFragmentModule {

    @Provides
    fun electionViewModel(repository: Repository): ElectionViewModel = ElectionViewModel(repository)

    @Provides
    fun provideElectionViewModel(electionViewModel: ElectionViewModel): ViewModelProvider.Factory
            = ViewModelProviderFactory(electionViewModel)

}