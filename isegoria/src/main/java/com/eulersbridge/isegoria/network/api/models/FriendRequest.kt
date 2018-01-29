package com.eulersbridge.isegoria.network.api.models

import com.squareup.moshi.Json

class FriendRequest (
    val id: Long,
    val accepted: Boolean?,
    val rejected: Boolean?,

    @Json(name = "requesterProfile")
    val requester: User?,

    @Json(name = "requestReceiverProfile")
    val requestReceiver: User?
)
