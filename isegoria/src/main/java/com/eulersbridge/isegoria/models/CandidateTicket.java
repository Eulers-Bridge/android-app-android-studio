package com.eulersbridge.isegoria.models;

import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Seb on 04/11/2017.
 */

public class CandidateTicket {

    @SerializedName("ticketId")
    public long id;

    private String name;
    private String givenName;
    private String familyName;

    @SerializedName("numberOfSupporters")
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
