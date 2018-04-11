package com.eulersbridge.isegoria.feed.photos


import android.arch.lifecycle.ViewModelProvider
import com.eulersbridge.isegoria.IsegoriaApp
import com.eulersbridge.isegoria.inject.ViewModelProviderFactory
import com.eulersbridge.isegoria.network.api.API
import dagger.Module
import dagger.Provides

@Module
class PhotosFragmentModule {

    @Provides
    fun photosViewModel(
        app: IsegoriaApp,
        api: API
    ): PhotoAlbumsViewModel = PhotoAlbumsViewModel(app.loggedInUser, api)

    @Provides
    fun providePhotoAlbumsViewModel(photoAlbumsViewModel: PhotoAlbumsViewModel): ViewModelProvider.Factory
            = ViewModelProviderFactory(photoAlbumsViewModel)

}