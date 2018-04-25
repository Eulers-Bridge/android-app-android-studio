package com.eulersbridge.isegoria.util.extension

import com.securepreferences.SecurePreferences

inline fun SecurePreferences.edit(action: SecurePreferences.Editor.() -> Unit) {
    val editor = edit()
    action(editor)
    editor.apply()
}