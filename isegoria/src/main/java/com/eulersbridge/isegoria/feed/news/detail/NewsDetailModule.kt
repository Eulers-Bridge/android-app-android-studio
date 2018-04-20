package com.eulersbridge.isegoria.feed.news.detail

import android.arch.lifecycle.ViewModelProvider
import com.eulersbridge.isegoria.Repository
import com.eulersbridge.isegoria.inject.ViewModelProviderFactory
import dagger.Module
import dagger.Provides

@Module
class NewsDetailModule {

    @Provides
    fun newsViewModel(repository: Repository): NewsDetailViewModel = NewsDetailViewModel(repository)

    @Provides
    fun provideNewsDetailViewModel(newsDetailViewModel: NewsDetailViewModel): ViewModelProvider.Factory
            = ViewModelProviderFactory(newsDetailViewModel)

}