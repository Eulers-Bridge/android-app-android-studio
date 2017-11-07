package com.eulersbridge.isegoria.models;

import android.support.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Seb on 04/11/2017.
 */

public class ElectionInstitution {

    private long id;
    private String name;
    private String campus;
    private @Nullable String state;
    private String country;

    ElectionInstitution(JSONObject jsonObject) {
        try {
            id = jsonObject.getLong("institutionId");
            name = jsonObject.getString("name");
            campus = jsonObject.getString("campus");
            state = jsonObject.getString("state");
            country = jsonObject.getString("country");

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCampus() {
        return campus;
    }

    @Nullable
    public String getState() {
        return state;
    }

    public String getCountry() {
        return country;
    }
}
