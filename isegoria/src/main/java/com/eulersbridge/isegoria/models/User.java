package com.eulersbridge.isegoria.models;

import com.squareup.moshi.Json;

import org.parceler.Parcel;
import org.parceler.Transient;

@Parcel
public class User extends GenericUser {

    @Transient
    private long newsFeedId;

    public boolean accountVerified;

    public boolean hasPPSEQuestions;
    public boolean hasPersonality;

    public boolean trackingOff;

    @Json(name = "optOutDataCollection")
    public boolean isOptedOutOfDataCollection;

    public String yearOfBirth;

    @Transient
    private transient Long id;

    public long getId() {
        return (id == null)? 0 : id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getNewsFeedId() {
        return newsFeedId;
    }

    public void setNewsFeedId(long newsFeedId) {
        this.newsFeedId = newsFeedId;
    }

    public void setTrackingOff(boolean trackingOff) {
        this.trackingOff = trackingOff;
    }

    public void setOptedOutOfDataCollection(boolean optedOutOfDataCollection) {
        this.isOptedOutOfDataCollection = optedOutOfDataCollection;
    }
}