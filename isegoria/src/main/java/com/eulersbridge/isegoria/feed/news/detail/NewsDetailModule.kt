package com.eulersbridge.isegoria.feed.news.detail

import android.arch.lifecycle.ViewModelProvider
import com.eulersbridge.isegoria.IsegoriaApp
import com.eulersbridge.isegoria.di.ViewModelProviderFactory
import com.eulersbridge.isegoria.network.NetworkService
import dagger.Module
import dagger.Provides

@Module
class NewsDetailModule {

    @Provides
    fun newsViewModel(
        app: IsegoriaApp,
        networkService: NetworkService
    ): NewsDetailViewModel =
        NewsDetailViewModel(app.loggedInUser, networkService.api)

    @Provides
    fun provideNewsDetailViewModel(newsDetailViewModel: NewsDetailViewModel): ViewModelProvider.Factory
            = ViewModelProviderFactory(newsDetailViewModel)

}