package com.eulersbridge.isegoria.models;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Seb on 03/11/2017.
 */

public class PhotoAlbum {

    private int id;

    private String name;
    private String description;
    private String location;
    private String thumbnailPhotoUrl;

    public PhotoAlbum(JSONObject jsonObject) {
        try {
            this.id = jsonObject.getInt("nodeId");
            this.name = jsonObject.getString("name");
            this.description = jsonObject.getString("description");
            this.location = jsonObject.getString("location");
            this.thumbnailPhotoUrl = jsonObject.getString("thumbNailUrl");

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getLocation() {
        return location;
    }

    public String getThumbnailPhotoUrl() {
        return thumbnailPhotoUrl;
    }

}
