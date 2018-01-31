package com.eulersbridge.isegoria.network.api.models

import com.squareup.moshi.Json

data class CandidateTicket (

    @Json(name = "ticketId")
    var id: Long = 0,

    var name: String?,
    var givenName: String?,
    var familyName: String?,

    @Json(name = "numberOfSupporters")
    var supportersCount: String?,

    var information: String?,
    var logo: String? = null,

    private val colour: String?

) {
    val fullName: String
        get() {
            return if (!name.isNullOrBlank()) {
                return name!!

            } else {
                "$givenName $familyName"
            }
        }

    fun getColour(): String {
        return if (!colour.isNullOrBlank()) {
            colour!!

        } else {
            "#000000"
        }
    }
}