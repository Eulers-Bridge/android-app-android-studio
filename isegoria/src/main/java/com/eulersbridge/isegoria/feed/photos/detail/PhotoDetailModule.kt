package com.eulersbridge.isegoria.feed.photos.detail

import android.arch.lifecycle.ViewModelProvider
import com.eulersbridge.isegoria.IsegoriaApp
import com.eulersbridge.isegoria.di.ViewModelProviderFactory
import com.eulersbridge.isegoria.network.NetworkService
import dagger.Module
import dagger.Provides

@Module
class PhotoDetailModule {

    @Provides
    fun newsViewModel(
        app: IsegoriaApp,
        networkService: NetworkService
    ): PhotoDetailViewModel =
        PhotoDetailViewModel(
            app.loggedInUser,
            networkService.api
        )

    @Provides
    fun providePhotoDetailViewModel(photoDetailViewModel: PhotoDetailViewModel): ViewModelProvider.Factory
            = ViewModelProviderFactory(photoDetailViewModel)

}