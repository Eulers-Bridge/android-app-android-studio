package com.eulersbridge.isegoria.network.api.models

/*abstract class GenericUser (
    open var gender: String,
    open var nationality: String,

    open var email: String,
    open var givenName: String,
    open var familyName: String,

    open var institutionId: Long? = null,

    open var level: Long = 0,
    open var experience: Long = 0,

    @Json(name = "numOfCompTasks") open var completedTasksCount: Long = 0,
    @Json(name = "numOfCompBadges") open var completedBadgesCount: Long = 0,

    @Json(name = "profilePhoto") open var profilePhotoURL: String

) {
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
}*/

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