package com.eulersbridge.isegoria.network;

import android.arch.lifecycle.LiveData;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;

/**
 * LiveData wrapper for an AWS Transfer.
 */
class AWSTransferLiveData extends LiveData<TransferState> implements TransferListener {

    private final TransferObserver transfer;

    AWSTransferLiveData(TransferObserver transfer) {
        this.transfer = transfer;
        this.transfer.setTransferListener(this);
    }

    @Override
    protected void onActive() {

    }

    @Override
    public void onStateChanged(int id, TransferState state) {
        setValue(state);
    }

    @Override
    public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {

    }

    @Override
    public void onError(int id, Exception ex) {

    }
}
