package com.eulersbridge.isegoria.network.api.models

import android.annotation.SuppressLint
import android.os.Parcel
import android.os.Parcelable
import com.eulersbridge.isegoria.network.adapters.Timestamp
import com.squareup.moshi.Json
import kotlinx.android.parcel.Parceler
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.TypeParceler
import org.parceler.Parcels

@SuppressLint("ParcelCreator")
@Parcelize
data class NewsArticle(
        @Json(name = "articleId") val id: Long,
        val institutionId: Long?,
        val title: String,
        val content: String,
        @TypeParceler<Photo, PhotoParceler> val photos: List<Photo>?,
        @Json(name = "date") @Timestamp val dateTimestamp: Long,
        @Json(name = "likes") val likeCount: Int = 0,
        @TypeParceler<Contact, ContactParceler>  @Json(name = "creatorProfile") val creator: Contact,
        @Json(name = "inappropriateContent") val hasInappropriateContent: Boolean
) : Parcelable {
    val photoUrl: String?
    get() {
        return photos?.get(0)?.thumbnailUrl
    }
}

class PhotoParceler : Parceler<Photo> {
    override fun Photo.write(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(Parcels.wrap(this), flags)
    }

    override fun create(parcel: Parcel): Photo {
        return Parcels.unwrap(parcel.readParcelable(Photo::class.java.classLoader))
    }
}

class ContactParceler : Parceler<Contact> {
    override fun Contact.write(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(Parcels.wrap(this), flags)
    }

    override fun create(parcel: Parcel): Contact {
        return Parcels.unwrap(parcel.readParcelable(Contact::class.java.classLoader))
    }
}