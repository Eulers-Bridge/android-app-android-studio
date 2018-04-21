package com.eulersbridge.isegoria.network.api.model

import com.squareup.moshi.Json

data class Country(
    @field:Json(name = "countryId")
    val id: Long = 0,

    @field:Json(name = "countryName")
    val name: String,

    val institutions: List<Institution>?
) {
    override fun toString() = name
}