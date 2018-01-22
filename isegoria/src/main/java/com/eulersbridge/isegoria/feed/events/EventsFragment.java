package com.eulersbridge.isegoria.feed.events;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.eulersbridge.isegoria.network.api.models.Event;
import com.eulersbridge.isegoria.util.ui.TitledFragment;
import com.eulersbridge.isegoria.R;

import java.util.List;

public class EventsFragment extends Fragment implements TitledFragment {

    private final EventAdapter adapter = new EventAdapter();
    private SwipeRefreshLayout refreshLayout;

    private EventsViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.events_fragment, container, false);

        viewModel = ViewModelProviders.of(this).get(EventsViewModel.class);

        RecyclerView eventsListView = rootView.findViewById(R.id.events_list_view);
        eventsListView.setAdapter(adapter);

        refreshLayout = rootView.findViewById(R.id.events_refresh_layout);
        refreshLayout.setOnRefreshListener(() -> {
            refresh();

            refreshLayout.setRefreshing(true);
            refreshLayout.postDelayed(() -> refreshLayout.setRefreshing(false), 7000);
        });

        refresh();

        return rootView;
    }

    @Override
    public String getTitle(Context context) {
        return "Events";
    }

    private void refresh() {
        viewModel.getEvents().observe(this, this::setEvents);
    }

    private void setEvents(@Nullable List<Event> events) {
        adapter.setLoading(false);

        if (refreshLayout != null)
            refreshLayout.post(() -> refreshLayout.setRefreshing(false));

        if (events != null)
            adapter.replaceItems(events);
    }
}