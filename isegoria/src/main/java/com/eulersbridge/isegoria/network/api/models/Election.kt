package com.eulersbridge.isegoria.network.api.models

import com.eulersbridge.isegoria.network.adapters.Timestamp
import com.squareup.moshi.Json

data class Election (
    @Json(name = "electionId")
    val id: Long,

    @Timestamp
    val start: Long,

    @Timestamp
    val end: Long,

    @Timestamp
    val startVoting: Long,

    @Timestamp
    val endVoting: Long,

    val title: String?,
    val introduction: String?,
    val process: String?,

    @Json(name = "institutionDomain")
    private val electionInstitution: ElectionInstitution? = null
)