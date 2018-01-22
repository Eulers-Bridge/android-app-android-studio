package com.eulersbridge.isegoria.network.api.models;

import com.squareup.moshi.Json;

public class PollResult {

    @Json(name = "answer")
    public long id;

    public long count;

}
