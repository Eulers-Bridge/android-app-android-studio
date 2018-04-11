package com.eulersbridge.isegoria.vote

import android.arch.lifecycle.ViewModelProvider
import com.eulersbridge.isegoria.IsegoriaApp
import com.eulersbridge.isegoria.inject.ViewModelProviderFactory
import com.eulersbridge.isegoria.network.api.API
import dagger.Module
import dagger.Provides

@Module
class VoteFragmentModule {

    @Provides
    fun voteViewModel(
        app: IsegoriaApp,
        api: API
    ): VoteViewModel = VoteViewModel(app.loggedInUser, api)

    @Provides
    fun provideVoteViewModel(voteViewModel: VoteViewModel): ViewModelProvider.Factory
            = ViewModelProviderFactory(voteViewModel)

}