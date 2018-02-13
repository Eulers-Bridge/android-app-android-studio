package com.eulersbridge.isegoria.util.network;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Retrofit's `enqueue` requires a non-null callback to execute a given call.
 * This callback subclass does not act on response, but simply logs errors.
 */
public class IgnoredCallback<T> implements Callback<T> {
    @Override
    public void onResponse(Call<T> call, Response<T> response) {
        // Ignored
    }

    @Override
    public void onFailure(Call<T> call, Throwable t) {
        t.printStackTrace();
    }
}