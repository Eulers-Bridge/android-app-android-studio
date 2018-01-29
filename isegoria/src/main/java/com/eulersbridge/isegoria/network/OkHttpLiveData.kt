package com.eulersbridge.isegoria.network

import android.arch.lifecycle.LiveData
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException

/**
 * LiveData wrapper for an OkHttp request call.
 */
internal class OkHttpLiveData(private val call: Call) : LiveData<String>(), Callback {

    override fun onActive() {
        if (!call.isCanceled && !call.isExecuted)
            call.enqueue(this)
    }

    @Throws(IOException::class)
    override fun onResponse(call: Call, response: Response) {
        value = if (response.isSuccessful) {
            response.body()?.toString() ?: ""

        } else {
            null
        }
    }

    override fun onFailure(call: Call, e: IOException) {
        value = null
    }

    fun cancel() {
        if (!call.isCanceled)
            call.cancel()
    }
}