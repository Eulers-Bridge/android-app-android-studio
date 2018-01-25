package com.eulersbridge.isegoria

import android.app.Activity
import android.app.ActivityManager
import android.graphics.BitmapFactory
import android.os.Build
import android.support.annotation.ColorInt
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager

//Change colour, use default title (app name)
fun Activity.setMultitaskColour(@ColorInt colour: Int) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        setMultitaskDescription(this, getString(R.string.app_name), colour)
}

private fun setMultitaskDescription(activity: Activity, title: String, @ColorInt colour: Int) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

        val icon = BitmapFactory.decodeResource(activity.resources, R.drawable.app_icon)

        activity.setTaskDescription(ActivityManager.TaskDescription(title, icon, colour))

        icon.recycle()
    }
}

var Activity.statusBarColour: Int?
    get() {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor

        } else {
            null
        }
    }
    set(colour) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && colour != null)
            window.statusBarColor = colour
    }

/**
 * @implNote Getter will always return false. There is no reliable method of checking for keyboard
 * visibility.
 */
var Activity.keyboardVisible: Boolean
    get() = false
    set(value) {
        if (value) {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)

        } else {
            currentFocus?.let {
                val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(currentFocus.windowToken, 0)
            }
        }
    }