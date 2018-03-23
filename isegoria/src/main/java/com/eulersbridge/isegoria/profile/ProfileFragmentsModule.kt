package com.eulersbridge.isegoria.profile;

import com.eulersbridge.isegoria.profile.badges.ProfileBadgesFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ProfileFragmentsModule {

    @ContributesAndroidInjector
    abstract fun contributeProfileOverviewFragment() : ProfileOverviewFragment

    @ContributesAndroidInjector
    abstract fun contributeProfileTaskProgressFragment() : ProfileTaskProgressFragment

    @ContributesAndroidInjector
    abstract fun contributeProfileBadgesFragment() : ProfileBadgesFragment

}