package com.eulersbridge.isegoria.util.data;

import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RetrofitLiveData<T> extends LiveData<T> implements Callback<T> {

    private final @NonNull Call<T> call;

    public RetrofitLiveData(@NonNull Call<T> call) {
        this.call = call;
    }

    @Override
    protected void onActive() {
        if (!call.isCanceled() && !call.isExecuted())
            call.enqueue(this);
    }


    @Override
    public void onResponse(Call<T> call, Response<T> response) {
        if (response.isSuccessful()) {
            postValue(response.body());

        } else {
            postValue(null);
        }
    }

    @Override
    public void onFailure(Call<T> call, Throwable t) {
        setValue(null);
    }

    public void cancel() {
        if (!call.isCanceled())
            call.cancel();
    }
}
