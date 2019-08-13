package com.eulersbridge.isegoria.network.api.model

import android.annotation.SuppressLint
import android.os.Parcelable
import com.squareup.moshi.Json
import kotlinx.android.parcel.Parcelize

@SuppressLint("ParcelCreator")
@Parcelize
data class Candidate (
    @field:Json(name = "candidateId")
    val id: Long = 0,

    val ticketId: Long?,
    val positionId: Long = 0,
    val userId: Long = 0,
    val familyName: String?,
    val givenName: String?,
    val policyStatement: String?,
    val information: String?,
    val userProfile: User

) : Parcelable {
    val name: String
        get() = "$givenName $familyName"
}