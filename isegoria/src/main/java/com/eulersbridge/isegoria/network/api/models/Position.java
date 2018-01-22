package com.eulersbridge.isegoria.network.api.models;

import android.support.annotation.Nullable;

import com.squareup.moshi.Json;

import org.parceler.Parcel;

@Parcel
public class Position {

    @Json(name = "positionId")
    public long id;

    @Json(name = "electionId")
    @Nullable
    public Long electionId;

    public String name;
    public String description;
}
