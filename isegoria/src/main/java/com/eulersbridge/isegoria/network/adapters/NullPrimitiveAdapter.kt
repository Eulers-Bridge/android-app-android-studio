package com.eulersbridge.isegoria.network.adapters

import com.squareup.moshi.FromJson

class NullPrimitiveAdapter {

    @FromJson
    fun intFromJson(value: Int?) = value ?: 0

    @FromJson
    fun booleanFromJson(value: Boolean?) = value ?: false

    @FromJson
    fun doubleFromJson(value: Double?) = value ?: 0.0

    @FromJson
    fun longFromJson(value: Long?) = value ?: 0
}
