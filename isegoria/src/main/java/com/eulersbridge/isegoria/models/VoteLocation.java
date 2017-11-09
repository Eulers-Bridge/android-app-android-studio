package com.eulersbridge.isegoria.models;

import com.squareup.moshi.Json;

public class VoteLocation {

    public long ownerId;

    @Json(name = "votingLocationId")
    public long id;

    public String name;
    public String information;
}
