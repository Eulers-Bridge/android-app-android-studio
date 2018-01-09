package com.eulersbridge.isegoria.feed;

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

import com.eulersbridge.isegoria.Isegoria;
import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.common.TitledFragment;
import com.eulersbridge.isegoria.models.Event;
import com.eulersbridge.isegoria.models.User;
import com.eulersbridge.isegoria.network.SimpleCallback;

import java.util.List;

import retrofit2.Response;

public class EventsFragment extends Fragment implements TitledFragment {

    private final EventAdapter adapter = new EventAdapter();
    private SwipeRefreshLayout refreshLayout;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.events_fragment, container, false);

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

    private final SimpleCallback<List<Event>> eventsCallback = new SimpleCallback<List<Event>>() {
        @Override
        protected void handleResponse(Response<List<Event>> response) {
            List<Event> events = response.body();
            setEvents(events);
        }
    };

    private void refresh() {
        if (getActivity() != null) {
            Isegoria isegoria = (Isegoria)getActivity().getApplication();

            User user = isegoria.getLoggedInUser();

            if (user != null && user.institutionId != null)
                isegoria.getAPI().getEvents(user.institutionId).enqueue(eventsCallback);
        }
    }

    private void setEvents(@Nullable List<Event> events) {
        adapter.setLoading(false);

        if (refreshLayout != null)
            refreshLayout.post(() -> refreshLayout.setRefreshing(false));

        if (events != null)
            adapter.replaceItems(events);
    }
}