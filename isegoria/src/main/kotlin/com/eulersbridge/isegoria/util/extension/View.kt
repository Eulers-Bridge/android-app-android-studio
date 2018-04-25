@file:JvmName("ViewUtil")

package com.eulersbridge.isegoria.util.extension

import android.support.v7.widget.TooltipCompat
import android.view.View

fun View.setCompatTooltipText(tooltipText: CharSequence) {
    TooltipCompat.setTooltipText(this, tooltipText)
}