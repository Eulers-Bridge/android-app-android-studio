package com.eulersbridge.isegoria;

import android.graphics.Color;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.eulersbridge.isegoria.models.Badge;
import com.eulersbridge.isegoria.models.Contact;
import com.eulersbridge.isegoria.models.Task;
import com.eulersbridge.isegoria.models.User;
import com.eulersbridge.isegoria.network.SimpleCallback;
import com.eulersbridge.isegoria.profile.TaskAdapter;
import com.eulersbridge.isegoria.views.CircularSeekBar;

import org.parceler.Parcels;

import java.util.List;

import retrofit2.Response;

public class ContactProfileFragment extends Fragment {

    private TextView friendsNumTextView;
    private TextView groupNumTextView;
    private TextView rewardsNumTextView;

    private CircularSeekBar circularSeekBar1;
    private CircularSeekBar circularSeekBar2;
    private CircularSeekBar circularSeekBar3;
    private CircularSeekBar circularSeekBar4;

    private final TaskAdapter taskAdapter = new TaskAdapter(this);

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.profile_fragment, container, false);

        rootView.findViewById(R.id.profile_personality_test_button).setVisibility(View.GONE);

        Isegoria isegoria = (Isegoria) getActivity().getApplication();

        int imageHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                (float) 100.00, getResources().getDisplayMetrics());

        ImageView photoImageView = rootView.findViewById(R.id.profile_image_small);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(imageHeight, imageHeight);
        photoImageView.setLayoutParams(layoutParams);

        friendsNumTextView = rootView.findViewById(R.id.friendsNum);
        groupNumTextView = rootView.findViewById(R.id.groupNum);
        rewardsNumTextView = rootView.findViewById(R.id.rewardsNum);

        circularSeekBar1 = rootView.findViewById(R.id.circularSeekBar1);
        circularSeekBar2 = rootView.findViewById(R.id.circularSeekBar2);
        circularSeekBar3 = rootView.findViewById(R.id.circularSeekBar3);
        circularSeekBar4 = rootView.findViewById(R.id.circularSeekBar4);

        circularSeekBar1.setCircleProgressColor(Color.parseColor("#2C9F47"));
        circularSeekBar2.setCircleProgressColor(Color.parseColor("#FFB400"));
        circularSeekBar3.setCircleProgressColor(Color.parseColor("#B61B1B"));

        setupTasksListView(rootView);

        Bundle bundle = this.getArguments();
        User user = Parcels.unwrap(bundle.getParcelable(Constant.ACTIVITY_EXTRA_PROFILE));

        final TextView name = rootView.findViewById(R.id.profile_name);
        name.setText(user.getFullName());

        updateCompletedBadgesCount(user.completedBadgesCount);
        updateCompletedTasksCount(user.completedTasksCount);
        updateExperience(user.level);

        // TODO: getUserDP; photoImageView for avatar, background on backgroundLinearLayout

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

        isegoria.getAPI().getTasks().enqueue(new SimpleCallback<List<Task>>() {
            @Override
            protected void handleResponse(Response<List<Task>> response) {
                List<Task> tasks = response.body();
                if (tasks != null) {
                    setTasks(tasks);
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

    private void setupTasksListView(View rootView) {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        RecyclerView tasksListView = rootView.findViewById(R.id.profile_tasks_list_view);
        tasksListView.setLayoutManager(layoutManager);
        tasksListView.setHasFixedSize(true);
        tasksListView.setAdapter(taskAdapter);
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

    private void setTasks(@NonNull List<Task> tasks) {
        if (getActivity() != null) {
            taskAdapter.replaceItems(tasks);
            taskAdapter.notifyDataSetChanged();
        }
    }
}
