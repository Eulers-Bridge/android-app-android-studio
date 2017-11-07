package com.eulersbridge.isegoria.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.eulersbridge.isegoria.utilities.TimeConverter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Seb on 02/11/2017.
 */

public class NewsArticle implements Parcelable {

    private long id;
    private long institutionId;

    private String title;
    private String content;
    private String photoURL;

    private long dateTimestamp;

    private int likeCount = 0;

    private User creator;

    private boolean hasInappropriateContent;

    public NewsArticle(JSONObject jsonObject) {
        try {
            id = jsonObject.getLong("articleId");
            institutionId = jsonObject.getLong("institutionId");
            title = jsonObject.getString("title");
            content = jsonObject.getString("content");

            JSONArray photos = jsonObject.getJSONArray("photos");
            if (photos != null && photos.length() > 0) {
                photoURL = photos.getJSONObject(0).getString("url");
            }

            likeCount = jsonObject.optInt("likes");
            dateTimestamp = TimeConverter.convertTimestampTimezone(jsonObject.getLong("date"));

            JSONObject userObject = jsonObject.getJSONObject("creatorProfile");
            creator = new User(userObject);
            hasInappropriateContent = jsonObject.getBoolean("inappropriateContent");

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private NewsArticle(Parcel in) {
        id = in.readLong();
        institutionId = in.readLong();
        title = in.readString();
        content = in.readString();

        dateTimestamp = TimeConverter.convertTimestampTimezone(in.readLong());

        photoURL = in.readString();

        likeCount = in.readInt();

        creator = new User(in.readParcelable(User.class.getClassLoader()));
        hasInappropriateContent = (in.readInt() == 1);
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(id);
        parcel.writeLong(institutionId);
        parcel.writeString(title);
        parcel.writeString(content);
        parcel.writeLong(dateTimestamp);
        parcel.writeString(photoURL);
        parcel.writeInt(likeCount);
        parcel.writeParcelable(creator, 0);
        parcel.writeInt(hasInappropriateContent? 1 : 0);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<NewsArticle> CREATOR = new Parcelable.Creator<NewsArticle>() {
        @Override
        public NewsArticle createFromParcel(Parcel in) {
            return new NewsArticle(in);
        }

        @Override
        public NewsArticle[] newArray(int size) {
            return new NewsArticle[size];
        }
    };

    public long getId() {
        return id;
    }

    public long getInstitutionId() {
        return institutionId;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public long getDateTimestamp() {
        return dateTimestamp;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public User getCreator() {
        return creator;
    }

    public String getPhotoURL() {
        return photoURL;
    }

    public boolean hasInappropriateContent() {
        return hasInappropriateContent;
    }
}
