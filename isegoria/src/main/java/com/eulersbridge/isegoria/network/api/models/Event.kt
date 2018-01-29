package com.eulersbridge.isegoria.network.api.models

import android.annotation.SuppressLint
import android.os.Parcelable
import com.eulersbridge.isegoria.network.adapters.Timestamp
import com.squareup.moshi.Json
import kotlinx.android.parcel.Parcelize

@SuppressLint("ParcelCreator")
@Parcelize
data class Event(
    val name: String?,
    val location: String?,
    val description: String?,
    val organizer: String?,
    val organizerEmail: String?,
    @Json(name = "created") @Timestamp val date: Long = 0,
    val photos: List<Photo>?
) : Parcelable {

    fun getPhotoUrl(): String? {
        return if (photos != null && photos.isNotEmpty()) {
            photos[0].thumbnailUrl
        } else {
            null
        }
    }
}