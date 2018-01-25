package com.eulersbridge.isegoria.util.network

import retrofit2.Call
import retrofit2.Response

/**
 * A simple Retrofit callback for a call `enqueue` that logs errors (via IgnoredCallback superclass)
 * and prompts response handling only on successful response.
 */
open class SimpleCallback<T> : IgnoredCallback<T>() {

    protected open fun handleResponse(response: Response<T>) {
        throw RuntimeException("Stub! Override this method.")
    }

    override fun onResponse(call: Call<T>, response: Response<T>) {
        if (response.isSuccessful)
            handleResponse(response)
    }
}
