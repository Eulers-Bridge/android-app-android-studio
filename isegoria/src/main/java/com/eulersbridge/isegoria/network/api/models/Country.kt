package com.eulersbridge.isegoria.network.api.models

import com.squareup.moshi.Json

data class Country(
    @Json(name = "countryId")
    val id: Long = 0,

    @Json(name = "countryName")
    val name: String,

    val institutions: List<Institution>?
) {
    override fun toString(): String {
        return name
    }
}