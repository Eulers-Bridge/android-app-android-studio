package com.eulersbridge.isegoria.network.api.model

import com.squareup.moshi.Json

class FriendRequest (
        val id: Long,
        val accepted: Boolean?,
        @field:Json(name = "requesterProfile")
        val requester: User?,

        @field:Json(name = "requestReceiverProfile")
        val requestReceiver: User?
)
