package com.eulersbridge.isegoria.network.api.model

import com.squareup.moshi.Json

data class Institution (
    @field:Json(name = "institutionId") var id: Long = 0,
    var newsFeedId: Long = 0,

    // Different JSON names used depending on API endpoint.
    // Try to read both, keep private and use public getter to return whichever is non-null.
    private val institutionName: String?,
    private val name: String?,

    var state: String?,
    var campus: String?,
    var country: String?
) {
    fun getName(): String? {
        return if (institutionName.isNullOrBlank()) {
            name

        } else {
            institutionName
        }
    }

    override fun toString(): String {
        return getName() ?: ""
    }
}
