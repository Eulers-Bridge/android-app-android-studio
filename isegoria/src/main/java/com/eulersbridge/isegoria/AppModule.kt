package com.eulersbridge.isegoria

import android.content.Context
import com.eulersbridge.isegoria.network.NetworkService
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AppModule(val app: IsegoriaApp) {

    @Provides
    @Singleton
    fun providesApiService() = app.api

    @Provides
    @Singleton
    fun providesContext(): Context = app

    @Provides
    @Singleton
    fun providesNetworkService() = NetworkService(app, app)

}