package com.eulersbridge.isegoria;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

class Event implements Parcelable {

    private String name;
    private String location;
    private String description;

    private String organizer;
    private String organizerEmail;

    private long date;

    private String imageUrl;

    Event(JSONObject jsonObject) {
        try {
            name = jsonObject.getString("name");
            location = jsonObject.getString("location");
            description = jsonObject.getString("description");
            organizer = jsonObject.getString("organizer");
            organizerEmail = jsonObject.getString("organizerEmail");

            if (jsonObject.has("created") && !jsonObject.isNull("created")) {
                date = jsonObject.getLong("created");
                //date = TimeConverter.convertTimestampTimezone(date);
            }

            if (jsonObject.has("photos") && !jsonObject.isNull("photos")) {

                JSONArray photos = jsonObject.getJSONArray("photos");
                if (photos.length() > 0) {
                    imageUrl = photos.getJSONObject(0).getString("url");
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private Event(Parcel in) {
        name = in.readString();
        location = in.readString();
        description = in.readString();

        organizer = in.readString();
        organizerEmail = in.readString();

        date = in.readLong();
        imageUrl = in.readString();
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeString(location);
        parcel.writeString(description);

        parcel.writeString(organizer);
        parcel.writeString(organizerEmail);

        parcel.writeLong(date);

        parcel.writeString(imageUrl);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Event> CREATOR = new Creator<Event>() {
        @Override
        public Event createFromParcel(Parcel in) {
            return new Event(in);
        }

        @Override
        public Event[] newArray(int size) {
            return new Event[size];
        }
    };

    public String getName() {
        return name;
    }

    String getLocation() {
        return location;
    }

    String getDescription() {
        return description;
    }

    public String getOrganizer() {
        return organizer;
    }

    public String getOrganizerEmail() {
        return organizerEmail;
    }

    long getDate() {
        return date;
    }

    String getImageUrl() {
        return imageUrl;
    }
}
