package com.eulersbridge.isegoria.network.api.models

import android.annotation.SuppressLint
import android.os.Parcelable
import com.squareup.moshi.Json
import kotlinx.android.parcel.Parcelize

@SuppressLint("ParcelCreator")
@Parcelize
class Contact (
    override var gender: String,
    override var nationality: String?,

    override var email: String,
    override var givenName: String,
    override var familyName: String,

    override var institutionId: Long?,

    override var level: Long = 0,
    override var experience: Long = 0,

    @Json(name = "numOfCompTasks")
    override var completedTasksCount: Long = 0,

    @Json(name = "numOfCompBadges")
    override var completedBadgesCount: Long = 0,

    @Json(name = "profilePhoto")
    override var profilePhotoURL: String?,

    // Subclass-specific

    @Json(name = "numOfContacts")
    val contactsCount: Long?,

    @Json(name = "totalTasks")
    var totalTasksCount: Long?,

    @Json(name = "totalBadges")
    var totalBadgesCount: Long?,

    @Json(name = "userId")
    val id: Long?

) : GenericUser, Parcelable
