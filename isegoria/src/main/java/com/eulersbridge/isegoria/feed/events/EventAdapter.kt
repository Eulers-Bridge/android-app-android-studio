package com.eulersbridge.isegoria.feed.events

import android.view.LayoutInflater
import android.view.ViewGroup
import com.eulersbridge.isegoria.R
import com.eulersbridge.isegoria.network.api.models.Event
import com.eulersbridge.isegoria.util.ui.LoadingAdapter

internal class EventAdapter : LoadingAdapter<Event, EventViewHolder>(1) {

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): EventViewHolder {
        val itemView = LayoutInflater.from(viewGroup.context).inflate(R.layout.events_list_item, viewGroup, false)
        return EventViewHolder(itemView)
    }
}