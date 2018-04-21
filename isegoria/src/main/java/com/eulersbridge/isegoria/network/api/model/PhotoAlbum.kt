package com.eulersbridge.isegoria.network.api.model

import android.annotation.SuppressLint
import android.os.Parcelable
import com.squareup.moshi.Json
import kotlinx.android.parcel.Parcelize

@SuppressLint("ParcelCreator")
@Parcelize
data class PhotoAlbum (
    @field:Json(name = "nodeId") val id: Long = 0,
    val name: String? = null,
    val description: String?,
    val location: String?,
    @field:Json(name = "thumbNailUrl") var thumbnailUrl: String?
) : Parcelable