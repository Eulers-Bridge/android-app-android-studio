package com.eulersbridge.isegoria.util.network

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Retrofit's `enqueue` requires a non-null callback to execute a given call.
 * This callback subclass does not act on response, but simply logs errors.
 */
open class IgnoredCallback<T> : Callback<T> {

    override fun onResponse(call: Call<T>, response: Response<T>) {
        // Ignored
    }

    override fun onFailure(call: Call<T>, t: Throwable) {
        t.printStackTrace()
    }
}
