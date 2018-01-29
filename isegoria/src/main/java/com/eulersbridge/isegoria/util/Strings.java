package com.eulersbridge.isegoria.util;

import android.support.annotation.NonNull;

public final class Strings {

    public static String notificationChannelIDFromName(@NonNull String name) {
        return name.toLowerCase().replace(" ","_");
    }

}
