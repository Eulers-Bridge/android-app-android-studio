package com.eulersbridge.isegoria.feed

import android.arch.lifecycle.ViewModelProvider
import com.eulersbridge.isegoria.AppRouter
import com.eulersbridge.isegoria.inject.ViewModelProviderFactory
import dagger.Module
import dagger.Provides

@Module
class FeedFragmentModule {

    @Provides
    fun feedViewModel(appRouter: AppRouter): FeedViewModel = FeedViewModel(appRouter)

    @Provides
    fun provideFeedViewModel(feedViewModel: FeedViewModel): ViewModelProvider.Factory
            = ViewModelProviderFactory(feedViewModel)

}