package com.eulersbridge.isegoria

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.LiveDataReactiveStreams
import io.reactivex.*
import io.reactivex.rxkotlin.subscribeBy

fun <T> Flowable<T>.toLiveData(): LiveData<T> = LiveDataReactiveStreams.fromPublisher(this)

fun <T> Observable<T>.toLiveData(strategy: BackpressureStrategy) = toFlowable(strategy).toLiveData()

fun <T> Single<T>.toLiveData() = toFlowable().toLiveData()

fun <T> Completable.toLiveData() = toFlowable<T>().toLiveData()

fun Completable.toBooleanSingle(): Single<Boolean> = toSingleDefault(true).onErrorReturnItem(false)

fun <T : Any> Single<T>.subscribeSuccess(onSuccess: (T) -> Unit) = subscribeBy(onSuccess = onSuccess, onError = { it.printStackTrace() })