package com.eulersbridge.isegoria.inject

import android.content.Context
import com.eulersbridge.isegoria.AppRouter
import com.eulersbridge.isegoria.BuildConfig
import com.eulersbridge.isegoria.IsegoriaApp
import com.eulersbridge.isegoria.data.DataRepository
import com.eulersbridge.isegoria.data.Repository
import com.eulersbridge.isegoria.network.*
import com.eulersbridge.isegoria.network.adapters.LenientLongAdapter
import com.eulersbridge.isegoria.network.adapters.NullPrimitiveAdapter
import com.eulersbridge.isegoria.network.adapters.TimestampAdapter
import com.eulersbridge.isegoria.network.api.API
import com.eulersbridge.isegoria.util.extension.addAppHeaders
import com.securepreferences.SecurePreferences
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.File
import javax.inject.Singleton

@Module
class AppModule {

    @Provides
    @Singleton
    fun provideContext(app: IsegoriaApp): Context = app

    @Provides
    @Singleton
    fun provideSecurePreferences(app: IsegoriaApp): SecurePreferences = SecurePreferences(app)

    @Provides
    @Singleton
    fun provideNetworkConfig(): NetworkConfig = NetworkConfig()

    @Provides
    @Singleton
    fun provideCache(appContext: Context): Cache {
        val cacheSize = 40 * 1024 * 1024 // Maximum cache size of 40 MiB
        return Cache(File(appContext.cacheDir, "network"), cacheSize.toLong())
    }

    @Provides
    @Singleton
    fun provideMoshi(): MoshiConverterFactory {
        val moshi = Moshi.Builder()
                .add(LenientLongAdapter())
                .add(NullPrimitiveAdapter())
                .add(TimestampAdapter())
                .build()

        return MoshiConverterFactory.create(moshi)
    }

    @Provides
    @Singleton
    fun provideBaseUrlInterceptor(networkConfig: NetworkConfig): BaseUrlInterceptor =
            BaseUrlInterceptor(networkConfig)

    @Provides
    @Singleton
    fun provideAuthenticationInterceptor(): AuthenticationInterceptor =
        AuthenticationInterceptor()

    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
        val logging = HttpLoggingInterceptor()

        logging.level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BASIC
        } else {
            HttpLoggingInterceptor.Level.NONE
        }

        return logging
    }

    @Provides
    @Singleton
    fun provideCacheInterceptor(appContext: Context): CacheInterceptor
            = CacheInterceptor(appContext)

    @Provides
    @Singleton
    fun provideHttpClient(
            loggingInterceptor: HttpLoggingInterceptor,
            baseUrlInterceptor: BaseUrlInterceptor,
            authenticationInterceptor: AuthenticationInterceptor,
            cacheInterceptor: CacheInterceptor,
            cache: Cache
    ): OkHttpClient {
        return OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .addInterceptor { chain ->
                    val request = chain.request().newBuilder()
                            .addAppHeaders()
                    chain.proceed(request.build())
                }
                .addInterceptor(cacheInterceptor)
                .addInterceptor(baseUrlInterceptor)
                .addInterceptor(authenticationInterceptor)
                .addNetworkInterceptor(cacheInterceptor)
                .cache(cache)
                .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(httpClient: OkHttpClient, networkConfig: NetworkConfig, moshi: MoshiConverterFactory): Retrofit
            = Retrofit.Builder()
            .client(httpClient)
            .baseUrl(networkConfig.baseUrl)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.createAsync())
            .addConverterFactory(UnwrapConverterFactory())
            .addConverterFactory(moshi)
            .build()

    @Provides
    @Singleton
    fun provideApiRetrofit(retrofit: Retrofit): API = retrofit.create(API::class.java)

    @Provides
    @Singleton
    fun provideRepository(appContext: Context, httpClient: OkHttpClient, api: API, networkConfig: NetworkConfig, securePreferences: SecurePreferences): Repository
            = DataRepository(appContext, httpClient, api, networkConfig, securePreferences)

    @Provides
    @Singleton
    fun provideAppRouter(app: IsegoriaApp): AppRouter = app

}