package com.eulersbridge.isegoria.network.api.models;

import com.eulersbridge.isegoria.network.adapters.Timestamp;

@SuppressWarnings({"WeakerAccess", "CanBeFinal"})
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
