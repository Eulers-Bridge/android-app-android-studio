package com.eulersbridge.isegoria

import android.arch.lifecycle.LiveDataReactiveStreams
import io.reactivex.*

fun <T> Flowable<T>.toLiveData() = LiveDataReactiveStreams.fromPublisher(this)

fun <T> Observable<T>.toLiveData(strategy: BackpressureStrategy) = this.toFlowable(strategy).toLiveData()

fun <T> Single<T>.toLiveData() = this.toFlowable().toLiveData()

fun <T> Completable.toLiveData() = this.toFlowable<T>().toLiveData()