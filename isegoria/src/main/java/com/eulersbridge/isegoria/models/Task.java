package com.eulersbridge.isegoria.models;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Seb on 04/11/2017.
 */

public class Task {

    private long id;
    private String action;
    private long xpValue;

    public Task(JSONObject jsonObject) {
        try {
            id = jsonObject.getLong("taskId");
            action = jsonObject.getString("action");
            xpValue = jsonObject.getLong("xpValue");

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public long getId() {
        return id;
    }

    public String getAction() {
        return action;
    }

    public long getXpValue() {
        return xpValue;
    }
}
