package com.eulersbridge.isegoria.feed.news.detail

import android.arch.lifecycle.ViewModelProvider
import com.eulersbridge.isegoria.IsegoriaApp
import com.eulersbridge.isegoria.inject.ViewModelProviderFactory
import com.eulersbridge.isegoria.network.api.API
import dagger.Module
import dagger.Provides

@Module
class NewsDetailModule {

    @Provides
    fun newsViewModel(app: IsegoriaApp, api: API): NewsDetailViewModel
            = NewsDetailViewModel(app.loggedInUser, api)

    @Provides
    fun provideNewsDetailViewModel(newsDetailViewModel: NewsDetailViewModel): ViewModelProvider.Factory
            = ViewModelProviderFactory(newsDetailViewModel)

}