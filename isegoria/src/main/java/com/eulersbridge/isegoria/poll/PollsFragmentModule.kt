package com.eulersbridge.isegoria.poll
import android.arch.lifecycle.ViewModelProvider
import com.eulersbridge.isegoria.IsegoriaApp
import com.eulersbridge.isegoria.di.ViewModelProviderFactory
import com.eulersbridge.isegoria.network.NetworkService
import dagger.Module
import dagger.Provides

@Module
class PollsFragmentModule {

    @Provides
    fun pollsViewModel(
        app: IsegoriaApp,
        networkService: NetworkService
    ): PollsViewModel = PollsViewModel(app.loggedInUser, networkService.api)

    @Provides
    fun providePollsViewModel(pollsViewModel: PollsViewModel): ViewModelProvider.Factory
            = ViewModelProviderFactory(pollsViewModel)

}