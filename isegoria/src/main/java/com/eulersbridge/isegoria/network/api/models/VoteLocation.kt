package com.eulersbridge.isegoria.network.api.models

import android.annotation.SuppressLint
import android.os.Parcelable
import com.squareup.moshi.Json
import kotlinx.android.parcel.Parcelize

@SuppressLint("ParcelCreator")
@Parcelize
data class VoteLocation (
    val ownerId: Long = 0,
    @field:Json(name = "votingLocationId") var id: Long = 0,
    val name: String?,
    val information: String?

) : Parcelable {
    override fun toString()
        = name ?: id.toString()
}