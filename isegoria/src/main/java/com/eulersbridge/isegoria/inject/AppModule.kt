package com.eulersbridge.isegoria.inject

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
    fun provideContext(app: IsegoriaApp): Context = app

    @Provides
    @Singleton
    fun provideNetworkService(app: IsegoriaApp) = NetworkService(app, app)

    @Provides
    @Singleton
    fun provideSecurePreferences(app: IsegoriaApp) = SecurePreferences(app)

}