package com.eulersbridge.isegoria.auth.login

import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class LoginFragmentProvider {

    @ContributesAndroidInjector(modules = [(LoginModule::class)])
    internal abstract fun provideLoginFragment(): LoginFragment

}