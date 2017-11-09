package com.eulersbridge.isegoria.network;

import com.eulersbridge.isegoria.models.Country;
import com.squareup.moshi.Json;

import java.util.List;

public class GeneralInfoResponse {

    @Json(name = "countrys")
    public List<Country> countries;

}
