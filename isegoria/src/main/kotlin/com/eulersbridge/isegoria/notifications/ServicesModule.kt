package com.eulersbridge.isegoria.notifications

import dagger.Module
import dagger.android.ContributesAndroidInjector


@Module
abstract class ServicesModule {

    @ContributesAndroidInjector
    internal abstract fun provideFirebaseIDService(): FirebaseIDService

}