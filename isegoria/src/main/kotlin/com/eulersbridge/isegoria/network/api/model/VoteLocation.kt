package com.eulersbridge.isegoria.network.api.model

import com.squareup.moshi.Json

data class VoteLocation (
    val ownerId: Long = 0,
    @field:Json(name = "votingLocationId") var id: Long = 0,
    val name: String?,
    val information: String?

) {
    override fun toString()
        = name ?: id.toString()
}