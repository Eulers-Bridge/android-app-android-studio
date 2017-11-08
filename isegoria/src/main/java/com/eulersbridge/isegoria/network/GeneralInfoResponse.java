package com.eulersbridge.isegoria.network;

import com.eulersbridge.isegoria.models.Country;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Seb on 07/11/2017.
 */

public class GeneralInfoResponse {

    @SerializedName("countrys")
    public List<Country> countries;

}
