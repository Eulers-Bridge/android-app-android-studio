package com.eulersbridge.isegoria.models;

import android.support.annotation.Nullable;

import com.squareup.moshi.Json;
@org.parceler.Parcel
public class PollOption {

    public long id;

    @Json(name = "txt")
    public String text;

    public Photo photo;

    @Json(name = "numOfVoters")
    @Nullable
    public Long votersCount;

    @Json(name = "voted")
    public boolean hasVoted;
}
