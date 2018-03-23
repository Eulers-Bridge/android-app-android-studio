package com.eulersbridge.isegoria.feed.photos


import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class PhotosFragmentProvider {

    @ContributesAndroidInjector(modules = [(PhotosFragmentModule::class)])
    internal abstract fun providePhotosFragment(): PhotosFragment

}