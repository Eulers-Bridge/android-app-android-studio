@file:JvmName("Util")

package com.eulersbridge.isegoria

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.LiveData
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
import com.securepreferences.SecurePreferences
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

fun <T> LifecycleOwner.observe(data: LiveData<T>?, onChanged: (value: T?) -> Unit) {
    data?.observe(this, android.arch.lifecycle.Observer {
        onChanged(it)
    })
}

fun <T> Call<T>.enqueue() = this.enqueue({}, { it.printStackTrace() })

inline fun <T> Call<T>.onSuccess(crossinline success: (value: T) -> Unit) {
    this.enqueue({
        it.takeIf { it.isSuccessful }?.body()?.let { body ->
            success(body)
        }
    })
}

inline fun <T> Call<T>.enqueue(crossinline success: (response: Response<T>) -> Unit = {},
                        crossinline failure: (t: Throwable) -> Unit = {}) {
    enqueue(object : Callback<T> {
        override fun onResponse(call: Call<T>?, response: Response<T>) = success(response)

        override fun onFailure(call: Call<T>?, t: Throwable) = failure(t)
    })
}

inline fun SecurePreferences.edit(action: SecurePreferences.Editor.() -> Unit) {
    val editor = edit()
    action(editor)
    editor.apply()
}

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

@JvmName("StringUtil")
fun notificationChannelIDFromName(name: String): String {
    return name.toLowerCase().replace(" ", "_")
}

val String?.isValidEmail
get() = !isNullOrBlank() && Patterns.EMAIL_ADDRESS.matcher(this!!).matches()