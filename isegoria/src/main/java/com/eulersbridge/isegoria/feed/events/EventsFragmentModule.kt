package com.eulersbridge.isegoria.feed.events

import android.arch.lifecycle.ViewModelProvider
import com.eulersbridge.isegoria.data.Repository
import com.eulersbridge.isegoria.inject.ViewModelProviderFactory
import dagger.Module
import dagger.Provides

@Module
class EventsFragmentModule {

    @Provides
    fun eventsViewModel(
        repository: Repository
    ): EventsViewModel = EventsViewModel(repository)

    @Provides
    fun provideEventsViewModel(eventsViewModel: EventsViewModel): ViewModelProvider.Factory
            = ViewModelProviderFactory(eventsViewModel)

}