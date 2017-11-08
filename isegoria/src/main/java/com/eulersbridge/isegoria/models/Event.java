package com.eulersbridge.isegoria.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

@org.parceler.Parcel
public class Event {

    public String name;
    public String location;
    public String description;

    public String organizer;
    public String organizerEmail;

    @SerializedName("created")
    public long date;

    public List<Photo> photos;
}
