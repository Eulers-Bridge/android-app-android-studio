package com.eulersbridge.isegoria.models;

import com.squareup.moshi.Json;

class ElectionInstitution {

    @Json(name = "institutionId")
    public long id;

    public String name;
    public String campus;
    public String state;
    public String country;
}
