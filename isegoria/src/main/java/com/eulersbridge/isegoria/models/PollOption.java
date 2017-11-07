package com.eulersbridge.isegoria.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Seb on 23/10/2017.
 */

public class PollOption implements Parcelable {

    private long id;
    private String text;
    private @Nullable String photoUrl;

    private long votersCount;
    private boolean hasVoted;

    PollOption(JSONObject jsonObject) {
        try {
            id = jsonObject.getLong("id");
            text = jsonObject.optString("txt");

            JSONObject photoObject = jsonObject.optJSONObject("photo");
            if (photoObject != null) {
                photoUrl = photoObject.optString("url");
            }

            votersCount = jsonObject.optLong("numOfVoters", 0);
            hasVoted = jsonObject.getBoolean("voted");

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private PollOption(Parcel in) {
        id = in.readLong();
        text = in.readString();
        photoUrl = in.readString();

        votersCount = in.readLong();

        hasVoted = (in.readInt() == 1);
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(id);
        parcel.writeString(text);
        parcel.writeString(photoUrl);

        parcel.writeLong(votersCount);

        parcel.writeInt(hasVoted? 1 : 0);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<PollOption> CREATOR = new Parcelable.Creator<PollOption>() {
        @Override
        public PollOption createFromParcel(Parcel in) {
            return new PollOption(in);
        }

        @Override
        public PollOption[] newArray(int size) {
            return new PollOption[size];
        }
    };

    public long getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    @Nullable
    public String getPhotoUrl() {
        return photoUrl;
    }

    public long getVotersCount() {
        return votersCount;
    }

    public boolean hasVoted() {
        return hasVoted;
    }
}
