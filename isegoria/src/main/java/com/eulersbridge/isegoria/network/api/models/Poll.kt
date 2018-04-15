package com.eulersbridge.isegoria.network.api.models

import android.annotation.SuppressLint
import android.os.Parcelable
import com.squareup.moshi.Json
import kotlinx.android.parcel.Parcelize


@SuppressLint("ParcelCreator")
@Parcelize
data class Poll (

    @field:Json(name = "nodeId")
    val id: Long,

    val creatorEmail: String?,

    val creator: Contact?,

    val question: String?,

    @field:Json(name = "pollOptions")
    var options: List<PollOption>,

    val closed: Boolean = false
) : Parcelable