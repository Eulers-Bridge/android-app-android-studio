package com.eulersbridge.isegoria.feed.photos.detail

import android.arch.lifecycle.ViewModelProvider
import com.eulersbridge.isegoria.Repository
import com.eulersbridge.isegoria.inject.ViewModelProviderFactory
import dagger.Module
import dagger.Provides

@Module
class PhotoDetailModule {

    @Provides
    fun newsViewModel(repository: Repository): PhotoDetailViewModel = PhotoDetailViewModel(repository)

    @Provides
    fun providePhotoDetailViewModel(photoDetailViewModel: PhotoDetailViewModel): ViewModelProvider.Factory
            = ViewModelProviderFactory(photoDetailViewModel)

}