package com.eulersbridge.isegoria.models;

import android.support.annotation.Nullable;

import com.squareup.moshi.Json;

public class Position {

    @Json(name = "positionId")
    public long id;

    @Json(name = "electionId")
    @Nullable
    public Long electionId;

    public String name;
    public String description;
}
