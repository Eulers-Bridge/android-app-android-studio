package com.eulersbridge.isegoria.util.data

import android.arch.lifecycle.LiveData

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RetrofitLiveData<T>(private val call: Call<T>) : LiveData<T>(), Callback<T> {

    override fun onActive() {
        if (!call.isCanceled && !call.isExecuted)
            call.enqueue(this)
    }

    override fun onResponse(call: Call<T>, response: Response<T>) {
        value = if (response.isSuccessful) {
            response.body()

        } else {
            null
        }
    }

    override fun onFailure(call: Call<T>, t: Throwable) {
        value = null
    }

    fun cancel() {
        if (!call.isCanceled)
            call.cancel()
    }
}
