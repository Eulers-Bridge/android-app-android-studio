package com.eulersbridge.isegoria;


import android.app.ActionBar;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;

public class TaskDetailProgressFragment extends SherlockFragment {
    private View rootView;

    private float dpWidth;
    private float dpHeight;

    private Isegoria isegoria;
    private Network network;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        DisplayMetrics displayMetrics = getActivity().getResources().getDisplayMetrics();
        this.isegoria = (Isegoria) getActivity().getApplication();
        rootView = inflater.inflate(R.layout.task_detail_fragment, container, false);

        dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        dpHeight = displayMetrics.heightPixels / displayMetrics.density;

        MainActivity mainActivity = (MainActivity) getActivity();
        network = mainActivity.getIsegoriaApplication().getNetwork();
        network.getRemainingTasks(this);

        ProgressBar pb = (ProgressBar) rootView.findViewById(R.id.progressBar);
        pb.setProgress(50);
        pb.setMax(100);
        pb.getProgressDrawable().setColorFilter(Color.parseColor("#4FBF31"), PorterDuff.Mode.SRC_IN);

        return rootView;
    }

    public void addCompletedTask(long taskId, String action, long xpValue) {
        LinearLayout tasksLinearLayout = (LinearLayout) rootView.findViewById(R.id.completedTasksLayout);

        RelativeLayout taskLayout = new RelativeLayout(getActivity());
        taskLayout.setGravity(Gravity.LEFT);
        taskLayout.setLayoutParams(new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 65));

        LinearLayout leftLayout = new LinearLayout(getActivity());
        LinearLayout rightLayout = new LinearLayout(getActivity());

        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

        rightLayout.setLayoutParams(lp);

        LinearLayout.LayoutParams layoutParams =
                new LinearLayout.LayoutParams(65, 65);
        ImageView iconImage = new ImageView(getActivity());
        iconImage.setScaleType(ImageView.ScaleType.FIT_XY);
        iconImage.setLayoutParams(layoutParams);
        iconImage.setPadding(5, 0, 0, 0);
        //iconImage.setBackgroundColor(Color.BLACK);

        network.getFirstPhoto((int) taskId, (int) taskId, iconImage);

        TextView taskLabel = new TextView(getActivity());
        taskLabel.setGravity(Gravity.CENTER_VERTICAL);
        taskLabel.setPadding(15, 15, 0, 0);

        TextView xpLabel = new TextView(getActivity());
        xpLabel.setGravity(Gravity.RIGHT);
        xpLabel.setPadding(0, 15, 15, 0);
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

    public void addRemainingTask(long taskId, String action, long xpValue) {
        LinearLayout tasksLinearLayout = (LinearLayout) rootView.findViewById(R.id.completedTasksLayout);

        RelativeLayout taskLayout = new RelativeLayout(getActivity());
        taskLayout.setGravity(Gravity.LEFT);
        taskLayout.setLayoutParams(new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 65));

        LinearLayout leftLayout = new LinearLayout(getActivity());
        LinearLayout rightLayout = new LinearLayout(getActivity());

        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

        rightLayout.setLayoutParams(lp);

        LinearLayout.LayoutParams layoutParams =
                new LinearLayout.LayoutParams(65, 65);
        ImageView iconImage = new ImageView(getActivity());
        iconImage.setScaleType(ImageView.ScaleType.FIT_XY);
        iconImage.setLayoutParams(layoutParams);
        iconImage.setPadding(5, 0, 0, 0);
        //iconImage.setBackgroundColor(Color.BLACK);

        network.getFirstPhoto((int) taskId, (int) taskId, iconImage);

        TextView taskLabel = new TextView(getActivity());
        taskLabel.setGravity(Gravity.CENTER_VERTICAL);
        taskLabel.setPadding(15, 15, 0, 0);

        TextView xpLabel = new TextView(getActivity());
        xpLabel.setGravity(Gravity.RIGHT);
        xpLabel.setPadding(0, 15, 15, 0);
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