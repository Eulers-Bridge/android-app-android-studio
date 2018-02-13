package com.eulersbridge.isegoria.network

import android.arch.lifecycle.LiveData
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException

/**
 * LiveData wrapper for an OkHttp request call.
 */
internal class OkHttpLiveData(private val call: Call) : LiveData<Boolean>(), Callback {

    override fun onActive() {
        if (!call.isCanceled && !call.isExecuted)
            call.enqueue(this)
    }

    @Throws(IOException::class)
    override fun onResponse(call: Call, response: Response) {
        postValue(response.isSuccessful && response.body() != null)
    }

    override fun onFailure(call: Call, e: IOException) {
        postValue(false)
    }

    fun cancel() {
        if (!call.isCanceled)
            call.cancel()
    }
}