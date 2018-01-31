@file:JvmName("Util")

package com.eulersbridge.isegoria

import android.content.Context
import android.net.ConnectivityManager
import android.os.Build
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateUtils
import android.util.Patterns
import android.view.View
import android.widget.AdapterView
import android.widget.EditText
import android.widget.Spinner
import java.util.*

fun Context.isNetworkAvailable(): Boolean {
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetworkInfo = connectivityManager.activeNetworkInfo
    return activeNetworkInfo?.isConnectedOrConnecting ?: false
}

@JvmSynthetic
inline fun Spinner.onItemSelected(crossinline onItemSelected: (position: Int) -> Unit) {
    this.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onNothingSelected(p0: AdapterView<*>?) { }

        override fun onItemSelected(adapterView: AdapterView<*>, view: View, position: Int, id: Long) {
            onItemSelected(position)
        }
    }
}

@JvmSynthetic
inline fun EditText.onTextChanged(crossinline onTextChanged: (String?) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {
            onTextChanged(charSequence?.toString())
        }

        override fun afterTextChanged(editable: Editable?) {
        }
    })
}

@JvmName("Util")
fun Long.toDateString(context: Context): String {
    val date = Date(this)

    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        // Use built-in Android ICU4J API subset ("reduce your APK footprint")
        android.icu.text.DateFormat.getDateTimeInstance(android.icu.text.DateFormat.LONG, android.icu.text.DateFormat.SHORT).format(date)

    } else {
        val flags = DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_YEAR or DateUtils.FORMAT_SHOW_TIME or DateUtils.FORMAT_NO_MIDNIGHT or DateUtils.FORMAT_NO_NOON
        DateUtils.formatDateTime(context, date.time, flags)
    }
}

val String?.isValidEmail
get() = !isNullOrBlank() && Patterns.EMAIL_ADDRESS.matcher(this!!).matches()