package com.eulersbridge.isegoria.network.api.models

import com.eulersbridge.isegoria.network.adapters.Timestamp

data class VoteReminder(
    val userEmail: String,
    val electionId: Long,
    var location: String,
    @Timestamp val date: Long
)