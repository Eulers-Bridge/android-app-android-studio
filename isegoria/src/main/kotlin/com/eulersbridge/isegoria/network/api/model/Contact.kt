package com.eulersbridge.isegoria.network.api.model

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

        @field:Json(name = "numOfCompTasks")
        override var completedTasksCount: Long = 0,

        @field:Json(name = "numOfCompBadges")
        override var completedBadgesCount: Long = 0,

        @field:Json(name = "profilePhoto")
        override var profilePhotoURL: String?,

    // Subclass-specific

        @field:Json(name = "numOfContacts")
        val contactsCount: Long?,

        @field:Json(name = "totalTasks")
        var totalTasksCount: Long?,

        @field:Json(name = "userId")
        val id: Long?

) : GenericUser, Parcelable
