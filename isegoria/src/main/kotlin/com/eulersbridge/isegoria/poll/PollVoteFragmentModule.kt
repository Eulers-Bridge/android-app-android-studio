package com.eulersbridge.isegoria.poll

import android.arch.lifecycle.ViewModelProvider
import com.eulersbridge.isegoria.data.Repository
import com.eulersbridge.isegoria.inject.ViewModelProviderFactory
import dagger.Module
import dagger.Provides

@Module
class PollVoteFragmentModule {

    @Provides
    fun pollsViewModel(repository: Repository): PollVoteViewModel = PollVoteViewModel(repository)

    @Provides
    fun providePollVoteViewModel(pollVoteViewModel: PollVoteViewModel): ViewModelProvider.Factory
            = ViewModelProviderFactory(pollVoteViewModel)

}