package com.eulersbridge.isegoria

import android.content.Context
import android.os.Build
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.text.format.DateUtils
import android.util.Patterns
import android.widget.EditText
import java.util.*

fun EditText.onTextChanged(onTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {
            onTextChanged(charSequence.toString())
        }

        override fun afterTextChanged(editable: Editable?) {
        }
    })
}

fun String.fromTimestamp(context: Context, timestamp: Long): String {
    val date = Date(timestamp)

    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        // Use built-in Android ICU4J API subset ("reduce your APK footprint")
        android.icu.text.DateFormat.getDateTimeInstance(android.icu.text.DateFormat.LONG, android.icu.text.DateFormat.SHORT).format(date)

    } else {
        val flags = DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_YEAR or DateUtils.FORMAT_SHOW_TIME or DateUtils.FORMAT_NO_MIDNIGHT or DateUtils.FORMAT_NO_NOON
        DateUtils.formatDateTime(context, date.getTime(), flags)
    }
}

val String?.isValidEmail
get() = !TextUtils.isEmpty(this) && Patterns.EMAIL_ADDRESS.matcher(this!!).matches()