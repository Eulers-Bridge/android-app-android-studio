package com.eulersbridge.isegoria.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Seb on 23/10/2017.
 */

@org.parceler.Parcel
public class PollOption {

    public long id;

    @SerializedName("txt")
    public String text;

    public Photo photo;

    @SerializedName("numOfVoters")
    public long votersCount;

    @SerializedName("voted")
    public boolean hasVoted;
}
