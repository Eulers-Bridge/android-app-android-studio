package com.eulersbridge.isegoria.feed.events

import android.view.LayoutInflater
import android.view.ViewGroup
import com.eulersbridge.isegoria.R
import com.eulersbridge.isegoria.network.api.models.Event
import com.eulersbridge.isegoria.util.ui.LoadingAdapter

internal class EventAdapter : LoadingAdapter<Event, EventViewHolder>(1) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.events_list_item,
            parent, false)
        return EventViewHolder(itemView)
    }
}