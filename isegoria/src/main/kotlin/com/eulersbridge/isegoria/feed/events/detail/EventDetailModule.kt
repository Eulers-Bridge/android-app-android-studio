package com.eulersbridge.isegoria.feed.events.detail

import android.arch.lifecycle.ViewModelProvider
import com.eulersbridge.isegoria.data.Repository
import com.eulersbridge.isegoria.inject.ViewModelProviderFactory
import dagger.Module
import dagger.Provides

@Module
class EventDetailModule {

    @Provides
    fun eventDetailViewModel(repository: Repository): EventDetailViewModel = EventDetailViewModel(repository)

    @Provides
    fun provideEventDetailViewModel(eventDetailViewModel: EventDetailViewModel): ViewModelProvider.Factory
            = ViewModelProviderFactory(eventDetailViewModel)

}