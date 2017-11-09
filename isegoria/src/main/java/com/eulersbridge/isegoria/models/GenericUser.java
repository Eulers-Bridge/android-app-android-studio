package com.eulersbridge.isegoria.models;

import android.support.annotation.Nullable;

import com.squareup.moshi.Json;

public class GenericUser {

    public String gender;
    public String nationality;

    public String email;
    public String givenName;
    public String familyName;

    @Nullable
    public Long institutionId;

    public long level;
    public long experience;

    @Json(name = "numOfCompTasks")
    public long completedTasksCount;

    @Json(name = "numOfCompBadges")
    public long completedBadgesCount;

    @Json(name = "profilePhoto")
    public String profilePhotoURL;

    public String getFullName() {
        return String.format("%s %s", givenName, familyName);
    }

}
