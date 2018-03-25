package com.eulersbridge.isegoria.feed.events.detail

import android.arch.lifecycle.ViewModelProvider
import com.eulersbridge.isegoria.inject.ViewModelProviderFactory
import com.eulersbridge.isegoria.network.NetworkService
import dagger.Module
import dagger.Provides

@Module
class EventDetailModule {

    @Provides
    fun eventDetailViewModel(
        networkService: NetworkService
    ): EventDetailViewModel =
        EventDetailViewModel(networkService.api)

    @Provides
    fun provideEventDetailViewModel(eventDetailViewModel: EventDetailViewModel): ViewModelProvider.Factory
            = ViewModelProviderFactory(eventDetailViewModel)

}