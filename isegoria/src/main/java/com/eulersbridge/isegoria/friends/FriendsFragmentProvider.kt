package com.eulersbridge.isegoria.friends

import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class FriendsFragmentProvider {

    @ContributesAndroidInjector(modules = [(FriendsFragmentModule::class)])
    internal abstract fun provideFriendsFragment(): FriendsFragment

}