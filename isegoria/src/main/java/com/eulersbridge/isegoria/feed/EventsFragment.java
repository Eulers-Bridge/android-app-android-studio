package com.eulersbridge.isegoria.feed;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.eulersbridge.isegoria.Isegoria;
import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.models.Event;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EventsFragment extends Fragment {

    private final EventAdapter adapter = new EventAdapter(this);
    private SwipeRefreshLayout refreshLayout;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.events_fragment, container, false);

        Isegoria isegoria = (Isegoria)getActivity().getApplication();

        RecyclerView eventsListView = rootView.findViewById(R.id.events_list_view);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        eventsListView.setLayoutManager(layoutManager);
        eventsListView.setHasFixedSize(true);
        eventsListView.setAdapter(adapter);

        refreshLayout = rootView.findViewById(R.id.events_refresh_layout);
        refreshLayout.setOnRefreshListener(() -> {
            getEvents(isegoria);

            refreshLayout.setRefreshing(true);
            refreshLayout.postDelayed(() -> refreshLayout.setRefreshing(false), 7000);
        });

        getEvents(isegoria);

        return rootView;
    }

    private final Callback<List<Event>> callback = new Callback<List<Event>>() {
        @Override
        public void onResponse(Call<List<Event>> call, Response<List<Event>> response) {
            List<Event> events = response.body();
            setEvents(events);
        }

        @Override
        public void onFailure(Call<List<Event>> call, Throwable t) {
            t.printStackTrace();
        }
    };

    private void getEvents(Isegoria isegoria) {
        isegoria.getAPI().getEvents(isegoria.getLoggedInUser().institutionId).enqueue(callback);
    }

    private void setEvents(@Nullable List<Event> events) {
        if (refreshLayout != null) refreshLayout.post(() -> refreshLayout.setRefreshing(false));

        if (events != null) {
            adapter.replaceItems(events);
            adapter.notifyDataSetChanged();
        }
    }
}