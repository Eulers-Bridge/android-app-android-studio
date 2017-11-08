package com.eulersbridge.isegoria.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Seb on 03/11/2017.
 */

@org.parceler.Parcel
public class Photo {

    @SerializedName("nodeId")
    public int id;

    public String title;
    public String description;

    @SerializedName("url")
    public String thumbnailUrl;

    @SerializedName("date")
    public long dateTimestamp;

    @SerializedName("numOfLikes")
    public int likeCount = 0;

    @SerializedName("inappropriateContent")
    public boolean hasInappropriateContent;
}
