package com.eulersbridge.isegoria.models;

import com.squareup.moshi.Json;

import org.parceler.Transient;

import java.util.List;


@org.parceler.Parcel
public class Poll {

    @Json(name = "nodeId")
    public long id;

    public String creatorEmail;

    @Transient
    private Contact creator;

    public String question;

    @Json(name = "pollOptions")
    public List<PollOption> options;

    public boolean closed;

    public void setCreator(Contact creator) {
        this.creator = creator;
    }

    public Contact getCreator() {
        return creator;
    }

}
