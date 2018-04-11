package com.eulersbridge.isegoria.network

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

        // TODO: Not an ideal solution
        if (originalHost != newBaseUrl.host() && !originalHost.contains("www.isegoria.com.au", true)) {

            /* Rewrite the host and port of the request URL, and add the new encoded path
            to the start of the request URL's encoded path. */

            val firstPathSegments = if (newBaseUrl.encodedPath().last().toString() == "/") {
                newBaseUrl.encodedPath().dropLast(1)
            } else {
                newBaseUrl.encodedPath()
            }
            val newEncodedPath = "$firstPathSegments${request.url().encodedPath()}"

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