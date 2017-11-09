package com.eulersbridge.isegoria.models;

import com.squareup.moshi.Json;

import org.parceler.Parcel;

@Parcel
public class PhotoAlbum {

    @Json(name = "nodeId")
    public int id;

    public String name;
    public String description;
    public String location;

    @Json(name = "thumbNailUrl")
    public String thumbnailPhotoUrl;

}
