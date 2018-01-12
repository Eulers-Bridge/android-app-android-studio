package com.eulersbridge.isegoria.network.api.models;

import android.support.annotation.Nullable;

import com.eulersbridge.isegoria.network.adapters.Timestamp;
import com.squareup.moshi.Json;

import java.util.List;

@org.parceler.Parcel
public class NewsArticle {

    @Json(name = "articleId")
    public long id;

    @Nullable
    public Long institutionId;

    public String title;
    public String content;

    List<Photo> photos;

    @Json(name = "date")
    @Timestamp
    public long dateTimestamp;

    @Json(name = "likes")
    public int likeCount = 0;

    @Json(name = "creatorProfile")
    public Contact creator;

    @Json(name = "inappropriateContent")
    public boolean hasInappropriateContent;

    public @Nullable String getPhotoUrl() {
        if (photos != null && photos.size() > 0) {
            return photos.get(0).thumbnailUrl;

        } else {
            return null;
        }
    }
}