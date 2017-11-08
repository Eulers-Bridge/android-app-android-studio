package com.eulersbridge.isegoria.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Seb on 04/11/2017.
 */

public class Position {

    @SerializedName("positionId")
    public long id;

    @SerializedName("electionId")
    public long electionId;

    public String name;
    public String description;
}
