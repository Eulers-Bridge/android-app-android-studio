package com.eulersbridge.isegoria.network.api.models

import com.squareup.moshi.Json

data class Ticket (

    @Json(name = "ticketId")
    val id: Long = 0,

    val electionId: Long = 0,

    val name: String?,
    val information: String?,

    private val colour: String?,
    val code: String?,
    val logo: String?,

    @Json(name = "numberOfSupporters")
    val supporterCount: Long = 0

) {
    fun getColour(): String {
        return if (!colour.isNullOrBlank()) {
            colour!!

        } else {
            "#000000"
        }
    }
}
