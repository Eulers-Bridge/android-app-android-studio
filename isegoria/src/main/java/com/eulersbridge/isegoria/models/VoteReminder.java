package com.eulersbridge.isegoria.models;

/**
 * Created by Seb on 07/11/2017.
 */

public class VoteReminder {

    public String userEmail;
    public long electionId;
    public String location;
    public long date;

    public VoteReminder(String userEmail, long electionId, String location, long date) {
        this.userEmail = userEmail;
        this.electionId = electionId;
        this.location = location;
        this.date = date;
    }

}
