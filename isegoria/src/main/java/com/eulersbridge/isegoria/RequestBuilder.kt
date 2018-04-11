package com.eulersbridge.isegoria

import okhttp3.Request

fun Request.Builder.addAppHeaders(): Request.Builder
        = this.addHeader("Accept", "application/json")
        .addHeader("Content-Type", "application/json")
        .addHeader("User-Agent", "IsegoriaApp Android")