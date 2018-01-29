package com.eulersbridge.isegoria.network.api.responses

import com.eulersbridge.isegoria.network.api.models.PollResult
import com.squareup.moshi.Json

data class PollResultsResponse (
    @Json(name = "nodeId")
    val id: Long,

    val pollId: Long,

    @Json(name = "answers")
    val results: List<PollResult>?
)