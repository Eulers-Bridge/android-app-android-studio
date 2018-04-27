package com.eulersbridge.isegoria.util.extension

import android.support.v4.app.Fragment

fun Fragment.runOnUiThread(action: () -> Unit) = activity?.runOnUiThread(action)