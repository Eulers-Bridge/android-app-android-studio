package com.eulersbridge.isegoria.network;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Seb on 07/11/2017.
 */

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
