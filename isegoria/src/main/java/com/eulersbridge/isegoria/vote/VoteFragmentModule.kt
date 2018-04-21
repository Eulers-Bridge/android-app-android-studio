package com.eulersbridge.isegoria.vote

import android.arch.lifecycle.ViewModelProvider
import com.eulersbridge.isegoria.data.Repository
import com.eulersbridge.isegoria.inject.ViewModelProviderFactory
import dagger.Module
import dagger.Provides

@Module
class VoteFragmentModule {

    @Provides
    fun voteViewModel(repository: Repository): VoteViewModel = VoteViewModel(repository)

    @Provides
    fun provideVoteViewModel(voteViewModel: VoteViewModel): ViewModelProvider.Factory
            = ViewModelProviderFactory(voteViewModel)

}