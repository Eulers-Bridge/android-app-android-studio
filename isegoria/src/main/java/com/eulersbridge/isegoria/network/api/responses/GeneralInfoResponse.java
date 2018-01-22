package com.eulersbridge.isegoria.network.api.responses;

import com.eulersbridge.isegoria.network.api.models.Country;
import com.squareup.moshi.Json;

import java.util.List;

public class GeneralInfoResponse {

    @Json(name = "countrys")
    public List<Country> countries;

}
