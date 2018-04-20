package com.eulersbridge.isegoria.feed.photos


import android.arch.lifecycle.ViewModelProvider
import com.eulersbridge.isegoria.Repository
import com.eulersbridge.isegoria.inject.ViewModelProviderFactory
import dagger.Module
import dagger.Provides

@Module
class PhotosFragmentModule {

    @Provides
    fun photoAlbumsViewModel(
        repository: Repository
    ): PhotoAlbumsViewModel = PhotoAlbumsViewModel(repository)

    @Provides
    fun providePhotoAlbumsViewModel(photoAlbumsViewModel: PhotoAlbumsViewModel): ViewModelProvider.Factory
            = ViewModelProviderFactory(photoAlbumsViewModel)

}