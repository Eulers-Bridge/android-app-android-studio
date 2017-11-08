package com.eulersbridge.isegoria.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Seb on 02/11/2017.
 */

@org.parceler.Parcel
public class NewsArticle {

    @SerializedName("articleId")
    public long id;

    public long institutionId;
    public String title;
    public String content;

    public List<Photo> photos;

    public long dateTimestamp;

    @SerializedName("likes")
    public int likeCount = 0;

    @SerializedName("creatorProfile")
    public UserProfile creator;

    @SerializedName("inappropriateContent")
    public boolean hasInappropriateContent;
}