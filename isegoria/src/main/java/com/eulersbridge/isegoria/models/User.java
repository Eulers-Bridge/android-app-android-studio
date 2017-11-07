package com.eulersbridge.isegoria.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

public class User implements Parcelable {

    private @Nullable String password;

    private @Nullable String id;

    private @NonNull String email;
    private String givenName;
    private String familyName;

    private boolean accountVerified;

    private boolean hasPPSEQuestions;
    private boolean hasPersonality;

    private boolean trackingOff;
    private boolean optOutDataCollection;

    private String gender;
    private String nationality;
    private String yearOfBirth;

    private long contactsCount;

    private long completedTasksCount;
    private long completedBadgesCount;
    private long experience;
    private long level;

    private long institutionId;

    private @Nullable String profilePhotoURL;

    public User(JSONObject jsonObject) {
        try {
            id = jsonObject.optString("userId");
            email = jsonObject.getString("email");
            givenName = jsonObject.getString("givenName");
            familyName = jsonObject.getString("familyName");

            accountVerified = jsonObject.optBoolean("accountVerified");
            hasPPSEQuestions = jsonObject.optBoolean("hasPPSEQuestions");
            hasPersonality = jsonObject.optBoolean("hasPersonality");

            trackingOff = jsonObject.optBoolean("trackingOff");
            optOutDataCollection = jsonObject.optBoolean("optOutDataCollection");

            gender = jsonObject.getString("gender");
            nationality = jsonObject.getString("nationality");
            yearOfBirth = jsonObject.optString("yearOfBirth");

            contactsCount = jsonObject.optLong("numOfContacts");

            completedTasksCount = jsonObject.getLong("numOfCompTasks");
            completedBadgesCount = jsonObject.getLong("numOfCompBadges");
            experience = jsonObject.getLong("experience");
            level = jsonObject.getLong("level");

            institutionId = jsonObject.optLong("institutionId");

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
        hasPPSEQuestions = (in.readInt() == 1);
        hasPersonality = (in.readInt() == 1);

        trackingOff = (in.readInt() == 1);
        optOutDataCollection= (in.readInt() == 1);

        gender = in.readString();
        nationality = in.readString();
        yearOfBirth = in.readString();

        contactsCount = in.readLong();

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
        parcel.writeInt(hasPPSEQuestions? 1 : 0);
        parcel.writeInt(hasPersonality? 1 : 0);

        parcel.writeInt(trackingOff? 1 : 0);
        parcel.writeInt(optOutDataCollection? 1 : 0);

        parcel.writeString(gender);
        parcel.writeString(nationality);
        parcel.writeString(yearOfBirth);

        parcel.writeLong(contactsCount);

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

    public void setId(String id) {
        this.id = id;
    }

    @Nullable
    public String getId() {
        return id;
    }

    @NonNull
    public String getEmail() {
        return email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Nullable
    public String getPassword() {
        return password;
    }

    public String getGivenName() {
        return givenName;
    }

    public String getFamilyName() {
        return familyName;
    }

    public String getFullName() {
        return String.format("%s %s", givenName, familyName);
    }

    public long getInstitutionId() {
        return institutionId;
    }

    @Nullable
    public String getProfilePhotoURL() {
        return profilePhotoURL;
    }

    public boolean isAccountVerified() {
        return accountVerified;
    }

    boolean hasPPSEQuestions() {
        return hasPPSEQuestions;
    }

    public boolean hasPersonality() {
        return hasPersonality;
    }

    public void setTrackingOff(boolean trackingOff) {
        this.trackingOff = trackingOff;
    }

    public boolean isTrackingOff() {
        return trackingOff;
    }

    public void setOptedOutOfDataCollection(boolean optedOutOfDataCollection) {
        this.optOutDataCollection = optedOutOfDataCollection;
    }

    public boolean isOptedOutOfDataCollection() {
        return optOutDataCollection;
    }

    public void setContactsCount(long contactsCount) {
        this.contactsCount = contactsCount;
    }

    public long getContactsCount() {
        return contactsCount;
    }

    public long getCompletedTasksCount() {
        return completedTasksCount;
    }

    public long getCompletedBadgesCount() {
        return completedBadgesCount;
    }

    long getExperience() {
        return experience;
    }

    public long getLevel() {
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
