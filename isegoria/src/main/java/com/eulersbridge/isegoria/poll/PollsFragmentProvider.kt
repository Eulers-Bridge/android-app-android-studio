package com.eulersbridge.isegoria.poll

import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class PollsFragmentProvider {

    @ContributesAndroidInjector(modules = [(PollsFragmentModule::class)])
    abstract fun contributePollsFragment() : PollsFragment

}