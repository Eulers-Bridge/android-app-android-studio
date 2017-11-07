package com.eulersbridge.isegoria.models;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Anthony on 24/03/2015.
 */
public class VoteLocation {

    private long ownerId;
    private long votingLocationId;
    private String name;
    private String information;

    public VoteLocation(JSONObject jsonObject) {
        try {
            this.ownerId = jsonObject.getLong("ownerId");
            this.votingLocationId = jsonObject.getLong("votingLocationId");
            this.name = jsonObject.getString("name");
            this.information = jsonObject.getString("information");

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public long getOwnerId() {
        return ownerId;
    }

    public long getVotingLocationId() {
        return votingLocationId;
    }

    public String getName() {
        return name;
    }

    public String getInformation() {
        return information;
    }

    public void setInformation(String information) {
        this.information = information;
    }
}
