package com.eulersbridge.isegoria.network.api.models

import com.squareup.moshi.Json

data class CandidateTicket (

    @field:Json(name = "ticketId")
    var id: Long = 0,

    var name: String?,
    var givenName: String?,
    var familyName: String?,

    @field:Json(name = "numberOfSupporters")
    var supportersCount: String?,

    var information: String?,
    var logo: String? = null,

    private val colour: String?

) {
    val fullName: String
        get() = name?.takeUnless { it.isBlank() } ?: "$givenName $familyName"
    
    fun getColour()
        = colour?.takeUnless { it.isBlank() } ?: "#000000"
}