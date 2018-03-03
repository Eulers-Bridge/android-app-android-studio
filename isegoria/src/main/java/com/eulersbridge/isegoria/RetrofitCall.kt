package com.eulersbridge.isegoria

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

fun <T> Call<T>.enqueue() = this.enqueue({}, { it.printStackTrace() })

inline fun <T> Call<T>.onSuccess(crossinline success: (value: T) -> Unit) {
    this.enqueue({
        it.takeIf { it.isSuccessful }?.body()?.let { body ->
            success(body)
        }
    })
}

inline fun <T> Call<T>.enqueue(crossinline success: (response: Response<T>) -> Unit = {},
                               crossinline failure: (t: Throwable) -> Unit = {}) {
    enqueue(object : Callback<T> {
        override fun onResponse(call: Call<T>?, response: Response<T>) = success(response)

        override fun onFailure(call: Call<T>?, t: Throwable) = failure(t)
    })
}