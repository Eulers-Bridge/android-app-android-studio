package com.eulersbridge.isegoria.feed;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.common.LoadingAdapter;
import com.eulersbridge.isegoria.models.NewsArticle;

public class NewsAdapter extends LoadingAdapter<NewsArticle, NewsViewHolder> {

    NewsAdapter() {
        super(3);
    }

    @Override
    public NewsViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).
                inflate(R.layout.news_partial_grid_item, viewGroup, false);
        return new NewsViewHolder(itemView);
    }
}