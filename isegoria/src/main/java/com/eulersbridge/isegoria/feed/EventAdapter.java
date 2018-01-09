package com.eulersbridge.isegoria.feed;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.common.LoadingAdapter;
import com.eulersbridge.isegoria.models.Event;

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