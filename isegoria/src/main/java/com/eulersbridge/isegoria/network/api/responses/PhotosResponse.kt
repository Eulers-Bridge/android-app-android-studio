package com.eulersbridge.isegoria.network.api.responses

import com.eulersbridge.isegoria.network.api.models.Photo

data class PhotosResponse (
    val totalPhotos: Long = 0,
    val photos: List<Photo>?
)
