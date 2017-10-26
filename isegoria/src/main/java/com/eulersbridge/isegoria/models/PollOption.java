package com.eulersbridge.isegoria.models;

import android.support.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Seb on 23/10/2017.
 */

public class PollOption {

    private long id;
    private String text;
    private @Nullable String photoUrl;

    private long votersCount;
    private boolean hasVoted;

    public PollOption(JSONObject jsonObject) {
        try {
            id = jsonObject.getLong("id");
            text = jsonObject.optString("txt");

            JSONObject photoObject = jsonObject.optJSONObject("photo");
            if (photoObject != null) {
                photoUrl = photoObject.optString("url");
            }

            votersCount = jsonObject.optLong("numOfVoters");
            hasVoted = jsonObject.getBoolean("voted");

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

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
