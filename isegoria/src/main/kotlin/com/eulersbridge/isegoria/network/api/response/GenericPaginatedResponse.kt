package com.eulersbridge.isegoria.network.api.response

data class GenericPaginatedResponse<out T> (
    val foundObjects: T?,
    val totalElements: Long = 0
)
