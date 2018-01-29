package com.eulersbridge.isegoria.network.api.models

import android.annotation.SuppressLint
import android.os.Parcelable
import com.squareup.moshi.Json
import kotlinx.android.parcel.Parcelize

@SuppressLint("ParcelCreator")
@Parcelize
data class Position (
    @Json(name = "positionId") var id: Long = 0,
    @Json(name = "electionId") val electionId: Long?,
    val name: String?,
    val description: String?
) : Parcelable
