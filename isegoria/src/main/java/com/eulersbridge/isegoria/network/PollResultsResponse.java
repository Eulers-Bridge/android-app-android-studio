package com.eulersbridge.isegoria.network;

import com.eulersbridge.isegoria.models.PollResult;
import com.squareup.moshi.Json;

import java.util.List;

public class PollResultsResponse {

    @Json(name = "nodeId")
    public long id;

    public long pollId;

    @Json(name = "answers")
    public List<PollResult> results;

}
