package com.eulersbridge.isegoria.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.eulersbridge.isegoria.utilities.TimeConverter;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Seb on 03/11/2017.
 */

public class Photo implements Parcelable {

    private int id;

    private String title;
    private String description;
    private String thumbnailUrl;
    private long dateTimestamp;

    private int likeCount = 0;
    private boolean hasInappropriateContent;

    public Photo(JSONObject jsonObject) {
        try {
            this.id = jsonObject.getInt("nodeId");
            this.title = jsonObject.getString("title");
            this.description = jsonObject.getString("description");
            this.thumbnailUrl = jsonObject.getString("url");

            this.dateTimestamp = TimeConverter.convertTimestampTimezone(jsonObject.getLong("date"));

            this.likeCount = jsonObject.optInt("numOfLikes", 0);
            this.hasInappropriateContent = jsonObject.getBoolean("inappropriateContent");

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private Photo(Parcel in) {
        id = in.readInt();
        title = in.readString();
        description = in.readString();
        thumbnailUrl = in.readString();

        dateTimestamp = in.readLong();

        likeCount = in.readInt();
        hasInappropriateContent = (in.readInt() == 1);
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(title);
        parcel.writeString(description);
        parcel.writeString(thumbnailUrl);

        parcel.writeLong(dateTimestamp);

        parcel.writeInt(likeCount);
        parcel.writeInt(hasInappropriateContent? 1 : 0);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<Photo> CREATOR = new Parcelable.Creator<Photo>() {
        @Override
        public Photo createFromParcel(Parcel in) {
            return new Photo(in);
        }

        @Override
        public Photo[] newArray(int size) {
            return new Photo[size];
        }
    };

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public boolean hasInappropriateContent() {
        return hasInappropriateContent;
    }

    public long getDateTimestamp() {
        return dateTimestamp;
    }
}
