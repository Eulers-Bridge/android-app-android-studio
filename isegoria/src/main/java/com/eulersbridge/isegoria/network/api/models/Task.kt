package com.eulersbridge.isegoria.network.api.models

import com.squareup.moshi.Json

data class Task (
    @Json(name = "taskId") val id: Long = 0,
    val action: String? = null,
    val xpValue: Long = 0
)