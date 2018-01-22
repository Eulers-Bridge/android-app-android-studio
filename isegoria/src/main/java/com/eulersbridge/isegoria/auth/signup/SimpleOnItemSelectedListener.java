package com.eulersbridge.isegoria.auth.signup;

import android.view.View;
import android.widget.AdapterView;

class SimpleOnItemSelectedListener implements AdapterView.OnItemSelectedListener {

    void onItemSelected(int position) {
        throw new RuntimeException("Stub! Override this method.");
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
        onItemSelected(position);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        // Ignored, unused
    }
}
