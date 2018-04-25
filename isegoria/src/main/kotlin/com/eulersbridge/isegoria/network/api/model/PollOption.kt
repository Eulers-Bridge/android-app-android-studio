package com.eulersbridge.isegoria.network.api.model

import android.annotation.SuppressLint
import android.os.Parcelable
import com.squareup.moshi.Json
import kotlinx.android.parcel.Parcelize

@SuppressLint("ParcelCreator")
@Parcelize
data class PollOption (

    val id: Long,

    @field:Json(name = "txt")
    val text: String?,

    val photo: Photo?,

    @field:Json(name = "numOfVoters")
    val voterCount: Long?,

    @field:Json(name = "voted")
    var hasVoted: Boolean,

    @Transient
    var result: PollResult?
) : Parcelable