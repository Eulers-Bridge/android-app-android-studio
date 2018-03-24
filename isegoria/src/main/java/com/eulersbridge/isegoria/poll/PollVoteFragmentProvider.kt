package com.eulersbridge.isegoria.poll

import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class PollVoteFragmentProvider {

    @ContributesAndroidInjector(modules = [(PollVoteFragmentModule::class)])
    abstract fun contributePollVoteFragment() : PollVoteFragment

}