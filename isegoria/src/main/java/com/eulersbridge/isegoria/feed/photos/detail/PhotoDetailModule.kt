package com.eulersbridge.isegoria.feed.photos.detail

import android.arch.lifecycle.ViewModelProvider
import com.eulersbridge.isegoria.IsegoriaApp
import com.eulersbridge.isegoria.inject.ViewModelProviderFactory
import com.eulersbridge.isegoria.network.api.API
import dagger.Module
import dagger.Provides

@Module
class PhotoDetailModule {

    @Provides
    fun newsViewModel(app: IsegoriaApp, api: API): PhotoDetailViewModel
            = PhotoDetailViewModel(app.loggedInUser, api)

    @Provides
    fun providePhotoDetailViewModel(photoDetailViewModel: PhotoDetailViewModel): ViewModelProvider.Factory
            = ViewModelProviderFactory(photoDetailViewModel)

}