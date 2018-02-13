package com.eulersbridge.isegoria.util.network;

import retrofit2.Call;
import retrofit2.Response;

/**
 * A simple Retrofit callback for a call `enqueue` that logs errors (via IgnoredCallback superclass)
 * and prompts response handling only on successful response.
 */
public class SimpleCallback<T> extends IgnoredCallback<T> {

    protected void handleResponse(Response<T> response) {
        throw new RuntimeException("Stub! Override this method.");
    }

    @Override
    public void onResponse(Call<T> call, Response<T> response) {
        if (response.isSuccessful())
            handleResponse(response);
    }
}