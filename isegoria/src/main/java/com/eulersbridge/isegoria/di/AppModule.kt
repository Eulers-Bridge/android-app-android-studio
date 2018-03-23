package com.eulersbridge.isegoria.di

import android.content.Context
import com.eulersbridge.isegoria.IsegoriaApp
import com.eulersbridge.isegoria.network.NetworkService
import com.securepreferences.SecurePreferences
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AppModule {

    @Provides
    @Singleton
    fun providesContext(app: IsegoriaApp): Context = app

    @Provides
    @Singleton
    fun providesNetworkService(app: IsegoriaApp) = NetworkService(app, app)

    @Provides
    @Singleton
    fun providesSecurePreferences(app: IsegoriaApp) = SecurePreferences(app)

}