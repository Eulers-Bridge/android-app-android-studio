package com.eulersbridge.isegoria.feed.news;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.ViewCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.eulersbridge.isegoria.GlideApp;
import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.network.api.models.NewsArticle;
import com.eulersbridge.isegoria.util.Constants;
import com.eulersbridge.isegoria.util.Strings;
import com.eulersbridge.isegoria.util.transformation.RoundedCornersTransformation;
import com.eulersbridge.isegoria.util.transformation.TintTransformation;
import com.eulersbridge.isegoria.util.ui.LoadingAdapter;

import org.parceler.Parcels;

class NewsViewHolder extends LoadingAdapter.ItemViewHolder<NewsArticle> {

    private NewsArticle item;

    final private ImageView imageView;
    final private TextView titleTextView;
    final private TextView dateTextView;

    private boolean isImageLoadStarted = false;

    NewsViewHolder(View itemView) {
        super(itemView);

        imageView = itemView.findViewById(R.id.news_grid_item_image_view);

        titleTextView = itemView.findViewById(R.id.news_grid_item_title_text_view);
        dateTextView = itemView.findViewById(R.id.news_grid_item_date_text_view);

        imageView.setOnClickListener(view -> {
            if (item == null) return;

            int[] location = new int[] {0,0};
            view.getLocationOnScreen(location);

            Intent activityIntent = new Intent(view.getContext(), NewsDetailActivity.class);

            Bundle extras = new Bundle();
            extras.putParcelable(Constants.ACTIVITY_EXTRA_NEWS_ARTICLE, Parcels.wrap(item));
            activityIntent.putExtras(extras);

            //Animate with a scale-up transition between the activities
            Bundle options = ActivityOptionsCompat.makeScaleUpAnimation(
                    view,
                    location[0],
                    location[1],
                    view.getWidth(),
                    view.getHeight())
                    .toBundle();

            ActivityCompat.startActivity(view.getContext(), activityIntent, options);
        });
    }

    @Override
    protected void onRecycled() {
        if (imageView.getContext() != null && isImageLoadStarted)
            GlideApp.with(imageView.getContext()).clear(imageView);
    }

    @Override
    protected void setItem(@Nullable NewsArticle item) {
        this.item = item;

        @DrawableRes int placeholderRes = R.drawable.round_rect_placeholder;

        if (item == null) {
            ViewCompat.setTransitionName(titleTextView, null);

            imageView.setBackgroundResource(placeholderRes);
            ViewCompat.setTransitionName(imageView, null);

        } else {
            titleTextView.setText(item.title);

            String dateTime = Strings.fromTimestamp(dateTextView.getContext(), item.dateTimestamp);
            dateTextView.setText(dateTime);

            ViewCompat.setTransitionName(titleTextView, item.title+"TextView");
            ViewCompat.setTransitionName(imageView, item.title+"ImageView");

            GlideApp.with(imageView.getContext())
                    .load(item.getPhotoUrl())
                    .placeholder(placeholderRes)
                    .transforms(new CenterCrop(), new TintTransformation(), new RoundedCornersTransformation())
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(imageView);

            isImageLoadStarted = true;
        }
    }
}