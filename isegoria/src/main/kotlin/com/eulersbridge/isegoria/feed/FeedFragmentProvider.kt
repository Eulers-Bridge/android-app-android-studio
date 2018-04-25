package com.eulersbridge.isegoria.feed


import com.eulersbridge.isegoria.feed.events.EventsFragment
import com.eulersbridge.isegoria.feed.events.EventsFragmentModule
import com.eulersbridge.isegoria.feed.news.NewsFragment
import com.eulersbridge.isegoria.feed.news.NewsFragmentModule
import com.eulersbridge.isegoria.feed.photos.PhotoAlbumFragment
import com.eulersbridge.isegoria.feed.photos.PhotoAlbumFragmentModule
import com.eulersbridge.isegoria.feed.photos.PhotosFragment
import com.eulersbridge.isegoria.feed.photos.PhotosFragmentModule
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class FeedFragmentProvider {

    @ContributesAndroidInjector(modules = [(FeedFragmentModule::class)])
    internal abstract fun provideFeedFragment(): FeedFragment

    @ContributesAndroidInjector(modules = [(NewsFragmentModule::class)])
    internal abstract fun provideNewsFragment(): NewsFragment

    @ContributesAndroidInjector(modules = [(PhotosFragmentModule::class)])
    internal abstract fun providePhotosFragment(): PhotosFragment

    @ContributesAndroidInjector(modules = [(PhotoAlbumFragmentModule::class)])
    internal abstract fun providePhotoAlbumsFragment(): PhotoAlbumFragment

    @ContributesAndroidInjector(modules = [(EventsFragmentModule::class)])
    internal abstract fun provideEventsFragment(): EventsFragment

}