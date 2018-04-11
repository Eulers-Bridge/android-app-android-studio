package com.eulersbridge.isegoria.feed.news


import android.arch.lifecycle.ViewModelProvider
import com.eulersbridge.isegoria.IsegoriaApp
import com.eulersbridge.isegoria.inject.ViewModelProviderFactory
import com.eulersbridge.isegoria.network.api.API
import dagger.Module
import dagger.Provides


@Module
class NewsFragmentModule {

    @Provides
    fun newsViewModel(
        app: IsegoriaApp,
        api: API
    ): NewsViewModel = NewsViewModel(app, api)

    @Provides
    fun provideNewsViewModel(newsViewModel: NewsViewModel): ViewModelProvider.Factory
        = ViewModelProviderFactory(newsViewModel)

}