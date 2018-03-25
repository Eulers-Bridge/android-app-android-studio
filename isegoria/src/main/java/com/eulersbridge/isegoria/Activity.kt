@file:JvmName("ActivityUtil")

package com.eulersbridge.isegoria

import android.app.Activity
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager

fun Activity.setKeyboardVisible(visible: Boolean) {
    if (visible) {
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)

    } else {
        currentFocus?.let {
            val inputMethodManager = systemService<InputMethodManager>(Activity.INPUT_METHOD_SERVICE)
            inputMethodManager?.hideSoftInputFromWindow(currentFocus.windowToken, 0)
        }
    }
}