package com.eulersbridge.isegoria.feed.events

import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.view.postDelayed
import com.eulersbridge.isegoria.R
import com.eulersbridge.isegoria.network.api.models.Event
import com.eulersbridge.isegoria.observe
import com.eulersbridge.isegoria.util.ui.TitledFragment
import kotlinx.android.synthetic.main.events_fragment.*

class EventsFragment : Fragment(), TitledFragment {

    private val adapter = EventAdapter()
    private val viewModel: EventsViewModel by lazy {
        ViewModelProviders.of(activity!!).get(EventsViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.events_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        listView.adapter = adapter

        refreshLayout.setOnRefreshListener {
            refresh()

            refreshLayout.isRefreshing = true
            refreshLayout.postDelayed(7000) { refreshLayout.isRefreshing = false }
        }

        refresh()
    }

    override fun getTitle(context: Context?) = "Events"

    private fun refresh() {
        observe(viewModel.getEvents()) {
            setEvents(it)
        }
    }

    private fun setEvents(events: List<Event>?) {
        adapter.isLoading = false

        refreshLayout.post { refreshLayout.isRefreshing = false }

        if (events != null)
            adapter.replaceItems(events)
    }
}