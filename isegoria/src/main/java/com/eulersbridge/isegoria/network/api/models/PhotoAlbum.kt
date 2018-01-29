package com.eulersbridge.isegoria.network.api.models

import android.annotation.SuppressLint
import android.os.Parcelable
import com.squareup.moshi.Json
import kotlinx.android.parcel.Parcelize

@SuppressLint("ParcelCreator")
@Parcelize
data class PhotoAlbum (
    @Json(name = "nodeId") val id: Int = 0,
    val name: String? = null,
    val description: String?,
    val location: String?,
    @Json(name = "thumbNailUrl") var thumbnailPhotoUrl: String?
) : Parcelable