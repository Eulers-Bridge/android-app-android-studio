package com.eulersbridge.isegoria.util.data;

import android.arch.lifecycle.LiveData;

public class FixedData<T> extends LiveData<T> {

    public FixedData(T value) {
        setValue(value);
    }

}
