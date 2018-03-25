@file:JvmName("Util")

package com.eulersbridge.isegoria

import android.content.Context
import android.net.ConnectivityManager

inline fun <reified T> Context.systemService(name: String): T? = getSystemService(name) as? T

fun Context.isNetworkAvailable(): Boolean {
    val connectivityManager = systemService<ConnectivityManager>(Context.CONNECTIVITY_SERVICE)
    return connectivityManager?.activeNetworkInfo?.isConnectedOrConnecting ?: false
}