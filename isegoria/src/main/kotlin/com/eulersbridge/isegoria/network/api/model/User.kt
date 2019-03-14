package com.eulersbridge.isegoria.network.api.model

import android.annotation.SuppressLint
import android.os.Parcelable
import com.squareup.moshi.Json
import kotlinx.android.parcel.Parcelize

@SuppressLint("ParcelCreator")
@Parcelize
data class User (
    override var gender: String,
    override var nationality: String?,

    override var email: String,
    override var givenName: String,
    override var familyName: String,

    override var institutionId: Long?,

    override var level: Long = 0,
    override var experience: Long = 0,

    @field:Json(name = "numOfCompTasks")
    override var completedTasksCount: Long = 0,

    @field:Json(name = "numOfCompBadges")
    override var completedBadgesCount: Long = 0,

    @field:Json(name = "profilePhoto")
    override var profilePhotoURL: String?,

    // Subclass-specific
    @Transient var newsFeedId: Long = 0,
    val accountVerified: Boolean = false,

    var hasPPSEQuestions: Boolean = false,
    var hasPersonality: Boolean = false,

    var trackingOff: Boolean = false,
    @field:Json(name = "optOutDataCollection")
    var isOptedOutOfDataCollection: Boolean = false,

    val yearOfBirth: String,

    @Transient internal var id: Long?

) : GenericUser, Parcelable {
    fun getId(): Long {
        return id ?: 0
    }
}