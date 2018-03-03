package com.eulersbridge.isegoria

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer

fun <T> LifecycleOwner.observe(data: LiveData<T>?, onChanged: (value: T?) -> Unit) {
    data?.observe(this, android.arch.lifecycle.Observer {
        onChanged(it)
    })
}

/**
 * Convenience function to map an optional boolean to a non-null boolean value
 */
inline fun <T> LifecycleOwner.observeBoolean(data: LiveData<T>, crossinline onChanged: (value: Boolean) -> Unit) {
    data.observe(this, Observer {
        onChanged(it == true)
    })
}