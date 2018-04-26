package com.eulersbridge.isegoria.profile

import com.eulersbridge.isegoria.profile.badges.ProfileBadgesFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ProfileFragmentProvider {

    @ContributesAndroidInjector(modules = [(ProfileFragmentModule::class)])
    abstract fun contributeProfileViewPagerFragment() : ProfileViewPagerFragment

    @ContributesAndroidInjector(modules = [(ProfileFragmentModule::class)])
    abstract fun contributeProfileOverviewFragment() : ProfileOverviewFragment

    @ContributesAndroidInjector(modules = [(ProfileFragmentModule::class)])
    abstract fun contributeProfileTaskProgressFragment() : ProfileTaskProgressFragment

    @ContributesAndroidInjector(modules = [(ProfileFragmentModule::class)])
    abstract fun contributeProfileBadgesFragment() : ProfileBadgesFragment

}