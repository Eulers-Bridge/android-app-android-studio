package com.eulersbridge.isegoria.profile;


import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.eulersbridge.isegoria.GlideApp;
import com.eulersbridge.isegoria.IsegoriaApp;
import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.network.api.API;
import com.eulersbridge.isegoria.network.api.models.Task;
import com.eulersbridge.isegoria.util.ui.TitledFragment;

import java.lang.reflect.Field;
import java.util.List;

public class ProfileTaskProgressFragment extends Fragment implements TitledFragment {

    private View rootView;

    private TextView completedTextView;
    private RecyclerView completedListView;
    private TaskAdapter completedAdapter;

    private RecyclerView remainingListView;
    private TaskAdapter remainingAdapter;

    private ProgressBar progressBar;

    private ProfileViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.profile_task_progress_fragment, container, false);

        //noinspection ConstantConditions
        viewModel = ViewModelProviders.of(getParentFragment()).get(ProfileViewModel.class);

        viewModel.totalXp.observe(this, totalXp -> {
            if (totalXp != null) {
                setLevel(totalXp);
            }
        });

        progressBar = rootView.findViewById(R.id.profile_tasks_progress_bar);
        progressBar.getProgressDrawable().setColorFilter(Color.parseColor("#4FBF31"), PorterDuff.Mode.SRC_IN);

        completedTextView = rootView.findViewById(R.id.profile_tasks_progress_complete_text_view);
        completedListView = rootView.findViewById(R.id.profile_tasks_progress_completed_list_view);
        remainingListView = rootView.findViewById(R.id.profile_tasks_progress_remaining_list_view);

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        API api = ((IsegoriaApp)getActivity().getApplication()).getAPI();

        completedAdapter = new TaskAdapter(GlideApp.with(this), api);
        completedListView.setAdapter(completedAdapter);

        remainingAdapter = new TaskAdapter(GlideApp.with(this), api);
        remainingListView.setAdapter(remainingAdapter);

        fetchTasks();
    }

    @Override
    public void onDestroyView() {
        GlideApp.with(this).onDestroy();

        super.onDestroyView();
    }

    private void fetchTasks() {
        viewModel.getRemainingTasks().observe(this, remainingTasks -> {
            if (remainingTasks != null)
                setRemainingTasks(remainingTasks);
        });

        viewModel.getCompletedTasks().observe(this, completedTasks -> {
            if (completedTasks != null) {
                completedTextView.setVisibility(View.VISIBLE);
                completedAdapter.setItems(completedTasks);

            } else {
                completedTextView.setVisibility(View.GONE);
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

                taskLevelField.setText(getString(R.string.profile_tasks_progress_level, level));

                int nextLevelPoints = (int) totalXp + 500;
                nextLevelPoints = nextLevelPoints / 1000;
                nextLevelPoints = nextLevelPoints * 1000;

                if (nextLevelPoints == 0) nextLevelPoints = 1000;

                progressBar.setMax(nextLevelPoints);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    progressBar.setProgress((int)totalXp, true);
                } else {
                    progressBar.setProgress((int)totalXp);
                }

                taskLevelDesc.setText(getString(R.string.profile_tasks_progress_description, totalXp, nextLevelPoints));
            });
        }
    }

    private void setRemainingTasks(@NonNull List<Task> remainingTasks) {
        if (getActivity() != null) {
            remainingAdapter.setItems(remainingTasks);

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
        return context.getString(R.string.profile_progress_section_title);
    }
}