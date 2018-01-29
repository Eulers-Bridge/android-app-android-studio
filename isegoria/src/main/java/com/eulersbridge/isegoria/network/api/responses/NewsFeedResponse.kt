package com.eulersbridge.isegoria.network.api.responses

import com.squareup.moshi.Json

data class NewsFeedResponse (
    @Json(name = "nodeId")
    var newsFeedId: Long
)
