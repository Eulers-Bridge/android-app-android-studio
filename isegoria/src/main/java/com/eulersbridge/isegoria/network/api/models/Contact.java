package com.eulersbridge.isegoria.network.api.models;

import android.os.Build;
import android.support.annotation.Nullable;

import com.squareup.moshi.Json;

import org.parceler.Parcel;

import java.util.Objects;

@Parcel
public class Contact extends GenericUser {

    @Json(name = "numOfContacts")
    @Nullable
    public Long contactsCount;

    @Json(name = "totalTasks")
    @Nullable
    public Long totalTasksCount;

    @Json(name = "totalBadges")
    @Nullable
    public Long totalBadgesCount;

    @Json(name = "userId")
    @Nullable
    public Long id;

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        if (!super.equals(o))
            return false;

        Contact contact = (Contact) o;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            return Objects.equals(this, contact);

        if (contactsCount != null ? !contactsCount.equals(contact.contactsCount) : contact.contactsCount != null)
            return false;
        return (totalTasksCount != null ? totalTasksCount.equals(contact.totalTasksCount) : contact.totalTasksCount == null) && (totalBadgesCount != null ? totalBadgesCount.equals(contact.totalBadgesCount) : contact.totalBadgesCount == null) && (id != null ? id.equals(contact.id) : contact.id == null);
    }

    @Override
    public int hashCode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            return Objects.hashCode(this);

        int result = super.hashCode();
        result = 31 * result + (contactsCount != null ? contactsCount.hashCode() : 0);
        result = 31 * result + (totalTasksCount != null ? totalTasksCount.hashCode() : 0);
        result = 31 * result + (totalBadgesCount != null ? totalBadgesCount.hashCode() : 0);
        result = 31 * result + (id != null ? id.hashCode() : 0);
        return result;
    }
}
