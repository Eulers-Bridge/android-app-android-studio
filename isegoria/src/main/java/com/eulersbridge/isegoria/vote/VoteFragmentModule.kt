package com.eulersbridge.isegoria.vote

import android.arch.lifecycle.ViewModelProvider
import com.eulersbridge.isegoria.IsegoriaApp
import com.eulersbridge.isegoria.di.ViewModelProviderFactory
import com.eulersbridge.isegoria.network.NetworkService
import dagger.Module
import dagger.Provides

@Module
class VoteFragmentModule {

    @Provides
    fun voteViewModel(
        app: IsegoriaApp,
        networkService: NetworkService
    ): VoteViewModel = VoteViewModel(app.loggedInUser, networkService.api)

    @Provides
    fun provideVoteViewModel(voteViewModel: VoteViewModel): ViewModelProvider.Factory
            = ViewModelProviderFactory(voteViewModel)

}