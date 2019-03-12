package com.eulersbridge.isegoria.network.api.model

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
    var code: String?,

    private val colour: String?

) {
    val fullName: String
        get() = name?.takeUnless { it.isBlank() } ?: "$givenName $familyName"
    
    fun getColour()
        = colour?.takeUnless { it.isBlank() } ?: "#000000"
}