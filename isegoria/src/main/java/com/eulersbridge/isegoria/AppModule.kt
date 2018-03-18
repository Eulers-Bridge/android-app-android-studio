package com.eulersbridge.isegoria

import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AppModule {

    @Singleton
    @Provides
    fun provideApiService(application: IsegoriaApp) = application.api

}