package com.eulersbridge.isegoria.network.api.response

import com.eulersbridge.isegoria.network.api.model.Photo

data class PhotosResponse (
    val totalPhotos: Long = 0,
    val photos: List<Photo>
)
