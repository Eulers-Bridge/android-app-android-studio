package com.eulersbridge.isegoria.feed.news


import android.arch.lifecycle.ViewModelProvider
import com.eulersbridge.isegoria.data.Repository
import com.eulersbridge.isegoria.inject.ViewModelProviderFactory
import dagger.Module
import dagger.Provides


@Module
class NewsFragmentModule {

    @Provides
    fun newsViewModel(repository: Repository): NewsViewModel = NewsViewModel(repository)

    @Provides
    fun provideNewsViewModel(newsViewModel: NewsViewModel): ViewModelProvider.Factory
        = ViewModelProviderFactory(newsViewModel)

}