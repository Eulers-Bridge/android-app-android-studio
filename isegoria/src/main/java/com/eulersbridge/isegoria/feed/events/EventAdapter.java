package com.eulersbridge.isegoria.feed.events;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.eulersbridge.isegoria.network.api.models.Event;
import com.eulersbridge.isegoria.util.ui.LoadingAdapter;
import com.eulersbridge.isegoria.R;

class EventAdapter extends LoadingAdapter<Event, EventViewHolder> {

    EventAdapter() {
        super(1);
    }

    @Override
    public EventViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).
                inflate(R.layout.events_list_item, viewGroup, false);
        return new EventViewHolder(itemView);
    }
}