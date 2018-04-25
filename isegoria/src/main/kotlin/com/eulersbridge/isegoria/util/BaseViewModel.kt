package com.eulersbridge.isegoria.util

import android.arch.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.addTo

open class BaseViewModel : ViewModel() {

    fun Disposable.addToDisposable() = this.addTo(compositeDisposable)

    private val compositeDisposable = CompositeDisposable()

    override fun onCleared() {
        compositeDisposable.dispose()
    }

}