package com.eulersbridge.isegoria.network.api.models;

import com.squareup.moshi.Json;

import java.util.List;


@org.parceler.Parcel
public class Poll {

    @Json(name = "nodeId")
    public long id;

    public String creatorEmail;

    public Contact creator;

    public String question;

    @Json(name = "pollOptions")
    public List<PollOption> options;

    public boolean closed;

    public Poll() {
        // Required empty constructor
    }

    /**
     * Copy constructor
     */
    public Poll(Poll poll) {
        this.id = poll.id;
        this.creatorEmail = poll.creatorEmail;
        this.creator = poll.creator;
        this.question = poll.question;
        this.options = poll.options;
        this.closed = poll.closed;
    }

}
