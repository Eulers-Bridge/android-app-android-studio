package com.eulersbridge.isegoria.network;

import android.util.Base64;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * For all methods that are not annotated with a "No Authentication" header,
 * add basic username/password credentials "Authorization" header.
 */
class AuthenticationInterceptor implements Interceptor {

    private final String username;
    private final String password;

    public AuthenticationInterceptor(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();

        if (request.header("No-Authentication") == null) {
            String credentials = username + ":" + password;
            String base64EncodedCredentials =
                    Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);

            request = request.newBuilder()
                    .removeHeader("No-Authentication")
                    .addHeader("Authorization", String.format("Basic %s", base64EncodedCredentials))
                    .build();
        }

        return chain.proceed(request);
    }
}