package com.eulersbridge.isegoria.feed.photos


import android.arch.lifecycle.ViewModelProvider
import com.eulersbridge.isegoria.IsegoriaApp
import com.eulersbridge.isegoria.di.ViewModelProviderFactory
import com.eulersbridge.isegoria.network.NetworkService
import dagger.Module
import dagger.Provides

@Module
class PhotosFragmentModule {

    @Provides
    fun photosViewModel(
        app: IsegoriaApp,
        networkService: NetworkService
    ): PhotoAlbumsViewModel = PhotoAlbumsViewModel(app, networkService)

    @Provides
    fun providePhotoAlbumsViewModel(photoAlbumsViewModel: PhotoAlbumsViewModel): ViewModelProvider.Factory
            = ViewModelProviderFactory(photoAlbumsViewModel)

}