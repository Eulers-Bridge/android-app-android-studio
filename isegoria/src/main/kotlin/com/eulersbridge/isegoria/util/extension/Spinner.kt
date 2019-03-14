package com.eulersbridge.isegoria.util.extension

import android.view.View
import android.widget.AdapterView
import android.widget.Spinner


internal inline fun Spinner.onItemSelected(crossinline onItemSelected: (position: Int) -> Unit) {
    this.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onNothingSelected(p0: AdapterView<*>?) { }

        override fun onItemSelected(adapterView: AdapterView<*>, view: View, position: Int, id: Long) {
            onItemSelected(position)
        }
    }
}