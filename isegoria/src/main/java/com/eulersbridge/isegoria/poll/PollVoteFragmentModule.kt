package com.eulersbridge.isegoria.poll

import android.arch.lifecycle.ViewModelProvider
import com.eulersbridge.isegoria.inject.ViewModelProviderFactory
import com.eulersbridge.isegoria.network.api.API
import dagger.Module
import dagger.Provides

@Module
class PollVoteFragmentModule {

    @Provides
    fun pollsViewModel(api: API): PollVoteViewModel = PollVoteViewModel(api)

    @Provides
    fun providePollVoteViewModel(pollVoteViewModel: PollVoteViewModel): ViewModelProvider.Factory
            = ViewModelProviderFactory(pollVoteViewModel)

}