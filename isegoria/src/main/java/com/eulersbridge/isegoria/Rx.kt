package com.eulersbridge.isegoria

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.LiveDataReactiveStreams
import io.reactivex.*

fun <T> Flowable<T>.toLiveData(): LiveData<T> = LiveDataReactiveStreams.fromPublisher(this)

fun <T> Observable<T>.toLiveData(strategy: BackpressureStrategy) = this.toFlowable(strategy).toLiveData()

fun <T> Single<T>.toLiveData() = this.toFlowable().toLiveData()

fun <T> Completable.toLiveData() = this.toFlowable<T>().toLiveData()

fun Completable.toBooleanSingle(): Single<Boolean> = this.toSingleDefault(true).onErrorReturnItem(false)