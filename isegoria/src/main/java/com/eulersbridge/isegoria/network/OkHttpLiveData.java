package com.eulersbridge.isegoria.network;

import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * LiveData wrapper for an OkHttp request call.
 */
class OkHttpLiveData extends LiveData<String> implements Callback {

    private final @NonNull Call call;

    OkHttpLiveData(@NonNull Call call) {
        this.call = call;
    }

    @Override
    protected void onActive() {
        if (!call.isCanceled() && !call.isExecuted())
            call.enqueue(this);
    }

    @Override
    public void onResponse(Call call, Response response) throws IOException {
        if (response.isSuccessful()) {
            ResponseBody body = response.body();
            postValue(body == null? "" : body.toString());

        } else {
            postValue(null);
        }
    }

    @Override
    public void onFailure(Call call, IOException e) {
        setValue(null);
    }

    @SuppressWarnings("unused")
    public void cancel() {
        if (!call.isCanceled())
            call.cancel();
    }

}
