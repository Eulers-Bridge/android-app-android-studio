package com.eulersbridge.isegoria.models;

import android.support.annotation.Nullable;

import com.squareup.moshi.Json;

import org.parceler.Parcel;

@Parcel
public class Contact extends GenericUser {

    @Json(name = "numOfContacts")
    @Nullable
    public Long contactsCount;

    @Json(name = "totalTasks")
    @Nullable
    public Long totalTasksCount;

    @Json(name = "totalBadges")
    @Nullable
    public Long totalBadgesCount;

    @Json(name = "userId")
    @Nullable
    public Long id;

}
