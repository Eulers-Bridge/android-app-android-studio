package com.eulersbridge.isegoria.network.api.model

import android.annotation.SuppressLint
import android.os.Parcelable
import com.eulersbridge.isegoria.network.adapters.Timestamp
import com.squareup.moshi.Json
import kotlinx.android.parcel.Parcelize

@SuppressLint("ParcelCreator")
@Parcelize
data class NewsArticle(
        @field:Json(name = "articleId")
        val id: Long,

        val institutionId: Long?,
        val title: String,
        val content: String,
        val photos: List<Photo>?,

        @Timestamp
        val date: Long,

        @field:Json(name = "likes")
        val likeCount: Int = 0,

        val creatorEmail: String?,

        @field:Json(name = "creatorProfile")
        val creator: Contact,

        @field:Json(name = "inappropriateContent")
        val hasInappropriateContent: Boolean

) : Parcelable {
    internal fun getPhotoUrl()
        = photos?.firstOrNull()?.getPhotoUrl()
}