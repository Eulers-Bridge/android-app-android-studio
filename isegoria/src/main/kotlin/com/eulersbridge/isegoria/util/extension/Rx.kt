package com.eulersbridge.isegoria.util.extension

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.LiveDataReactiveStreams
import io.reactivex.*
import io.reactivex.rxkotlin.Singles
import io.reactivex.rxkotlin.subscribeBy
import java.util.concurrent.TimeUnit

fun <T> Flowable<T>.toLiveData(): LiveData<T>
        = LiveDataReactiveStreams.fromPublisher(this)

fun <T> Observable<T>.toLiveData(strategy: BackpressureStrategy)
        = toFlowable(strategy).toLiveData()

fun <T> Single<T>.toLiveData()
        = toFlowable().toLiveData()

fun Completable.toBooleanSingle(): Single<Boolean>
        = toSingleDefault(true).onErrorReturnItem(false)

fun <T : Any> Single<T>.subscribeSuccess(onSuccess: (T) -> Unit)
        = subscribeBy(onSuccess = onSuccess, onError = { it.printStackTrace() })

fun <T> zipWithTimer(stream: Single<T>)
        = Singles.zip(stream, Single.timer(1500, TimeUnit.MILLISECONDS)) { t, _ -> t }