package com.eulersbridge.isegoria.network.api.model

import android.annotation.SuppressLint
import android.os.Parcelable
import com.squareup.moshi.Json
import kotlinx.android.parcel.Parcelize

@SuppressLint("ParcelCreator")
@Parcelize
data class PollResult (
    @field:Json(name = "answer")
    val id: Long = 0,

    val count: Long = 0
) : Parcelable
