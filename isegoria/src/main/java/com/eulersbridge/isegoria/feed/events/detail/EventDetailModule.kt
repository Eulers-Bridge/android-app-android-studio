package com.eulersbridge.isegoria.feed.events.detail

import android.arch.lifecycle.ViewModelProvider
import com.eulersbridge.isegoria.inject.ViewModelProviderFactory
import com.eulersbridge.isegoria.network.api.API
import dagger.Module
import dagger.Provides

@Module
class EventDetailModule {

    @Provides
    fun eventDetailViewModel(api: API): EventDetailViewModel = EventDetailViewModel(api)

    @Provides
    fun provideEventDetailViewModel(eventDetailViewModel: EventDetailViewModel): ViewModelProvider.Factory
            = ViewModelProviderFactory(eventDetailViewModel)

}