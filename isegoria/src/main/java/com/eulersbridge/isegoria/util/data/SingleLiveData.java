package com.eulersbridge.isegoria.util.data;

import android.arch.lifecycle.LiveData;

public class SingleLiveData<T> extends LiveData<T> {

    public SingleLiveData(T value) {
        setValue(value);
    }

}
