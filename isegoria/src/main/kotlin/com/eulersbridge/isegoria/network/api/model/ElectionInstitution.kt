package com.eulersbridge.isegoria.network.api.model

import com.squareup.moshi.Json

data class ElectionInstitution (
    @field:Json(name = "institutionId")
    var id: Long = 0,

    val name: String?,
    val campus: String?,
    val state: String?,
    val country: String?
)
