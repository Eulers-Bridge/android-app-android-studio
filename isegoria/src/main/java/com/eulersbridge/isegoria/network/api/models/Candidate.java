package com.eulersbridge.isegoria.network.api.models;

import com.squareup.moshi.Json;

import org.parceler.Parcel;

@Parcel
public class Candidate {

    @Json(name = "candidateId")
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
