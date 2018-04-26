package com.eulersbridge.isegoria.network.api.response

import com.eulersbridge.isegoria.network.api.model.PollResult
import com.squareup.moshi.Json

data class PollResultsResponse (
    @field:Json(name = "nodeId")
    val id: Long,

    val pollId: Long,

    @field:Json(name = "answers")
    val results: List<PollResult>
)