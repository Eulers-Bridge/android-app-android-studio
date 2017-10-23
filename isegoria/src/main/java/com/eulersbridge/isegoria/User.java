package com.eulersbridge.isegoria;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

class User implements Parcelable {

    private String id;

    private String email;
    private String givenName;
    private String familyName;

    private boolean accountVerified;
    private boolean hasPersonality;

    private boolean trackingOff;
    private boolean optOutDataCollection;

    private String gender;
    private String nationality;
    private String yearOfBirth;

    private long completedTasksCount;
    private long completedBadgesCount;
    private long experience;
    private long level;

    private long institutionId;

    private String profilePhotoURL;

    User(JSONObject jsonObject) {
        try {
            id = String.valueOf(jsonObject.optLong("userId"));
            email = jsonObject.getString("email");
            givenName = jsonObject.getString("givenName");
            familyName = jsonObject.getString("familyName");

            accountVerified = jsonObject.optBoolean("accountVerified");
            hasPersonality = jsonObject.getBoolean("hasPersonality");

            trackingOff = jsonObject.getBoolean("trackingOff");
            optOutDataCollection = jsonObject.getBoolean("optOutDataCollection");

            gender = jsonObject.getString("gender");
            nationality = jsonObject.getString("nationality");
            yearOfBirth = jsonObject.getString("yearOfBirth");

            completedTasksCount = jsonObject.getLong("numOfCompTasks");
            completedBadgesCount = jsonObject.getLong("numOfCompBadges");
            experience = jsonObject.getLong("experience");
            level = jsonObject.getLong("level");

            institutionId = jsonObject.getLong("institutionId");

            if (jsonObject.has("profilePhoto") && !jsonObject.isNull("profilePhoto")) {
                profilePhotoURL = jsonObject.getString("profilePhoto");
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private User(Parcel in) {
        id = in.readString();
        email = in.readString();
        givenName = in.readString();
        familyName = in.readString();

        accountVerified = (in.readInt() == 1);
        hasPersonality = (in.readInt() == 1);

        trackingOff = (in.readInt() == 1);
        optOutDataCollection= (in.readInt() == 1);

        gender = in.readString();
        nationality = in.readString();
        yearOfBirth = in.readString();

        completedTasksCount = in.readLong();
        completedBadgesCount = in.readLong();
        experience = in.readLong();
        level = in.readLong();

        institutionId = in.readLong();

        profilePhotoURL = in.readString();
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);

        parcel.writeString(email);
        parcel.writeString(givenName);
        parcel.writeString(familyName);

        parcel.writeInt(accountVerified? 1 : 0);
        parcel.writeInt(hasPersonality? 1 : 0);

        parcel.writeInt(trackingOff? 1 : 0);
        parcel.writeInt(optOutDataCollection? 1 : 0);

        parcel.writeString(gender);
        parcel.writeString(nationality);
        parcel.writeString(yearOfBirth);

        parcel.writeLong(completedTasksCount);
        parcel.writeLong(completedBadgesCount);
        parcel.writeLong(experience);
        parcel.writeLong(level);

        parcel.writeLong(institutionId);

        parcel.writeString(profilePhotoURL);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    void setId(String id) {
        this.id = id;
    }

    String getId() {
        return id;
    }

    String getEmail() {
        return email;
    }

    public String getGivenName() {
        return givenName;
    }

    String getFamilyName() {
        return familyName;
    }

    String getFullName() {
        return String.format("%s %s", givenName, familyName);
    }

    public long getInstitutionId() {
        return institutionId;
    }

    String getProfilePhotoURL() {
        return profilePhotoURL;
    }

    boolean isAccountVerified() {
        return accountVerified;
    }

    boolean hasPersonality() {
        return hasPersonality;
    }

    void setTrackingOff(boolean trackingOff) {
        this.trackingOff = trackingOff;
    }

    boolean isTrackingOff() {
        return trackingOff;
    }

    void setOptedOutOfDataCollection(boolean optedOutOfDataCollection) {
        this.optOutDataCollection = optedOutOfDataCollection;
    }

    boolean isOptedOutOfDataCollection() {
        return optOutDataCollection;
    }

    long getCompletedTasksCount() {
        return completedTasksCount;
    }

    long getCompletedBadgesCount() {
        return completedBadgesCount;
    }

    long getExperience() {
        return experience;
    }

    long getLevel() {
        return level;
    }

    public String getGender() {
        return gender;
    }

    public String getNationality() {
        return nationality;
    }

    public String getYearOfBirth() {
        return yearOfBirth;
    }
}
