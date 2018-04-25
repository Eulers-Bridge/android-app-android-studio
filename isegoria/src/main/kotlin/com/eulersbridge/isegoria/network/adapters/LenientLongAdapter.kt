package com.eulersbridge.isegoria.network.adapters

import com.squareup.moshi.*
import java.io.IOException

/**
 * Needed to handle cases of null IDs for users/contacts/institutions/etc.
 */
class LenientLongAdapter : JsonAdapter<Long>() {

    @FromJson
    @Throws(IOException::class)
    override fun fromJson(reader: JsonReader): Long? {

        return try {
            val token = reader.peek()

            if (token == JsonReader.Token.NULL) {
                reader.nextNull<Any>()
                0L

            } else {
                reader.nextLong()
            }
        } catch (e: Exception) {
            0L
        }
    }

    @ToJson
    @Throws(IOException::class)
    override fun toJson(writer: JsonWriter, value: Long?) {
        writer.value(value)
    }
}