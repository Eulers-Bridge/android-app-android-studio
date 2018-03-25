package com.eulersbridge.isegoria.network.api.models

import android.annotation.SuppressLint
import android.os.Parcelable
import com.squareup.moshi.Json
import kotlinx.android.parcel.Parcelize

@SuppressLint("ParcelCreator")
@Parcelize
data class Position (
    @field:Json(name = "positionId") var id: Long = 0,
    @field:Json(name = "electionId") val electionId: Long?,
    val name: String?,
    val description: String?
) : Parcelable
