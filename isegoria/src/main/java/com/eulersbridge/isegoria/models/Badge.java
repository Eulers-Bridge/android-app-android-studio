package com.eulersbridge.isegoria.models;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Seb on 03/11/2017.
 */

public class Badge {

    private long id;
    private String name;
    private String description;
    private int level;
    private int xpValue;

    public Badge(JSONObject jsonObject) {
        try {
            id = jsonObject.getLong("badgeId");
            name = jsonObject.getString("name");
            description = jsonObject.getString("description");
            level = jsonObject.getInt("level");
            xpValue = jsonObject.getInt("xpValue");

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

    public String getDescription() {
        return description;
    }

    public int getLevel() {
        return level;
    }

    public int getXpValue() {
        return xpValue;
    }
}
