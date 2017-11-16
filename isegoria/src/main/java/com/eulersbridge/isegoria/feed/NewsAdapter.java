package com.eulersbridge.isegoria.feed;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.eulersbridge.isegoria.Constant;
import com.eulersbridge.isegoria.GlideApp;
import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.models.NewsArticle;
import com.eulersbridge.isegoria.utilities.ClickableViewHolder;
import com.eulersbridge.isegoria.utilities.TintTransformation;
import com.eulersbridge.isegoria.utilities.Utils;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

public class NewsAdapter extends RecyclerView.Adapter<NewsViewHolder> implements ClickableViewHolder.ClickListener {
    final private Fragment fragment;
    final private List<NewsArticle> items = new ArrayList<>();

    NewsAdapter(Fragment fragment) {
        this.fragment = fragment;
    }

    void replaceItems(@NonNull List<NewsArticle> newItems) {
        items.clear();
        items.addAll(newItems);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public void onBindViewHolder(NewsViewHolder viewHolder, int index) {
        NewsArticle item = items.get(index);

        viewHolder.titleTextView.setText(item.title);

        String dateTime = Utils.convertTimestampToString(fragment.getContext(), item.dateTimestamp);
        viewHolder.dateTextView.setText(dateTime);

        GlideApp.with(fragment)
                .load(item.getPhotoUrl())
                .placeholder(R.color.grey)
                .transforms(new CenterCrop(), new TintTransformation())
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(viewHolder.imageView);
    }

    @Override
    public void onItemClick(RecyclerView.ViewHolder viewHolder, int position) {
        Activity activity = fragment.getActivity();
        if (activity == null) return;

        int[] location = new int[] {0,0};
        viewHolder.itemView.getLocationOnScreen(location);

        Intent activityIntent = new Intent(activity, NewsDetailActivity.class);

        Bundle extras = new Bundle();
        extras.putParcelable(Constant.ACTIVITY_EXTRA_NEWS_ARTICLE, Parcels.wrap(items.get(position)));
        activityIntent.putExtras(extras);

        //Animate with a scale-up transition between the activities
        Bundle options = ActivityOptionsCompat.makeScaleUpAnimation(viewHolder.itemView, location[0],
                location[1], viewHolder.itemView.getWidth(), viewHolder.itemView.getHeight()).toBundle();

        ActivityCompat.startActivity(activity, activityIntent, options);
    }

    @Override
    public NewsViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).
                inflate(R.layout.news_partial_grid_item, viewGroup, false);
        return new NewsViewHolder(itemView, this);
    }
}