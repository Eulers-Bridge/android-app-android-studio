package com.eulersbridge.isegoria.feed;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.eulersbridge.isegoria.GlideApp;
import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.utilities.RecyclerViewItemClickListener;
import com.eulersbridge.isegoria.models.Event;
import com.eulersbridge.isegoria.utilities.TintTransformation;
import com.eulersbridge.isegoria.utilities.Utils;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

class EventAdapter extends RecyclerView.Adapter<EventViewHolder> implements RecyclerViewItemClickListener {
    final private Fragment fragment;
    final private List<Event> items = new ArrayList<>();

    EventAdapter(Fragment fragment) {
        this.fragment = fragment;
    }

    void replaceItems(List<Event> newItems) {
        items.clear();
        items.addAll(newItems);
    }

    @Override
    public int getItemCount() { return items.size(); }

    @Override
    public void onBindViewHolder(EventViewHolder viewHolder, int index) {
        final Event item = items.get(index);

        viewHolder.titleTextView.setText(item.name);

        String dateTime = Utils.convertTimestampToString(fragment.getContext(), item.date);
        viewHolder.detailsTextView.setText(dateTime);

        GlideApp.with(fragment)
                .load(item.getPhotoUrl())
                .placeholder(R.color.grey)
                .transform(new TintTransformation())
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(viewHolder.imageView);
    }

    @Override
    public void onItemClick(View view, int position) {
        Activity activity = fragment.getActivity();
        if (activity == null) return;

        int[] location = new int[] {0,0};
        view.getLocationOnScreen(location);

        Intent activityIntent = new Intent(activity, EventDetailActivity.class);

        Bundle extras = new Bundle();
        extras.putParcelable("event", Parcels.wrap(items.get(position)));
        activityIntent.putExtras(extras);

        //Animate with a scale-up transition between the activities
        Bundle options = ActivityOptionsCompat.makeScaleUpAnimation(view, location[0],
                location[1], view.getWidth(), view.getHeight()).toBundle();

        ActivityCompat.startActivity(activity, activityIntent, options);
    }

    @Override
    public EventViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).
                inflate(R.layout.events_partial_list_item, viewGroup, false);
        return new EventViewHolder(itemView, this);
    }
}