package com.eulersbridge.isegoria.models;

import com.eulersbridge.isegoria.network.Timestamp;
import com.squareup.moshi.Json;

@org.parceler.Parcel
public class Photo {

    @Json(name = "nodeId")
    public int id;

    public String title;
    public String description;

    @Json(name = "url")
    public String thumbnailUrl;

    @Json(name = "date")
    @Timestamp
    public long dateTimestamp;

    @Json(name = "numOfLikes")
    public int likeCount = 0;

    @Json(name = "inappropriateContent")
    public boolean hasInappropriateContent;
}
