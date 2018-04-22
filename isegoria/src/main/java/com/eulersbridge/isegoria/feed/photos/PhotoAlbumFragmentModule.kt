package com.eulersbridge.isegoria.feed.photos


import android.arch.lifecycle.ViewModelProvider
import com.eulersbridge.isegoria.data.Repository
import com.eulersbridge.isegoria.inject.ViewModelProviderFactory
import dagger.Module
import dagger.Provides

@Module
class PhotoAlbumFragmentModule {

    @Provides
    fun photoAlbumViewModel(repository: Repository): PhotoAlbumViewModel = PhotoAlbumViewModel(repository)

    @Provides
    fun providePhotoAlbumsViewModel(photoAlbumViewModel: PhotoAlbumViewModel): ViewModelProvider.Factory
            = ViewModelProviderFactory(photoAlbumViewModel)

}