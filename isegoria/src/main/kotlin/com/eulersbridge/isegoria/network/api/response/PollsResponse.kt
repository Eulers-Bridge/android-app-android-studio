package com.eulersbridge.isegoria.network.api.response

import com.eulersbridge.isegoria.network.api.model.Poll

data class PollsResponse (
    val polls: List<Poll>?,
    val totalPolls: Long = 0
)