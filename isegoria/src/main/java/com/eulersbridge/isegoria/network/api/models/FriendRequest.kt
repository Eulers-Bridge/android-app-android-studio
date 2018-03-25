package com.eulersbridge.isegoria.network.api.models

import com.squareup.moshi.Json

class FriendRequest (
    val id: Long,
    val accepted: Boolean?,
    val rejected: Boolean?,

    @field:Json(name = "requesterProfile")
    val requester: User?,

    @field:Json(name = "requestReceiverProfile")
    val requestReceiver: User?
)
