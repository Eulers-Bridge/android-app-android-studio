package com.eulersbridge.isegoria.auth.signup;

import android.support.annotation.NonNull;

import org.parceler.Parcel;

@Parcel
public class SignUpUser {

    public String givenName;
    public String familyName;
    public String gender;
    public String nationality;
    public String yearOfBirth;
    public String email;
    public String password;

    public String institutionName;
    public long institutionId;

    public boolean accountVerified = false;
    public boolean hasPersonality = false;

    SignUpUser() {
        // Required empty constructor for @Parcel
    }

    public SignUpUser(String givenName, String familyName, String gender, String nationality,
                      String yearOfBirth, String email, String password, String institutionName) {
        this.givenName = givenName;
        this.familyName = familyName;
        this.gender = gender;
        this.nationality = nationality;
        this.yearOfBirth = yearOfBirth;
        this.email = email;
        this.password = password;

        this.institutionName = institutionName;
    }

    // Copy Constructor
    public SignUpUser(@NonNull SignUpUser user) {
        this.givenName = user.givenName;
        this.familyName = user.familyName;
        this.gender = user.gender;
        this.nationality = user.nationality;
        this.yearOfBirth = user.yearOfBirth;
        this.email = user.email;
        this.password = user.password;
        this.institutionName = user.institutionName;
        this.institutionId = user.institutionId;
        this.accountVerified = user.accountVerified;
        this.hasPersonality = user.hasPersonality;

    }

}
