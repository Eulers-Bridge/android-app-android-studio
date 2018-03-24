package com.eulersbridge.isegoria.poll

import android.arch.lifecycle.ViewModelProvider
import com.eulersbridge.isegoria.di.ViewModelProviderFactory
import com.eulersbridge.isegoria.network.NetworkService
import dagger.Module
import dagger.Provides

@Module
class PollVoteFragmentModule {

    @Provides
    fun pollsViewModel(
        networkService: NetworkService
    ): PollVoteViewModel = PollVoteViewModel(networkService.api)

    @Provides
    fun providePollVoteViewModel(pollVoteViewModel: PollVoteViewModel): ViewModelProvider.Factory
            = ViewModelProviderFactory(pollVoteViewModel)

}