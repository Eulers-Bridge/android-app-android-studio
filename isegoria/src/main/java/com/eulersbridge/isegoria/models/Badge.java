package com.eulersbridge.isegoria.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Seb on 03/11/2017.
 */

public class Badge {

    @SerializedName("badgeId")
    public long id;

    public String name;
    public String description;
    public int level;
    public int xpValue;
}
