package com.eulersbridge.isegoria.network;

import android.support.annotation.Nullable;

import com.squareup.moshi.FromJson;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.JsonReader;
import com.squareup.moshi.JsonWriter;
import com.squareup.moshi.ToJson;

import java.io.IOException;

/**
 * Needed to handle cases of null IDs for users/contacts/institutions/etc.
 */
class LenientLongAdapter extends JsonAdapter<Long> {

    @FromJson
    public Long fromJson(JsonReader reader) throws IOException {

        try {
            JsonReader.Token token = reader.peek();

            if (token == JsonReader.Token.NULL) {
                reader.nextNull();
                return 0L;

            } else {
                return reader.nextLong();
            }
        } catch (Exception e) {
            return 0L;
        }
    }

    @ToJson
    public void toJson(JsonWriter writer, @Nullable Long value) throws IOException {
        writer.value(value);
    }
}