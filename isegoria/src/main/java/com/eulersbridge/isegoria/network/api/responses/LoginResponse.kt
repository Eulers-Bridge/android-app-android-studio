package com.eulersbridge.isegoria.network.api.responses

import com.eulersbridge.isegoria.network.api.models.NewsArticle
import com.eulersbridge.isegoria.network.api.models.User

data class LoginResponse (
    val articles: List<NewsArticle>?,
    val user: User?,
    val userId: Long
)
