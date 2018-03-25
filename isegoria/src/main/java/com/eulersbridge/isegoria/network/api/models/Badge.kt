package com.eulersbridge.isegoria.network.api.models

import android.annotation.SuppressLint
import android.os.Parcelable
import com.squareup.moshi.Json
import kotlinx.android.parcel.Parcelize

@SuppressLint("ParcelCreator")
@Parcelize
data class Badge(
    @field:Json(name = "badgeId")
    val id: Long = 0,

    val name: String?,
    val description: String?,
    val level: Int = 0,
    val xpValue: Int = 0
) : Parcelable