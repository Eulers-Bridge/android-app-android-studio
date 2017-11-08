package com.eulersbridge.isegoria.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Anthony on 24/03/2015.
 */
public class VoteLocation {

    public long ownerId;

    @SerializedName("votingLocationId")
    public long id;

    public String name;
    public String information;
}
