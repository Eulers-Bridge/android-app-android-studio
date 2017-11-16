package com.eulersbridge.isegoria.network;

import com.squareup.moshi.FromJson;
import com.squareup.moshi.ToJson;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * Convert Unix timestamps in JSON to a Unix timestamp in the user's local timezone.
 */
@SuppressWarnings("unused")
class TimestampAdapter {

    private static int getUserOffsetFromUtc() {
        Calendar calendar = Calendar.getInstance();
        TimeZone timeZone = calendar.getTimeZone();
        return timeZone.getOffset(0) / 1000;
    }

    /**
     * Convert a timestamp in the user's timezone to UTC
     */
    @ToJson
    long toJson(@Timestamp long timestamp) {
        return timestamp - getUserOffsetFromUtc();
    }

    /**
     * Convert a JSON timestamp to one in the user's timezone
     */
    @FromJson
    @Timestamp long fromJson(long timestamp) {
        return timestamp + getUserOffsetFromUtc();
    }

}