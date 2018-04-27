package com.eulersbridge.isegoria.util.ui

import android.content.Context

interface TitledFragment {
    fun getTitle(context: Context?): String?
}