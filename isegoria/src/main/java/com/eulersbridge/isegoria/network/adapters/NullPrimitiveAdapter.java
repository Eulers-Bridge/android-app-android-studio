package com.eulersbridge.isegoria.network.adapters;

import android.support.annotation.Nullable;

import com.squareup.moshi.FromJson;


@SuppressWarnings("unused")
public class NullPrimitiveAdapter {

    @FromJson
    public int intFromJson(@Nullable Integer value) {
        return value == null? 0 : value;
    }

    @FromJson
    public boolean booleanFromJson(@Nullable Boolean value) {
        return value == null? false : value;
    }

    @FromJson
    public double doubleFromJson(@Nullable Double value) {
        return value == null? 0 : value;
    }

    @FromJson
    public long longFromJson(@Nullable Long value) {
        return value == null? 0 : value;
    }
}
