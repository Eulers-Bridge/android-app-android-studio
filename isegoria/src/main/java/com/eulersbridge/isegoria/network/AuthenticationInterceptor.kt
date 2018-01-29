package com.eulersbridge.isegoria.network

import android.util.Base64
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

/**
 * For all methods that are not annotated with a "No Authentication" header,
 * add basic username/password credentials "Authorization" header.
 */
internal class AuthenticationInterceptor(
        private val username: String,
        private val password: String
) : Interceptor {

    private val base64EncodedCredentials: String by lazy {
        val credentials = "$username:$password"
        Base64.encodeToString(credentials.toByteArray(), Base64.NO_WRAP)
    }

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()

        if (request.header("No-Authentication") == null)
            request = request.newBuilder()
                    .removeHeader("No-Authentication")
                    .addHeader("Authorization", "Basic $base64EncodedCredentials")
                    .build()

        return chain.proceed(request)
    }
}