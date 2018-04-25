package com.eulersbridge.isegoria.util.data

import android.arch.lifecycle.LiveData

class SingleLiveData<T>(value: T) : LiveData<T>() {
    init {
        this.value = value
    }
}