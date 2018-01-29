package com.eulersbridge.isegoria.network.api.responses

import com.eulersbridge.isegoria.network.api.models.Poll

data class PollsResponse (
    val polls: List<Poll>?,
    val totalPolls: Long = 0
)