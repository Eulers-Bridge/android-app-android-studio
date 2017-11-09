package com.eulersbridge.isegoria.models;

import com.squareup.moshi.Json;

import org.parceler.Transient;

import java.util.ArrayList;


@org.parceler.Parcel
public class Poll {

    @Json(name = "nodeId")
    public long id;

    public String creatorEmail;

    @Transient
    private Contact creator;

    public String question;

    @Json(name = "pollOptions")
    public ArrayList<PollOption> options;

    public void setCreator(Contact creator) {
        this.creator = creator;
    }

    public Contact getCreator() {
        return creator;
    }

}
