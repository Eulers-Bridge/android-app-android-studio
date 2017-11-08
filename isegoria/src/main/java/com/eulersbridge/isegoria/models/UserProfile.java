package com.eulersbridge.isegoria.models;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;
import org.parceler.Transient;

/**
 * Created by Seb on 07/11/2017.
 */

@Parcel
public class UserProfile {

    @Transient
    private String password;

    @Transient
    private long newsFeedId;

    @SerializedName("userId")
    public long id;

    public String email;
    public String givenName;
    public String familyName;

    public boolean accountVerified;

    public boolean hasPPSEQuestions;
    public boolean hasPersonality;

    public boolean trackingOff;

    @SerializedName("optOutDataCollection")
    public boolean isOptedOutOfDataCollection;

    public String gender;
    public String nationality;
    public String yearOfBirth;

    @SerializedName("numOfContacts")
    public long contactsCount;

    @SerializedName("numOfCompTasks")
    public long completedTasksCount;

    @SerializedName("totalTasks")
    public long totalTasksCount;

    @SerializedName("numOfCompBadges")
    public long completedBadgesCount;
    public long experience;
    public long level;

    public long institutionId;

    @SerializedName("profilePhoto")
    public String profilePhotoURL;

    public String getFullName() {
        return String.format("%s %s", givenName, familyName);
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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