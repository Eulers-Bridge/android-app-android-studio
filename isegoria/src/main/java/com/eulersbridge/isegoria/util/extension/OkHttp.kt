package com.eulersbridge.isegoria.util.extension

import io.reactivex.Single
import okhttp3.Request
import okhttp3.Response

fun Request.Builder.addAppHeaders(): Request.Builder
        = addHeader("Accept", "application/json")
        .addHeader("Content-Type", "application/json")
        .addHeader("User-Agent", "IsegoriaApp Android")

fun Response.toSingle(): Single<Boolean> = Single.fromCallable { this }.map { it -> it.isSuccessful }