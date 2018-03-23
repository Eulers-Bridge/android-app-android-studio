package com.eulersbridge.isegoria.feed.news


import android.arch.lifecycle.ViewModelProvider
import com.eulersbridge.isegoria.IsegoriaApp
import com.eulersbridge.isegoria.di.ViewModelProviderFactory
import com.eulersbridge.isegoria.network.NetworkService
import dagger.Module
import dagger.Provides


@Module
class NewsFragmentModule {

    @Provides
    fun newsViewModel(
        app: IsegoriaApp,
        networkService: NetworkService
    ): NewsViewModel = NewsViewModel(app, networkService)

    @Provides
    fun provideNewsViewModel(newsViewModel: NewsViewModel): ViewModelProvider.Factory
        = ViewModelProviderFactory(newsViewModel)

}