package com.eulersbridge.isegoria.network.api.response

import com.eulersbridge.isegoria.network.api.model.NewsArticle
import com.eulersbridge.isegoria.network.api.model.User

data class LoginResponse (
    val articles: List<NewsArticle>?,
    val user: User,
    val userId: Long
)
