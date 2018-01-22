package com.eulersbridge.isegoria.network.api.models;

import com.squareup.moshi.Json;

public class Task {

    @Json(name = "taskId")
    public long id;

    public String action;
    public long xpValue;
}
