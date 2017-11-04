package com.eulersbridge.isegoria.models;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Seb on 04/11/2017.
 */

public class Candidate {

    private long id;
    private long ticketId;
    private long positionId;
    private long userId;

    private String familyName;
    private String givenName;
    private String policyStatement;
    private String information;

    public Candidate(JSONObject jsonObject) {
        try {
            id = jsonObject.getLong("candidateId");
            ticketId = jsonObject.getLong("ticketId");
            positionId = jsonObject.getLong("positionId");
            userId = jsonObject.getLong("userId");

            familyName = jsonObject.getString("familyName");
            givenName = jsonObject.getString("givenName");
            policyStatement = jsonObject.getString("policyStatement");
            information = jsonObject.getString("information");

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public long getId() {
        return id;
    }

    public long getTicketId() {
        return ticketId;
    }

    public long getPositionId() {
        return positionId;
    }

    public long getUserId() {
        return userId;
    }

    public String getFamilyName() {
        return familyName;
    }

    public String getGivenName() {
        return givenName;
    }

    public String getPolicyStatement() {
        return policyStatement;
    }

    public String getInformation() {
        return information;
    }
}
