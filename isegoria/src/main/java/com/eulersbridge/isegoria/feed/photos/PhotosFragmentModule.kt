package com.eulersbridge.isegoria.feed.photos


import android.arch.lifecycle.ViewModelProvider
import com.eulersbridge.isegoria.IsegoriaApp
import com.eulersbridge.isegoria.inject.ViewModelProviderFactory
import com.eulersbridge.isegoria.network.NetworkService
import dagger.Module
import dagger.Provides

@Module
class PhotosFragmentModule {

    @Provides
    fun photosViewModel(
        app: IsegoriaApp,
        networkService: NetworkService
    ): PhotoAlbumsViewModel = PhotoAlbumsViewModel(app.loggedInUser, networkService.api)

    @Provides
    fun providePhotoAlbumsViewModel(photoAlbumsViewModel: PhotoAlbumsViewModel): ViewModelProvider.Factory
            = ViewModelProviderFactory(photoAlbumsViewModel)

}