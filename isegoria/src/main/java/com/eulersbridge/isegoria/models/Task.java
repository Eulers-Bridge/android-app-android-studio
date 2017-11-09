package com.eulersbridge.isegoria.models;

import com.squareup.moshi.Json;

public class Task {

    @Json(name = "taskId")
    public long id;

    public String action;
    public long xpValue;
}
