package com.eulersbridge.isegoria.models;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Seb on 04/11/2017.
 */

public class CandidateTicket {

    private long id;

    private String name;
    private String supportersCount;
    private String information;
    private String logo;
    private String colour;

    public CandidateTicket(JSONObject jsonObject) {
        try {
            id = jsonObject.getLong("ticketId");

            name = jsonObject.optString("name");

            if (TextUtils.isEmpty(name)) {
                String givenName = jsonObject.getString("givenName");
                String familyName = jsonObject.getString("familyName");

                name = String.format("%s %s", givenName, familyName);
            }

            supportersCount = jsonObject.getString("numberOfSupporters");
            information = jsonObject.getString("information");
            logo = jsonObject.getString("logo");

            if (jsonObject.isNull("colour")) {
                colour = "#000000";
            } else {
                colour = jsonObject.getString("colour");
            }


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

    public String getSupportersCount() {
        return supportersCount;
    }

    public String getInformation() {
        return information;
    }

    public String getLogo() {
        return logo;
    }

    public String getColour() {
        return colour;
    }
}
