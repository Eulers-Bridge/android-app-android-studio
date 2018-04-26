package com.eulersbridge.isegoria.network.api.model

import com.squareup.moshi.Json

data class Badge(
    @field:Json(name = "badgeId")
    val id: Long = 0,

    val name: String?,
    val description: String?,
    val level: Int = 0,
    val xpValue: Int = 0
)