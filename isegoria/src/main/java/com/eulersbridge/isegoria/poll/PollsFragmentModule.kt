package com.eulersbridge.isegoria.poll
import android.arch.lifecycle.ViewModelProvider
import com.eulersbridge.isegoria.IsegoriaApp
import com.eulersbridge.isegoria.inject.ViewModelProviderFactory
import com.eulersbridge.isegoria.network.api.API
import dagger.Module
import dagger.Provides

@Module
class PollsFragmentModule {

    @Provides
    fun pollsViewModel(
        app: IsegoriaApp,
        api: API
    ): PollsViewModel = PollsViewModel(app.loggedInUser, api)

    @Provides
    fun providePollsViewModel(pollsViewModel: PollsViewModel): ViewModelProvider.Factory
            = ViewModelProviderFactory(pollsViewModel)

}