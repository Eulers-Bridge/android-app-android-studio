package com.eulersbridge.isegoria.network.adapters

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.util.*

/**
 * Convert Unix timestamps in JSON to a Unix timestamp in the user's local timezone.
 */
class TimestampAdapter {
    private val userOffsetFromUtc: Int
        get() {
            return Calendar.getInstance().timeZone.getOffset(0) / 1000
        }

    /**
     * Convert a timestamp in the user's timezone to UTC
     */
    @ToJson
    internal fun toJson(@Timestamp timestamp: Long)
            = timestamp - userOffsetFromUtc

    /**
     * Convert a JSON timestamp to one in the user's timezone
     */
    @FromJson
    @Timestamp
    internal fun fromJson(timestamp: Long)
            = timestamp + userOffsetFromUtc

}