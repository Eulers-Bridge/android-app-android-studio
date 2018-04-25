package com.eulersbridge.isegoria.auth.signup

import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class SignUpFragmentProvider {

    @ContributesAndroidInjector(modules = [(SignUpModule::class)])
    internal abstract fun provideSignUpFragment(): SignUpFragment

    @ContributesAndroidInjector
    internal abstract fun provideConsentAgreementFragment(): ConsentAgreementFragment

}