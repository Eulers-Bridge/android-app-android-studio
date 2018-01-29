package com.eulersbridge.isegoria.network.api.models

import android.annotation.SuppressLint
import android.os.Parcelable
import com.eulersbridge.isegoria.network.adapters.Timestamp
import com.squareup.moshi.Json
import kotlinx.android.parcel.Parcelize

@SuppressLint("ParcelCreator")
@Parcelize
data class Photo(
    @Json(name = "nodeId")
    val id: Int = 0,

    val title: String?,
    val description: String?,

    @Json(name = "url")
    val thumbnailUrl: String?,

    @Timestamp
    val date: Long = 0,

    @Json(name = "numOfLikes")
    val likeCount: Int = 0,

    @Json(name = "inappropriateContent")
    val hasInappropriateContent: Boolean = false
) : Parcelable