package com.eulersbridge.isegoria.di

import com.eulersbridge.isegoria.MainActivity
import com.eulersbridge.isegoria.feed.events.EventsFragmentProvider
import com.eulersbridge.isegoria.feed.news.NewsFragmentProvider
import com.eulersbridge.isegoria.feed.photos.PhotosFragmentProvider
import com.eulersbridge.isegoria.personality.PersonalityActivity
import com.eulersbridge.isegoria.personality.PersonalityModule
import dagger.Module
import dagger.android.ContributesAndroidInjector


@Module
abstract class ActivityBuilder {

    @ContributesAndroidInjector(modules = [(NewsFragmentProvider::class),
        (PhotosFragmentProvider::class), (EventsFragmentProvider::class)])
    internal abstract fun bindMainActivity(): MainActivity

    @ContributesAndroidInjector(modules = [(PersonalityModule::class)])
    internal abstract fun bindPersonalityActivity(): PersonalityActivity

//    @ContributesAndroidInjector(modules = [(NewsDetailActivityModule::class)])
//    internal abstract fun bindNewsDetailActivity(): NewsDetailActivity

//    @ContributesAndroidInjector
//    internal abstract fun bindAuthActivity(): AuthActivity

}