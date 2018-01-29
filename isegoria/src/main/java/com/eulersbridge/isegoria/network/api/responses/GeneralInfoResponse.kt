package com.eulersbridge.isegoria.network.api.responses

import com.eulersbridge.isegoria.network.api.models.Country
import com.squareup.moshi.Json

data class GeneralInfoResponse (
    @Json(name = "countrys")
    val countries: List<Country>?
)
