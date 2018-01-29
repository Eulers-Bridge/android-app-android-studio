package com.eulersbridge.isegoria.network

import android.arch.lifecycle.LiveData

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState

/**
 * LiveData wrapper for an AWS Transfer.
 */
internal class AWSTransferLiveData(private val transfer: TransferObserver) : LiveData<TransferState>(), TransferListener {

    init {
        this.transfer.setTransferListener(this)
    }

    override fun onStateChanged(id: Int, state: TransferState) {
        value = state
    }

    override fun onProgressChanged(id: Int, bytesCurrent: Long, bytesTotal: Long) { }

    override fun onError(id: Int, ex: Exception) { }
}
