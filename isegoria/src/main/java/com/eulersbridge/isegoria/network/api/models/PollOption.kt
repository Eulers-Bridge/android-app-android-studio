package com.eulersbridge.isegoria.network.api.models

import android.annotation.SuppressLint
import android.os.Parcelable
import com.squareup.moshi.Json
import kotlinx.android.parcel.Parcelize

@SuppressLint("ParcelCreator")
@Parcelize
data class PollOption (

    val id: Long,

    @Json(name = "txt")
    val text: String?,

    val photo: Photo?,

    @Json(name = "numOfVoters")
    val votersCount: Long?,

    @Json(name = "voted")
    var hasVoted: Boolean,

    @Transient
    var result: PollResult?
) : Parcelable