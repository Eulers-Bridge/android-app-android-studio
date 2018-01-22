package com.eulersbridge.isegoria.feed.events;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.eulersbridge.isegoria.GlideApp;
import com.eulersbridge.isegoria.util.Strings;
import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.util.Constants;
import com.eulersbridge.isegoria.util.ui.LoadingAdapter;
import com.eulersbridge.isegoria.util.transformation.TintTransformation;
import com.eulersbridge.isegoria.network.api.models.Event;

import org.parceler.Parcels;

class EventViewHolder extends LoadingAdapter.ItemViewHolder<Event> implements View.OnClickListener {

    private Event item;

    final private ImageView imageView;
    final private TextView titleTextView;
    final private TextView detailsTextView;

    EventViewHolder(View view) {
        super(view);

        view.setOnClickListener(this);

        imageView = view.findViewById(R.id.event_list_image_view);
        titleTextView = view.findViewById(R.id.event_list_title_text_view);
        detailsTextView = view.findViewById(R.id.event_list_details_text_view);
    }

    @Override
    protected void setItem(@Nullable Event item) {
        this.item = item;

        @ColorRes int placeholderRes = R.color.lightGrey;

        if (item == null) {
            imageView.setBackgroundResource(placeholderRes);

        } else {
            titleTextView.setText(item.name);

            String dateTime = Strings.fromTimestamp(detailsTextView.getContext(), item.date);
            detailsTextView.setText(dateTime);

            GlideApp.with(imageView.getContext())
                    .load(item.getPhotoUrl())
                    .placeholder(placeholderRes)
                    .transform(new TintTransformation())
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(imageView);
        }
    }

    @Override
    protected void onRecycled() {
        GlideApp.with(imageView.getContext()).clear(imageView);
    }

    @Override
    public void onClick(View view) {
        if (item == null) return;

        int[] location = new int[] {0,0};
        view.getLocationOnScreen(location);

        Intent activityIntent = new Intent(view.getContext(), EventDetailActivity.class);

        Bundle extras = new Bundle();
        extras.putParcelable(Constants.ACTIVITY_EXTRA_EVENT, Parcels.wrap(item));
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
    }
}