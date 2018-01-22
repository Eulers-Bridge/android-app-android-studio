package com.eulersbridge.isegoria.network.api.models;

import android.os.Build;

import com.squareup.moshi.Json;

import org.parceler.Parcel;
import org.parceler.Transient;

import java.util.Objects;

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

    public User() {
        // Required empty constructor for @Parcel
    }

    public User(User user) {
        super(user);

        this.newsFeedId = user.getNewsFeedId();
        this.accountVerified = user.accountVerified;
        this.hasPPSEQuestions = user.hasPPSEQuestions;
        this.hasPersonality = user.hasPersonality;
        this.trackingOff = user.trackingOff;
        this.isOptedOutOfDataCollection = user.isOptedOutOfDataCollection;
        this.yearOfBirth = user.yearOfBirth;

        long userId = user.getId();
        this.id = (userId == 0)? null : userId;
    }

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

    public void setPPSEQuestionsCompleted() {
        this.hasPPSEQuestions = true;
    }

    @SuppressWarnings("SimplifiableIfStatement")
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        User user = (User) o;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            return Objects.equals(this, user);

        if (getNewsFeedId() != user.getNewsFeedId()) return false;
        if (accountVerified != user.accountVerified) return false;
        if (hasPPSEQuestions != user.hasPPSEQuestions) return false;
        if (hasPersonality != user.hasPersonality) return false;
        if (trackingOff != user.trackingOff) return false;
        if (isOptedOutOfDataCollection != user.isOptedOutOfDataCollection) return false;
        if (yearOfBirth != null ? !yearOfBirth.equals(user.yearOfBirth) : user.yearOfBirth != null)
            return false;
        return getId() == user.getId();
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (int) (getNewsFeedId() ^ (getNewsFeedId() >>> 32));
        result = 31 * result + (accountVerified ? 1 : 0);
        result = 31 * result + (hasPPSEQuestions ? 1 : 0);
        result = 31 * result + (hasPersonality ? 1 : 0);
        result = 31 * result + (trackingOff ? 1 : 0);
        result = 31 * result + (isOptedOutOfDataCollection ? 1 : 0);
        result = 31 * result + (yearOfBirth != null ? yearOfBirth.hashCode() : 0);
        result = 31 * result + (int) (getId() ^ (getId() >>> 32));
        return result;
    }
}