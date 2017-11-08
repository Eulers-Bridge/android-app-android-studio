package com.eulersbridge.isegoria.models;

import com.google.gson.annotations.SerializedName;

import org.parceler.Transient;

import java.util.ArrayList;

/**
 * Created by Seb on 04/11/2017.
 */

@org.parceler.Parcel
public class Poll {

    @SerializedName("nodeId")
    public long id;

    public String creatorEmail;

    @Transient
    private UserProfile creator;

    public String question;

    @SerializedName("pollOptions")
    public ArrayList<PollOption> options;

    public void setCreator(UserProfile creator) {
        this.creator = creator;
    }

    public UserProfile getCreator() {
        return creator;
    }

}
