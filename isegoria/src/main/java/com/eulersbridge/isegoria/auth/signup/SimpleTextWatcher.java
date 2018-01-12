package com.eulersbridge.isegoria.auth.signup;

import android.text.Editable;
import android.text.TextWatcher;

/**
 * Convenience wrapper for TextWatcher, to avoid having to define an anonymous class
 * overriding all three methods (before../on../after..) when only one is used.
 */
class SimpleTextWatcher implements TextWatcher {

    private final Consumer<CharSequence> callbackConsumer;

    SimpleTextWatcher(Consumer<CharSequence> callbackConsumer) {
        this.callbackConsumer = callbackConsumer;
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
        // Ignored, unused
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
        if (callbackConsumer != null)
            callbackConsumer.accept(charSequence);
    }

    @Override
    public void afterTextChanged(Editable editable) {
        // Ignored, unused
    }
}
