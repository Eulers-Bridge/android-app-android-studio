@file:JvmName("StringUtil")

package com.eulersbridge.isegoria

import android.content.Context
import android.os.Build
import android.text.format.DateUtils
import java.util.*

fun Long.toDateString(context: Context): String {
    val date = Date(this)

    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        // Use built-in Android ICU4J API subset ("reduce your APK footprint")
        android.icu.text.DateFormat.getDateTimeInstance(
            android.icu.text.DateFormat.LONG,
            android.icu.text.DateFormat.SHORT
        ).format(date)

    } else {
        val flags =
                DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_YEAR or DateUtils.FORMAT_SHOW_TIME or DateUtils.FORMAT_NO_MIDNIGHT or DateUtils.FORMAT_NO_NOON
        DateUtils.formatDateTime(context, date.time, flags)
    }
}

fun notificationChannelIDFromName(name: String)
        = name.toLowerCase().replace(" ", "_")