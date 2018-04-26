package com.eulersbridge.isegoria.auth

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText

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