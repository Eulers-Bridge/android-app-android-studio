package com.eulersbridge.isegoria.network.api.models;

import android.text.TextUtils;

import com.squareup.moshi.Json;

@SuppressWarnings("unused")
public class CandidateTicket {

    @Json(name = "ticketId")
    public long id;

    private String name;
    private String givenName;
    private String familyName;

    @Json(name = "numberOfSupporters")
    public String supportersCount;

    public String information;
    public String logo;

    private String colour;

    public String getName() {
        if (!TextUtils.isEmpty(name)) {
            return name;

        } else {
            return String.format("%s %s", givenName, familyName);
        }
    }

    public String getColour() {
        if (!TextUtils.isEmpty(colour)) {
            return colour;

        } else {
            return "#000000";
        }
    }
}
