package com.eulersbridge.isegoria.feed.news

import android.view.LayoutInflater
import android.view.ViewGroup
import com.eulersbridge.isegoria.R
import com.eulersbridge.isegoria.network.api.model.NewsArticle
import com.eulersbridge.isegoria.util.ui.LoadingAdapter

class NewsAdapter internal constructor() : LoadingAdapter<NewsArticle, NewsViewHolder>(3) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.news_partial_grid_item,
            parent, false)
        return NewsViewHolder(itemView)
    }
}