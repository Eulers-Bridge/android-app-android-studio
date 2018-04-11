package com.eulersbridge.isegoria.network

import android.content.Context
import com.eulersbridge.isegoria.isNetworkAvailable
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

/**
 * Forces caching of GET requests for 1 minute, or 5 minutes if no network connection,
 * by setting 'Cache-Control' header of request.
 */
class CacheInterceptor(private val appContext: Context) : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        if (request.method() == "GET") {
            val cacheHeaderValue = if (appContext.isNetworkAvailable())
                "public, max-age=60" // 60 seconds or 1 minute
            else
                "public, only-if-cached, max-stale=300" // 300 seconds or 5 minutes

            return response.newBuilder()
                    .removeHeader("Pragma")
                    .removeHeader("Cache-Control")
                    .header("Cache-Control", cacheHeaderValue)
                    .build()
        }

        return response
    }
}