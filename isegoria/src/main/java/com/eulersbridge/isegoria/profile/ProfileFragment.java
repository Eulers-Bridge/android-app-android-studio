package com.eulersbridge.isegoria.profile;

import android.app.ActionBar;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.eulersbridge.isegoria.Isegoria;
import com.eulersbridge.isegoria.MainActivity;
import com.eulersbridge.isegoria.Network;
import com.eulersbridge.isegoria.login.PersonalityQuestionsFragment;
import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.models.Task;
import com.eulersbridge.isegoria.models.User;
import com.eulersbridge.isegoria.views.CircularSeekBar;

import java.util.ArrayList;

public class ProfileFragment extends Fragment {
	private View rootView;

    private TextView friendsNumTextView;
    private TextView groupNumTextView;
    private TextView rewardsNumTextView;

    private CircularSeekBar circularSeekBar1;
    private CircularSeekBar circularSeekBar2;
    private CircularSeekBar circularSeekBar3;
    private CircularSeekBar circularSeekBar4;

    private Network network;

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.profile_fragment, container, false);

		setHasOptionsMenu(true);

        final MainActivity mainActivity = (MainActivity) getActivity();
        network = mainActivity.getIsegoriaApplication().getNetwork();

        int imageHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                (float) 100.00, getResources().getDisplayMetrics());

        ImageView photoImageView = rootView.findViewById(R.id.profilePic);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(imageHeight, imageHeight);
        photoImageView.setLayoutParams(layoutParams);

        friendsNumTextView = rootView.findViewById(R.id.friendsNum);
        groupNumTextView = rootView.findViewById(R.id.groupNum);
        rewardsNumTextView = rootView.findViewById(R.id.rewardsNum);

        network.getRemainingBadgeCount(new Network.RemainingBadgeCountListener() {
            @Override
            public void onFetchSuccess(final long remainingBadgeCount) {
                getActivity().runOnUiThread(() -> updateRemainingBadgesCount(remainingBadgeCount));
            }

            @Override
            public void onFetchFailure(Exception e) { }
        });

        /*final TextView showProgressButton = (TextView) rootView.findViewById(R.id.showProfile);
        showProgressButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPager.setCurrentItem(1);
            }
        });*/

        circularSeekBar1 = rootView.findViewById(R.id.circularSeekBar1);
        circularSeekBar2 = rootView.findViewById(R.id.circularSeekBar2);
        circularSeekBar3 = rootView.findViewById(R.id.circularSeekBar3);
        circularSeekBar4 = rootView.findViewById(R.id.circularSeekBar4);

        circularSeekBar1.setCircleProgressColor(Color.parseColor("#2C9F47"));
        circularSeekBar2.setCircleProgressColor(Color.parseColor("#FFB400"));
        circularSeekBar3.setCircleProgressColor(Color.parseColor("#B61B1B"));

        User user = ((Isegoria)getActivity().getApplication()).getLoggedInUser();

        final TextView name = rootView.findViewById(R.id.profileName);
        name.setText(user.getFullName());

        final TextView personalityTestButton = rootView.findViewById(R.id.personalityTestButton);

        if (user.hasPersonality()) {
            personalityTestButton.setVisibility(View.GONE);

        } else {
            personalityTestButton.setOnClickListener(view -> {

                PersonalityQuestionsFragment personalityQuestionsFragment = new PersonalityQuestionsFragment();
                personalityQuestionsFragment.setTabLayout(mainActivity.getTabLayout());

                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .addToBackStack(null)
                        .add(R.id.container, personalityQuestionsFragment)
                        .commit();
            });
        }

        updateCompletedBadgesCount(user.getCompletedBadgesCount());
        updateCompletedTasksCount(user.getCompletedTasksCount());
        updateExperience(user.getLevel());

        LinearLayout backgroundLinearLayout = rootView.findViewById(R.id.topBackgroundNews);
        network.getUserDP(photoImageView, backgroundLinearLayout);

        network.getTasks(new Network.TasksListener() {
            @Override
            public void onFetchSuccess(ArrayList<Task> tasks) {
                addTasks(tasks);
            }

            @Override
            public void onFetchFailure(Exception e) {}
        });

        network.getProfileStats(user.getEmail(), new Network.ProfileStatsListener() {
            @Override
            public void onFetchSuccess(int contactsCount, int totalTasksCount) {
                updateStats(contactsCount, totalTasksCount);
            }

            @Override
            public void onFetchFailure(Exception e) { }
        });

		return rootView;
	}

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.profile, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
	    if (item.getItemId() == R.id.profileLogout) {

            ((Isegoria)getActivity().getApplication()).logOut();

	        return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
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
    public void updateStats(int numOfContacts, int totalTasks) {
        friendsNumTextView.setText(String.valueOf(numOfContacts));
        groupNumTextView.setText(String.valueOf("0"));
        rewardsNumTextView.setText(String.valueOf("0"));

        circularSeekBar3.setBottomLine("PER DAY");
        circularSeekBar4.setTopLine(String.valueOf("0"));
        circularSeekBar4.setBottomLine("ATTENDED");

        circularSeekBar3.setMax(totalTasks);

        circularSeekBar4.setProgress(30);
        Thread t4 = new Thread(circularSeekBar4);
        t4.start();
    }

    private void addTasks(ArrayList<Task> tasks) {
	    if (getActivity() != null && tasks.size() > 0) {
	        getActivity().runOnUiThread(() -> {
	            for (Task task : tasks) {
	                addTask(task.getId(), task.getAction(), task.getXpValue());
                }
            });
        }
    }

    @UiThread
    public void addTask(long taskId, String action, long xpValue) {
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