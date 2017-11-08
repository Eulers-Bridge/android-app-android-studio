package com.eulersbridge.isegoria.models;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

/**
 * Created by Seb on 03/11/2017.
 */

@Parcel
public class PhotoAlbum {

    @SerializedName("nodeId")
    public int id;

    public String name;
    public String description;
    public String location;

    @SerializedName("thumbNailUrl")
    public String thumbnailPhotoUrl;

}
