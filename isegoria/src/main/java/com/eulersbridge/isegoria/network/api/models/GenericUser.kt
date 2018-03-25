package com.eulersbridge.isegoria.network.api.models


interface GenericUser {
    var gender: String
    var nationality: String?

    var email: String
    var givenName: String
    var familyName: String

    var institutionId: Long?

    var level: Long
    var experience: Long

    var completedTasksCount: Long
    var completedBadgesCount: Long

    var profilePhotoURL: String?

    val fullName: String
        get() = if (givenName.isBlank() && familyName.isBlank()) {
            ""

        } else if (familyName.isBlank()) {
            givenName

        } else if (givenName.isBlank()) {
            familyName

        } else {
            "$givenName $familyName"
        }
}