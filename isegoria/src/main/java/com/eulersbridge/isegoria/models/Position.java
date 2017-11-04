package com.eulersbridge.isegoria.models;

import org.json.JSONObject;

/**
 * Created by Seb on 04/11/2017.
 */

public class Position {

    private long id;
    private long electionId;
    private String name;
    private String description;

    public Position(JSONObject jsonObject) {
        try {
            id = jsonObject.getLong("positionId");
            electionId = jsonObject.optLong("electionId", 0);
            name = jsonObject.getString("name");
            description = jsonObject.getString("description");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public long getId() {
        return id;
    }

    public long getElectionId() {
        return electionId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
