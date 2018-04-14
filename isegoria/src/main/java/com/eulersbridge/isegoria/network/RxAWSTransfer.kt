package com.eulersbridge.isegoria.network

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import io.reactivex.Completable
import java.lang.Exception

fun TransferObserver.toCompletable(): Completable {
    return Completable.create { emitter ->
        this.setTransferListener(object : TransferListener {
            override fun onProgressChanged(id: Int, bytesCurrent: Long, bytesTotal: Long) { }

            override fun onStateChanged(id: Int, state: TransferState?) {
                if (emitter.isDisposed)
                    return

                if (state == TransferState.COMPLETED) {
                    emitter.onComplete()
                } else if (state == TransferState.FAILED) {
                    emitter.onError(Throwable("transfer failed"))
                }
            }

            override fun onError(id: Int, ex: Exception?) {
                if (ex != null && !emitter.isDisposed)
                    emitter.onError(ex)
            }
        })
    }
}