package com.eulersbridge.isegoria.profile;


import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.eulersbridge.isegoria.Isegoria;
import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.models.Task;
import com.eulersbridge.isegoria.network.SimpleCallback;
import com.eulersbridge.isegoria.utilities.TitledFragment;

import java.lang.reflect.Field;
import java.util.List;

import retrofit2.Response;

public class TaskDetailProgressFragment extends Fragment implements TitledFragment {

    private Isegoria isegoria;

    private View rootView;

    private final TaskAdapter completedAdapter = new TaskAdapter(this);
    private final TaskAdapter remainingAdapter = new TaskAdapter(this);

    private RecyclerView remainingListView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.task_detail_fragment, container, false);

        isegoria = (Isegoria) getActivity().getApplication();

        ProgressBar progressBar = rootView.findViewById(R.id.profile_tasks_progress_bar);
        progressBar.setProgress(50);
        progressBar.setMax(1000);
        progressBar.getProgressDrawable().setColorFilter(Color.parseColor("#4FBF31"), PorterDuff.Mode.SRC_IN);

        RecyclerView completedListView = rootView.findViewById(R.id.profile_tasks_progress_completed_list_view);
        setupRecyclerView(completedListView, completedAdapter);

        remainingListView = rootView.findViewById(R.id.profile_tasks_progress_remaining_list_view);
        setupRecyclerView(remainingListView, remainingAdapter);

        fetchData();

        return rootView;
    }

    private void setupRecyclerView(RecyclerView recyclerView, RecyclerView.Adapter adapter) {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }

    private void fetchData() {
        long userId = isegoria.getLoggedInUser().getId();

        isegoria.getAPI().getRemainingTasks(userId).enqueue(new SimpleCallback<List<Task>>() {
            @Override
            protected void handleResponse(Response<List<Task>> response) {
                List<Task> tasks = response.body();
                if (tasks != null) setRemainingTasks(tasks);
            }
        });

        isegoria.getAPI().getCompletedTasks(userId).enqueue(new SimpleCallback<List<Task>>() {
            @Override
            protected void handleResponse(Response<List<Task>> response) {
                List<Task> tasks = response.body();
                if (tasks != null) setCompletedTasks(tasks);
            }
        });
    }

    @Override
    public void onDetach() {
        super.onDetach();

        // Work around a child fragment manager bug: https://stackoverflow.com/a/15656428/447697
        try {
            Field childFragmentManager = Fragment.class.getDeclaredField("mChildFragmentManager");
            childFragmentManager.setAccessible(true);
            childFragmentManager.set(this, null);

        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @UiThread
    private void setLevel(long totalXp) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                final TextView taskLevelField = rootView.findViewById(R.id.profile_tasks_progress_level_text_view);
                final TextView taskLevelDesc = rootView.findViewById(R.id.profile_tasks_progress_description_text_view);

                int level = ((int)totalXp / 1000) + 1;

                int nextLevelPoints = (int) totalXp + 500;
                nextLevelPoints = nextLevelPoints / 1000;
                nextLevelPoints = nextLevelPoints * 1000;

                if (nextLevelPoints == 0) nextLevelPoints = 1000;

                taskLevelField.setText(getString(R.string.profile_tasks_progress_level, level));
                taskLevelDesc.setText(getString(R.string.profile_tasks_progress_description, totalXp, nextLevelPoints));
            });
        }
    }

    private void setCompletedTasks(@NonNull List<Task> completedTasks) {
        if (getActivity() != null) {
            completedAdapter.replaceItems(completedTasks);
            completedAdapter.notifyDataSetChanged();

            long totalXp = 0;

            for (Task task : completedTasks) {
                totalXp += task.xpValue;
            }

            setLevel(totalXp);
        }
    }

    private void setRemainingTasks(@NonNull List<Task> remainingTasks) {
        if (getActivity() != null) {
            remainingAdapter.replaceItems(remainingTasks);
            remainingAdapter.notifyDataSetChanged();

            // Calculate rough new list view size to 'autosize' it
            getActivity().runOnUiThread(() -> {
                int heightDp = 44 * remainingTasks.size();

                int heightPx = Math.round(TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, heightDp, getResources().getDisplayMetrics()));

                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, heightPx);

                remainingListView.setLayoutParams(layoutParams);
            });
        }
    }

    @Override
    public String getTitle(Context context) {
        return getString(R.string.profile_progress_section_title);
    }
}