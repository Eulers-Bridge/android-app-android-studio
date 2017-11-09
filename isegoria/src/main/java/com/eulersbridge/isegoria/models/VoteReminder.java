package com.eulersbridge.isegoria.models;

import com.eulersbridge.isegoria.network.Timestamp;

public class VoteReminder {

    public String userEmail;
    public long electionId;
    public String location;

    @Timestamp
    public long date;

    public VoteReminder(String userEmail, long electionId, String location, @Timestamp long date) {
        this.userEmail = userEmail;
        this.electionId = electionId;
        this.location = location;
        this.date = date;
    }

}
