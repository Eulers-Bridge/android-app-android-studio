package com.eulersbridge.isegoria.network.api.responses;

import com.eulersbridge.isegoria.network.api.models.PollResult;
import com.squareup.moshi.Json;

import java.util.List;

public class PollResultsResponse {

    @Json(name = "nodeId")
    public long id;

    public long pollId;

    @Json(name = "answers")
    public List<PollResult> results;

}
