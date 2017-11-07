package com.eulersbridge.isegoria.profile;


import android.app.ActionBar;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.eulersbridge.isegoria.MainActivity;
import com.eulersbridge.isegoria.Network;
import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.models.Task;

import java.util.ArrayList;

public class TaskDetailProgressFragment extends Fragment {
    private View rootView;

    private Network network;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.task_detail_fragment, container, false);

        MainActivity mainActivity = (MainActivity) getActivity();
        network = mainActivity.getIsegoriaApplication().getNetwork();

        network.getRemainingTasks(new Network.TasksTotalXPListener() {
            @Override
            public void onFetchSuccess(ArrayList<Task> remainingTasks, long totalXp) {
                addRemainingTasks(remainingTasks);
            }

            @Override
            public void onFetchFailure(Exception e) {}
        });

        network.getCompletedTasks(new Network.TasksTotalXPListener() {
            @Override
            public void onFetchSuccess(ArrayList<Task> completedTasks, long totalXp) {
                addCompletedTasks(completedTasks);
                setLevel(totalXp);
            }

            @Override
            public void onFetchFailure(Exception e) {}
        });

        ProgressBar pb = rootView.findViewById(R.id.progressBar);
        pb.setProgress(50);
        pb.setMax(1000);
        pb.getProgressDrawable().setColorFilter(Color.parseColor("#4FBF31"), PorterDuff.Mode.SRC_IN);

        return rootView;
    }

    @UiThread
    private void setLevel(long totalXp) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                final TextView taskLevelField = rootView.findViewById(R.id.taskLevelField);
                final TextView taskLevelDesc = rootView.findViewById(R.id.taskLevelDesc);

                int level = ((int)totalXp / 1000) + 1;

                int nextLevelPoints = (int) totalXp + 500;
                nextLevelPoints = nextLevelPoints / 1000;
                nextLevelPoints = nextLevelPoints * 1000;

                if(nextLevelPoints == 0)
                    nextLevelPoints = 1000;

                taskLevelField.setText(String.format("Level %d", level));
                taskLevelDesc.setText(String.format("%d out of %d XP till the next level!", totalXp, nextLevelPoints));
            });
        }
    }

    private void addCompletedTasks(ArrayList<Task> completedTasks) {
        if (getActivity() != null && completedTasks.size() > 0) {
            getActivity().runOnUiThread(() -> {
                for (Task task : completedTasks) {
                    addCompletedTask(task.getId(), task.getAction(), task.getXpValue());
                }
            });
        }
    }

    @UiThread
    public void addCompletedTask(long taskId, String action, long xpValue) {
        LinearLayout tasksLinearLayout = rootView.findViewById(R.id.completedTasksLayout);

        int paddingMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                (float) 43.33333333, getResources().getDisplayMetrics());
        int paddingMargin2 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                (float) 3.333333333, getResources().getDisplayMetrics());
        int paddingMargin3 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                (float) 10, getResources().getDisplayMetrics());

        RelativeLayout taskLayout = new RelativeLayout(getActivity());
        taskLayout.setGravity(Gravity.START);
        taskLayout.setLayoutParams(new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, paddingMargin));

        LinearLayout leftLayout = new LinearLayout(getActivity());
        LinearLayout rightLayout = new LinearLayout(getActivity());

        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

        rightLayout.setLayoutParams(lp);

        LinearLayout.LayoutParams layoutParams =
                new LinearLayout.LayoutParams(paddingMargin, paddingMargin);
        ImageView iconImage = new ImageView(getActivity());
        iconImage.setScaleType(ImageView.ScaleType.FIT_XY);
        iconImage.setLayoutParams(layoutParams);
        iconImage.setPadding(paddingMargin2, 0, 0, 0);
        //iconImage.setBackgroundColor(Color.BLACK);

        network.getFirstPhoto((int) taskId, iconImage);

        TextView taskLabel = new TextView(getActivity());
        taskLabel.setGravity(Gravity.CENTER_VERTICAL);
        taskLabel.setPadding(paddingMargin3, paddingMargin3, 0, 0);

        TextView xpLabel = new TextView(getActivity());
        xpLabel.setGravity(Gravity.END);
        xpLabel.setPadding(0, paddingMargin3, paddingMargin3, 0);
        xpLabel.setText(String.valueOf(xpValue) + " XP");

        taskLabel.setText(action);

        leftLayout.addView(iconImage);
        leftLayout.addView(taskLabel);
        rightLayout.addView(xpLabel);

        taskLayout.addView(leftLayout);
        taskLayout.addView(rightLayout);

        View divider = new View(getActivity());
        divider.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));
        divider.setBackgroundColor(Color.parseColor("#838a8a8a"));

        tasksLinearLayout.addView(divider);
        tasksLinearLayout.addView(taskLayout);

        divider = new View(getActivity());
        divider.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));
        divider.setBackgroundColor(Color.parseColor("#838a8a8a"));
        tasksLinearLayout.addView(divider);
    }

    private void addRemainingTasks(ArrayList<Task> remainingTasks) {
        if (getActivity() != null && remainingTasks.size() > 0) {
            getActivity().runOnUiThread(() -> {
                for (Task task : remainingTasks) {
                    addCompletedTask(task.getId(), task.getAction(), task.getXpValue());
                }
            });
        }
    }

    public void addRemainingTask(long taskId, String action, long xpValue) {
        LinearLayout tasksLinearLayout = rootView.findViewById(R.id.remainingTasksLayout);

        int paddingMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                (float) 43.33333333, getResources().getDisplayMetrics());
        int paddingMargin2 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                (float) 3.333333333, getResources().getDisplayMetrics());
        int paddingMargin3 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                (float) 10, getResources().getDisplayMetrics());

        RelativeLayout taskLayout = new RelativeLayout(getActivity());
        taskLayout.setGravity(Gravity.START);
        taskLayout.setLayoutParams(new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, paddingMargin));

        LinearLayout leftLayout = new LinearLayout(getActivity());
        LinearLayout rightLayout = new LinearLayout(getActivity());

        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

        rightLayout.setLayoutParams(lp);

        LinearLayout.LayoutParams layoutParams =
                new LinearLayout.LayoutParams(paddingMargin, paddingMargin);
        ImageView iconImage = new ImageView(getActivity());
        iconImage.setScaleType(ImageView.ScaleType.FIT_XY);
        iconImage.setLayoutParams(layoutParams);
        iconImage.setPadding(paddingMargin2, 0, 0, 0);
        //iconImage.setBackgroundColor(Color.BLACK);

        network.getFirstPhoto((int) taskId, iconImage);

        TextView taskLabel = new TextView(getActivity());
        taskLabel.setGravity(Gravity.CENTER_VERTICAL);
        taskLabel.setPadding(paddingMargin3, paddingMargin3, 0, 0);

        TextView xpLabel = new TextView(getActivity());
        xpLabel.setGravity(Gravity.END);
        xpLabel.setPadding(0, paddingMargin3, paddingMargin3, 0);
        xpLabel.setText(String.valueOf(xpValue) + " XP");

        taskLabel.setText(action);

        leftLayout.addView(iconImage);
        leftLayout.addView(taskLabel);
        rightLayout.addView(xpLabel);

        taskLayout.addView(leftLayout);
        taskLayout.addView(rightLayout);

        View divider = new View(getActivity());
        divider.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));
        divider.setBackgroundColor(Color.parseColor("#838a8a8a"));

        tasksLinearLayout.addView(divider);
        tasksLinearLayout.addView(taskLayout);

        divider = new View(getActivity());
        divider.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));
        divider.setBackgroundColor(Color.parseColor("#838a8a8a"));
        tasksLinearLayout.addView(divider);
    }
}