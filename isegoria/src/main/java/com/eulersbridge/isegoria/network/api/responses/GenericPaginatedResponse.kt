package com.eulersbridge.isegoria.network.api.responses

data class GenericPaginatedResponse<out T> (
    val foundObjects: T?,
    val totalElements: Long = 0
)
