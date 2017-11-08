package com.eulersbridge.isegoria.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Seb on 04/11/2017.
 */

public class Candidate {

    @SerializedName("candidateId")
    public long id;
    public long ticketId;
    public long positionId;
    public long userId;

    public String familyName;
    public String givenName;
    public String policyStatement;
    public String information;

    public String getName() {
        return String.format("%s %s", givenName, familyName);
    }

}
