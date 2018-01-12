package com.eulersbridge.isegoria.util;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Patterns;

import java.util.Date;

public final class Strings {

    private Strings() {
        // Hide implicit public constructor
    }

    public static boolean isValidEmail(@Nullable String emailAddress) {
        return !TextUtils.isEmpty(emailAddress) && Patterns.EMAIL_ADDRESS.matcher(emailAddress).matches();
    }

    public static String notificationChannelIDFromName(@NonNull String name) {
        return name.toLowerCase().replace(" ","_");
    }

    public static String fromTimestamp(Context context, long timestamp) {
        Date date = new Date(timestamp);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // Use built-in Android ICU4J API subset ("reduce your APK footprint")
            return android.icu.text.DateFormat.getDateTimeInstance(android.icu.text.DateFormat.LONG, android.icu.text.DateFormat.SHORT).format(date);

        } else {
            int flags = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_NO_MIDNIGHT | DateUtils.FORMAT_NO_NOON;
            return DateUtils.formatDateTime(context, date.getTime(), flags);
        }
    }

}
