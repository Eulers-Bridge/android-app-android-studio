package com.eulersbridge.isegoria.models;

import android.support.annotation.Nullable;

import com.squareup.moshi.Json;

import org.parceler.Transient;

@org.parceler.Parcel
public class PollOption {

    public long id;

    @Json(name = "txt")
    public String text;

    public Photo photo;

    @Json(name = "numOfVoters")
    @Nullable
    public Long votersCount;

    @Json(name = "voted")
    public boolean hasVoted;

    @Transient
    private transient PollResult result;

    public void setResult(PollResult result) {
        this.result = result;
    }

    public PollResult getResult() {
        return result;
    }
}
