package com.eulersbridge.isegoria.network.api.responses

import com.eulersbridge.isegoria.network.api.models.Photo

data class PhotosResponse (
    val photos: List<Photo>?,
    val totalPhotos: Long = 0
)
