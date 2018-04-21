package com.eulersbridge.isegoria.feed.photos


import android.arch.lifecycle.ViewModelProvider
import com.eulersbridge.isegoria.inject.ViewModelProviderFactory
import com.eulersbridge.isegoria.network.api.API
import dagger.Module
import dagger.Provides

@Module
class PhotoAlbumFragmentModule {

    @Provides
    fun photoAlbumViewModel(api: API): PhotoAlbumViewModel = PhotoAlbumViewModel(api)

    @Provides
    fun providePhotoAlbumsViewModel(photoAlbumViewModel: PhotoAlbumViewModel): ViewModelProvider.Factory
            = ViewModelProviderFactory(photoAlbumViewModel)

}