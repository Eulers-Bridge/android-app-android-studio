package com.eulersbridge.isegoria.network.api.models;

import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.squareup.moshi.Json;

import java.util.Objects;

public class GenericUser {

    public String gender;
    public String nationality;

    public String email;
    public String givenName;
    public String familyName;

    @Nullable
    public Long institutionId;

    public long level;
    public long experience;

    @Json(name = "numOfCompTasks")
    public long completedTasksCount;

    @Json(name = "numOfCompBadges")
    public long completedBadgesCount;

    @Json(name = "profilePhoto")
    public String profilePhotoURL;

    public @NonNull String getFullName() {
        if (TextUtils.isEmpty(givenName) && TextUtils.isEmpty(familyName)) {
            return "";

        } else if (TextUtils.isEmpty(familyName)) {
            return givenName;

        } else if (TextUtils.isEmpty(givenName)) {
            return familyName;

        } else {
            return String.format("%s %s", givenName, familyName);
        }
    }

    GenericUser() {
        // Required empty constructor
    }

    GenericUser(GenericUser user) {
        this.gender = user.gender;
        this.nationality = user.nationality;
        this.email = user.email;
        this.givenName = user.givenName;
        this.familyName = user.familyName;
        this.institutionId = user.institutionId;
        this.level = user.level;
        this.experience = user.experience;
        this.completedTasksCount = user.completedTasksCount;
        this.completedBadgesCount = user.completedBadgesCount;
        this.profilePhotoURL = user.profilePhotoURL;
    }

    // Android Studio auto-generated equals & hashCode

    @SuppressWarnings("SimplifiableIfStatement")
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof GenericUser))
            return false;

        GenericUser that = (GenericUser) o;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            return Objects.equals(this, that);

        if (level != that.level) return false;
        if (experience != that.experience) return false;
        if (completedTasksCount != that.completedTasksCount) return false;
        if (completedBadgesCount != that.completedBadgesCount) return false;
        if (gender != null ? !gender.equals(that.gender) : that.gender != null) return false;
        if (nationality != null ? !nationality.equals(that.nationality) : that.nationality != null)
            return false;
        if (!email.equals(that.email)) return false;
        if (givenName != null ? !givenName.equals(that.givenName) : that.givenName != null)
            return false;
        return (familyName != null ? familyName.equals(that.familyName) : that.familyName == null) && (institutionId != null ? institutionId.equals(that.institutionId) : that.institutionId == null) && (profilePhotoURL != null ? profilePhotoURL.equals(that.profilePhotoURL) : that.profilePhotoURL == null);
    }

    @Override
    public int hashCode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            return Objects.hashCode(this);

        int result = gender != null ? gender.hashCode() : 0;
        result = 31 * result + (nationality != null ? nationality.hashCode() : 0);
        result = 31 * result + email.hashCode();
        result = 31 * result + (givenName != null ? givenName.hashCode() : 0);
        result = 31 * result + (familyName != null ? familyName.hashCode() : 0);
        result = 31 * result + (institutionId != null ? institutionId.hashCode() : 0);
        result = 31 * result + (int) (level ^ (level >>> 32));
        result = 31 * result + (int) (experience ^ (experience >>> 32));
        result = 31 * result + (int) (completedTasksCount ^ (completedTasksCount >>> 32));
        result = 31 * result + (int) (completedBadgesCount ^ (completedBadgesCount >>> 32));
        result = 31 * result + (profilePhotoURL != null ? profilePhotoURL.hashCode() : 0);
        return result;
    }
}
