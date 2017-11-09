package com.eulersbridge.isegoria.models;

import com.squareup.moshi.Json;

public class Badge {

    @Json(name = "badgeId")
    public long id;

    public String name;
    public String description;
    public int level;
    public int xpValue;
}
