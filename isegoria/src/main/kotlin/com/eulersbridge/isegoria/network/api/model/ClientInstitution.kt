package com.eulersbridge.isegoria.network.api.model

data class ClientInstitution(
    val name: String?,
    val apiRoot: String?
) {
    override fun toString(): String {
        return name ?: ""
    }
}