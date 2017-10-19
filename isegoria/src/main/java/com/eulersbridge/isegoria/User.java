package com.eulersbridge.isegoria;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

class User implements Parcelable {

    private String userId;
    private String email;
    private String givenName;
    private String familyName;

    private long institutionId;

    private String profilePhotoURL;

    User(JSONObject jsonObject) {
        try {
            email = jsonObject.getString("email");
            givenName = jsonObject.getString("givenName");
            familyName = jsonObject.getString("familyName");

            institutionId = jsonObject.getLong("institutionId");

            if (jsonObject.has("profilePhoto") && !jsonObject.isNull("profilePhoto")) {
                profilePhotoURL = jsonObject.getString("profilePhoto");
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    User(Parcel in) {
        email = in.readString();
        givenName = in.readString();
        familyName = in.readString();

        institutionId = in.readLong();

        profilePhotoURL = in.readString();
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(email);
        parcel.writeString(givenName);
        parcel.writeString(familyName);

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

    public String getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
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

    public String getProfilePhotoURL() {
        return profilePhotoURL;
    }
}
