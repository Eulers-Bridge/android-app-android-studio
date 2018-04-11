package com.eulersbridge.isegoria.network

import android.util.Base64
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

/**
 * Adds a basic username/password credentials "Authorization" header to all Retrofit request methods
 * that are not annotated with a "No Authentication" header.
 */
class AuthenticationInterceptor : Interceptor {

    companion object {
        var username: String? = null
        var password: String? = null
    }

    private fun getBase64EncodedCredentials(): String {
        val credentials = "$username:$password"
        return Base64.encodeToString(credentials.toByteArray(), Base64.NO_WRAP)
    }

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {

        var request = chain.request()
        val haveCreds = username != null && password != null

        if (request.header("No-Authentication") == null && haveCreds)
            request = request.newBuilder()
                    .removeHeader("No-Authentication")
                    .addHeader("Authorization", "Basic ${getBase64EncodedCredentials()}")
                    .build()

        return chain.proceed(request)
    }
}