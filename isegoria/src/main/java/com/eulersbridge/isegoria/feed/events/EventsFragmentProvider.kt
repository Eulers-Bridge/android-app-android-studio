package com.eulersbridge.isegoria.feed.events

import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class EventsFragmentProvider {

    @ContributesAndroidInjector(modules = [(EventsFragmentModule::class)])
    internal abstract fun provideEventsFragment(): EventsFragment

}