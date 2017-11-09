package com.eulersbridge.isegoria;

import android.app.ActionBar;
import android.graphics.Color;
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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.eulersbridge.isegoria.models.Badge;
import com.eulersbridge.isegoria.models.Contact;
import com.eulersbridge.isegoria.models.Task;
import com.eulersbridge.isegoria.models.User;
import com.eulersbridge.isegoria.network.PhotosResponse;
import com.eulersbridge.isegoria.network.SimpleCallback;
import com.eulersbridge.isegoria.views.CircularSeekBar;

import org.parceler.Parcels;

import java.util.List;

import retrofit2.Response;

public class ContactProfileFragment extends Fragment {
    private View rootView;

    private TextView friendsNumTextView;
    private TextView groupNumTextView;
    private TextView rewardsNumTextView;

    private CircularSeekBar circularSeekBar1;
    private CircularSeekBar circularSeekBar2;
    private CircularSeekBar circularSeekBar3;
    private CircularSeekBar circularSeekBar4;

    private Isegoria isegoria;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.profile_fragment, container, false);

        rootView.findViewById(R.id.profile_personality_test_button).setVisibility(View.GONE);

        isegoria = (Isegoria) getActivity().getApplication();

        int imageHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                (float) 100.00, getResources().getDisplayMetrics());

        ImageView photoImageView = rootView.findViewById(R.id.profile_image_small);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(imageHeight, imageHeight);
        photoImageView.setLayoutParams(layoutParams);

        friendsNumTextView = rootView.findViewById(R.id.friendsNum);
        groupNumTextView = rootView.findViewById(R.id.groupNum);
        rewardsNumTextView = rootView.findViewById(R.id.rewardsNum);

        long userId = isegoria.getLoggedInUser().getId();

        isegoria.getAPI().getRemainingBadges(userId).enqueue(new SimpleCallback<List<Badge>>() {
            @Override
            protected void handleResponse(Response<List<Badge>> response) {
                List<Badge> remainingBadges = response.body();
                if (remainingBadges != null) {
                    updateRemainingBadgesCount(remainingBadges.size());
                }
            }
        });

        circularSeekBar1 = rootView.findViewById(R.id.circularSeekBar1);
        circularSeekBar2 = rootView.findViewById(R.id.circularSeekBar2);
        circularSeekBar3 = rootView.findViewById(R.id.circularSeekBar3);
        circularSeekBar4 = rootView.findViewById(R.id.circularSeekBar4);

        circularSeekBar1.setCircleProgressColor(Color.parseColor("#2C9F47"));
        circularSeekBar2.setCircleProgressColor(Color.parseColor("#FFB400"));
        circularSeekBar3.setCircleProgressColor(Color.parseColor("#B61B1B"));

        Bundle bundle = this.getArguments();
        User user = Parcels.unwrap(bundle.getParcelable("profile"));

        final TextView name = rootView.findViewById(R.id.profile_name);
        name.setText(user.getFullName());

        updateCompletedBadgesCount(user.completedBadgesCount);
        updateCompletedTasksCount(user.completedTasksCount);
        updateExperience(user.level);

        // TODO: getUserDP; photoImageView for avatar, background on backgroundLinearLayout

        isegoria.getAPI().getTasks().enqueue(new SimpleCallback<List<Task>>() {
            @Override
            protected void handleResponse(Response<List<Task>> response) {
                List<Task> tasks = response.body();
                if (tasks != null) {
                    addTasks(tasks);
                }
            }
        });

        isegoria.getAPI().getContact(user.email).enqueue(new SimpleCallback<Contact>() {
            @Override
            protected void handleResponse(Response<Contact> response) {
                Contact user = response.body();
                if (user != null) {
                    updateStats(user.contactsCount, user.totalTasksCount);
                }
            }
        });

        return rootView;
    }

    @UiThread
    private void updateCompletedBadgesCount(long count) {
        circularSeekBar2.setTopLine(String.valueOf(count));
        circularSeekBar2.setProgress((int)count);

        if (circularSeekBar2.getMax() > 0) {
            Thread t2 = new Thread(circularSeekBar2);
            t2.start();
        }
    }

    @UiThread
    private void updateRemainingBadgesCount(long count) {
        circularSeekBar2.setBottomLine("/" + String.valueOf(count));
        circularSeekBar2.setMax((int)count);

        if (circularSeekBar2.getProgress() > 0) {
            Thread t2 = new Thread(circularSeekBar2);
            t2.start();
        }
    }

    @UiThread
    private void updateCompletedTasksCount(long count) {
        circularSeekBar3.setTopLine(String.valueOf(count));
        circularSeekBar3.setProgress((int)count);
        Thread t3 = new Thread(circularSeekBar3);
        t3.start();
    }

    @UiThread
    private void updateExperience(long experience) {
        circularSeekBar1.setTopLine(String.valueOf(experience));
        circularSeekBar1.setBottomLine("NEED " + (1000 - (experience % 1000)));
        circularSeekBar1.setProgress((int)experience);
        circularSeekBar1.setMax((int) ((experience / 1000) % 1000));
        Thread t1 = new Thread(circularSeekBar1);
        t1.start();
    }

    @UiThread
    private void updateStats(long numOfContacts, long totalTasks) {
        friendsNumTextView.setText(String.valueOf(numOfContacts));
        groupNumTextView.setText(String.valueOf("0"));
        rewardsNumTextView.setText(String.valueOf("0"));

        circularSeekBar3.setBottomLine("PER DAY");
        circularSeekBar4.setTopLine(String.valueOf("0"));
        circularSeekBar4.setBottomLine("ATTENDED");

        circularSeekBar3.setMax((int)totalTasks);

        circularSeekBar4.setProgress(30);
        Thread t4 = new Thread(circularSeekBar4);
        t4.start();
    }

    private void addTasks(@NonNull List<Task> tasks) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                for (Task task : tasks) {
                    addTask(task.id, task.action, task.xpValue);
                }
            });
        }
    }

    @UiThread
    private void addTask(long taskId, String action, long xpValue) {
        LinearLayout tasksLinearLayout = rootView.findViewById(R.id.tasksLayout);

        int paddingMargin1 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                (float) 43.33333333, getResources().getDisplayMetrics());
        int paddingMargin2 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                (float) 3.333333333, getResources().getDisplayMetrics());
        int paddingMargin3 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                (float) 10, getResources().getDisplayMetrics());

        RelativeLayout taskLayout = new RelativeLayout(getActivity());
        taskLayout.setGravity(Gravity.START);
        taskLayout.setLayoutParams(new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, paddingMargin1));

        LinearLayout leftLayout = new LinearLayout(getActivity());
        LinearLayout rightLayout = new LinearLayout(getActivity());

        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

        rightLayout.setLayoutParams(lp);

        LinearLayout.LayoutParams layoutParams =
                new LinearLayout.LayoutParams(paddingMargin1, paddingMargin1);
        ImageView iconImage = new ImageView(getActivity());
        iconImage.setScaleType(ImageView.ScaleType.FIT_XY);
        iconImage.setLayoutParams(layoutParams);
        iconImage.setPadding(paddingMargin2, 0, 0, 0);
        //iconImage.setBackgroundColor(Color.BLACK);

        isegoria.getAPI().getPhotos(taskId).enqueue(new SimpleCallback<PhotosResponse>() {
            @Override
            protected void handleResponse(Response<PhotosResponse> response) {
                PhotosResponse body = response.body();
                if (body != null && body.totalPhotos > 0) {
                    GlideApp.with(ContactProfileFragment.this)
                            .load(body.photos.get(0).thumbnailUrl)
                            .centerCrop()
                            .into(iconImage);
                }
            }
        });

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
