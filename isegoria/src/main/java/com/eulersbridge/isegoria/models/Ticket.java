package com.eulersbridge.isegoria.models;

import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Seb on 05/11/2017.
 */

public class Ticket {

    @SerializedName("ticketId")
    public long id;

    public long electionId;

    public String name;
    public String information;

    private String colour;
    public String code;
    public String logo;

    @SerializedName("numberOfSupporters")
    public long supporterCount;

    public String getColour() {
        if (!TextUtils.isEmpty(colour)) {
            return colour;

        } else {
            return "#000000";
        }
    }
}
