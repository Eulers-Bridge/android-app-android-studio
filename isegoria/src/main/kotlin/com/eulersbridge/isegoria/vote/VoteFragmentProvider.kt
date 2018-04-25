package com.eulersbridge.isegoria.vote

import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class VoteFragmentProvider {

    @ContributesAndroidInjector(modules = [(VoteFragmentModule::class)])
    abstract fun contributeVoteViewPagerFragment() : VoteViewPagerFragment

}