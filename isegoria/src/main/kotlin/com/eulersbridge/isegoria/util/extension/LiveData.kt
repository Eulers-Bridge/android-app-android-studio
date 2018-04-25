@file:JvmName("LiveData")

package com.eulersbridge.isegoria.util.extension

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Transformations

fun <T, O> LiveData<T>.map(function: (T) -> O): LiveData<O> = Transformations.map(this, function)