package com.eulersbridge.isegoria.network.api.models;

import android.os.Build;
import android.support.annotation.Nullable;

import com.squareup.moshi.Json;

import java.util.Objects;

@SuppressWarnings("unused")
public class FriendRequest {

    public long id;

    @Nullable
    public Boolean accepted;

    @Nullable
    public Boolean rejected;

    @Json(name = "requesterProfile")
    public User requester;

    @Json(name = "requestReceiverProfile")
    public User requestReceiver;

    @SuppressWarnings("SimplifiableIfStatement")
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        FriendRequest that = (FriendRequest) o;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            return Objects.equals(this, that);

        if (id != that.id) return false;
        if (accepted != null ? !accepted.equals(that.accepted) : that.accepted != null)
            return false;
        return (rejected != null ? rejected.equals(that.rejected) : that.rejected == null) && (requester != null ? requester.equals(that.requester) : that.requester == null) && (requestReceiver != null ? requestReceiver.equals(that.requestReceiver) : that.requestReceiver == null);
    }

    @Override
    public int hashCode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            return Objects.hashCode(this);

        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (accepted != null ? accepted.hashCode() : 0);
        result = 31 * result + (rejected != null ? rejected.hashCode() : 0);
        result = 31 * result + (requester != null ? requester.hashCode() : 0);
        result = 31 * result + (requestReceiver != null ? requestReceiver.hashCode() : 0);
        return result;
    }
}
