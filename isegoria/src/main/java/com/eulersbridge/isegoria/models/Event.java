package com.eulersbridge.isegoria.models;

import com.eulersbridge.isegoria.network.Timestamp;
import com.squareup.moshi.Json;

import java.util.List;

@org.parceler.Parcel
public class Event {

    public String name;
    public String location;
    public String description;

    public String organizer;
    public String organizerEmail;

    @Json(name = "created")
    @Timestamp
    public long date;

    public List<Photo> photos;
}
