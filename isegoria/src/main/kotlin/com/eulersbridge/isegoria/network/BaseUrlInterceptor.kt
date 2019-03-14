package com.eulersbridge.isegoria.network

import com.eulersbridge.isegoria.PLACEHOLDER_BASE_URL
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

/**
 * Re-writes host, port and encoded path of requests to a set base URL.
 */
class BaseUrlInterceptor(private val networkConfig: NetworkConfig) : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()

        val newBaseUrl = HttpUrl.parse(networkConfig.baseUrl)!!
        val originalHost = request.url().host()
        val placeholderHost = HttpUrl.parse(PLACEHOLDER_BASE_URL)!!.host()

        if (originalHost == placeholderHost) {
            val newEncodedPath = "${newBaseUrl.encodedPath().dropLast(1)}${request.url().encodedPath()}"

            val newUrl = request.url().newBuilder()
                    .host(newBaseUrl.host())
                    .port(newBaseUrl.port())
                    .encodedPath(newEncodedPath)
                    .build()

            request = request.newBuilder()
                    .url(newUrl)
                    .build()
        }

        return chain.proceed(request)
    }
}