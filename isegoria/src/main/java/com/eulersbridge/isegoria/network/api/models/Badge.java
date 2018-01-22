package com.eulersbridge.isegoria.network.api.models;

import com.squareup.moshi.Json;

@SuppressWarnings("unused")
public class Badge {

    @Json(name = "badgeId")
    public long id;

    public String name;
    public String description;
    public int level;
    public int xpValue;

}
