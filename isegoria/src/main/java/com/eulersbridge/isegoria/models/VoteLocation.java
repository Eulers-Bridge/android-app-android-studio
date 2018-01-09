package com.eulersbridge.isegoria.models;

import com.squareup.moshi.Json;

import org.parceler.Parcel;

@SuppressWarnings("unused")
@Parcel
public class VoteLocation {

    public long ownerId;

    @Json(name = "votingLocationId")
    public long id;

    public String name;
    public String information;

    @Override
    public String toString() {
        return name;
    }
}
