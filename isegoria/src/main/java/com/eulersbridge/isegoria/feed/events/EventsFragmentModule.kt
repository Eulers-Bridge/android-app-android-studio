package com.eulersbridge.isegoria.feed.events

import android.arch.lifecycle.ViewModelProvider
import com.eulersbridge.isegoria.IsegoriaApp
import com.eulersbridge.isegoria.di.ViewModelProviderFactory
import com.eulersbridge.isegoria.network.NetworkService
import dagger.Module
import dagger.Provides

@Module
class EventsFragmentModule {

    @Provides
    fun eventsViewModel(
        app: IsegoriaApp,
        networkService: NetworkService
    ): EventsViewModel = EventsViewModel(app, networkService)

    @Provides
    fun provideEventsViewModel(eventsViewModel: EventsViewModel): ViewModelProvider.Factory
            = ViewModelProviderFactory(eventsViewModel)

}