package com.eulersbridge.isegoria.network.api.models;

import com.squareup.moshi.Json;

@SuppressWarnings("unused")
class ElectionInstitution {

    @Json(name = "institutionId")
    public long id;

    public String name;
    public String campus;
    public String state;
    public String country;
}
