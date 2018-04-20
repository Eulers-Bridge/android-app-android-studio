package com.eulersbridge.isegoria.network.api.response

import com.eulersbridge.isegoria.network.api.model.Country
import com.squareup.moshi.Json

data class GeneralInfoResponse (
    @field:Json(name = "countrys")
    val countries: List<Country>
)
