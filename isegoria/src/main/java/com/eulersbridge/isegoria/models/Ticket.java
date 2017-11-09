package com.eulersbridge.isegoria.models;

import android.text.TextUtils;

import com.squareup.moshi.Json;

public class Ticket {

    @Json(name = "ticketId")
    public long id;

    public long electionId;

    public String name;
    public String information;

    private String colour;
    public String code;
    public String logo;

    @Json(name = "numberOfSupporters")
    public long supporterCount;

    public String getColour() {
        if (!TextUtils.isEmpty(colour)) {
            return colour;

        } else {
            return "#000000";
        }
    }
}
