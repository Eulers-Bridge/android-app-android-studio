package com.eulersbridge.isegoria.poll
import android.arch.lifecycle.ViewModelProvider
import com.eulersbridge.isegoria.data.Repository
import com.eulersbridge.isegoria.inject.ViewModelProviderFactory
import dagger.Module
import dagger.Provides

@Module
class PollsFragmentModule {

    @Provides
    fun pollsViewModel(repository: Repository): PollsViewModel = PollsViewModel(repository)

    @Provides
    fun providePollsViewModel(pollsViewModel: PollsViewModel): ViewModelProvider.Factory
            = ViewModelProviderFactory(pollsViewModel)

}