package com.eulersbridge.isegoria.auth.signup

import android.view.View
import android.widget.AdapterView

internal open class SimpleOnItemSelectedListener : AdapterView.OnItemSelectedListener {

    internal open fun onItemSelected(position: Int) {
        throw RuntimeException("Stub! Override this method.")
    }

    override fun onItemSelected(adapterView: AdapterView<*>, view: View, position: Int, id: Long) {
        onItemSelected(position)
    }

    override fun onNothingSelected(adapterView: AdapterView<*>) {
        // Ignored, unused
    }
}
