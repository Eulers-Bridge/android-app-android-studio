package com.eulersbridge.isegoria.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Seb on 04/11/2017.
 */

class ElectionInstitution {

    @SerializedName("institutionId")
    public long id;

    public String name;
    public String campus;
    public String state;
    public String country;
}
