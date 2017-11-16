package com.eulersbridge.isegoria.network;

import android.support.annotation.Nullable;

import com.squareup.moshi.FromJson;


@SuppressWarnings("unused")
class NullPrimitiveAdapter {

    @FromJson
    public int intFromJson(@Nullable Integer value) {
        if (value == null) {
            return 0;
        }
        return value;
    }

    @FromJson
    public boolean booleanFromJson(@Nullable Boolean value) {
        if (value == null) {
            return false;
        }
        return value;
    }

    @FromJson
    public double doubleFromJson(@Nullable Double value) {
        if (value == null) {
            return 0;
        }
        return value;
    }

    @FromJson
    public long longFromJson(@Nullable Long value) {
        if (value == null) {
            return 0;
        }
        return value;
    }
}
