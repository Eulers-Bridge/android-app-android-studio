package com.eulersbridge.isegoria.auth.verification

import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class EmailVerificationFragmentProvider {

    @ContributesAndroidInjector(modules = [(EmailVerificationModule::class)])
    internal abstract fun provideEmailVerificationFragment(): EmailVerificationFragment

}