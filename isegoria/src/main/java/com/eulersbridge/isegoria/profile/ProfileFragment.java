package com.eulersbridge.isegoria.profile;

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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.eulersbridge.isegoria.GlideApp;
import com.eulersbridge.isegoria.Isegoria;
import com.eulersbridge.isegoria.MainActivity;
import com.eulersbridge.isegoria.models.Badge;
import com.eulersbridge.isegoria.models.Contact;
import com.eulersbridge.isegoria.models.Institution;
import com.eulersbridge.isegoria.models.Photo;
import com.eulersbridge.isegoria.models.User;
import com.eulersbridge.isegoria.login.PersonalityQuestionsFragment;
import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.models.Task;
import com.eulersbridge.isegoria.network.PhotosResponse;
import com.eulersbridge.isegoria.network.SimpleCallback;
import com.eulersbridge.isegoria.views.CircularSeekBar;

import java.lang.reflect.Field;
import java.util.List;

import retrofit2.Response;

public class ProfileFragment extends Fragment {

    private TextView friendsNumTextView;
    private TextView groupNumTextView;
    private TextView rewardsNumTextView;

    private CircularSeekBar circularSeekBar1;
    private CircularSeekBar circularSeekBar2;
    private CircularSeekBar circularSeekBar3;
    private CircularSeekBar circularSeekBar4;

    private final TaskAdapter taskAdapter = new TaskAdapter(this);

    private RecyclerView tasksListView;

    @Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.profile_fragment, container, false);

        Isegoria isegoria = (Isegoria) getActivity().getApplication();

        MainActivity mainActivity = (MainActivity) getActivity();

        int imageHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                (float) 100.00, getResources().getDisplayMetrics());

        ImageView photoImageView = rootView.findViewById(R.id.profile_image_small);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(imageHeight, imageHeight);
        photoImageView.setLayoutParams(layoutParams);

        friendsNumTextView = rootView.findViewById(R.id.friendsNum);

        View.OnClickListener friendsClickListener = view -> mainActivity.showFriends();

        friendsNumTextView.setOnClickListener(friendsClickListener);
        rootView.findViewById(R.id.friendsTitle).setOnClickListener(friendsClickListener);

        groupNumTextView = rootView.findViewById(R.id.groupNum);
        rewardsNumTextView = rootView.findViewById(R.id.rewardsNum);

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

        setupTasksListView(rootView);

        User user = isegoria.getLoggedInUser();

        final TextView name = rootView.findViewById(R.id.profile_name);
        name.setText(user.getFullName());

        final TextView personalityTestButton = rootView.findViewById(R.id.profile_personality_test_button);

        if (user.hasPersonality) {
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

        updateCompletedBadgesCount(user.completedBadgesCount);
        updateCompletedTasksCount(user.completedTasksCount);
        updateExperience(user.level);

        Long userId = isegoria.getLoggedInUser().getId();

        isegoria.getAPI().getRemainingBadges(userId).enqueue(new SimpleCallback<List<Badge>>() {
            @Override
            protected void handleResponse(Response<List<Badge>> response) {
                List<Badge> remainingBadges = response.body();
                if (remainingBadges != null) {
                    updateRemainingBadgesCount(remainingBadges.size());
                }
            }
        });

        TextView institutionTextView = rootView.findViewById(R.id.profile_institution);

        isegoria.getAPI().getInstitution(user.institutionId).enqueue(new SimpleCallback<Institution>() {
            @Override
            protected void handleResponse(Response<Institution> response) {
                Institution institution = response.body();
                if (institution != null){
                    institutionTextView.setText(institution.name);
                }
            }
        });

        GlideApp.with(this)
                .load(user.profilePhotoURL)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(photoImageView);

        ImageView backgroundImageView = rootView.findViewById(R.id.profile_image_background);

        isegoria.getAPI().getPhotos(user.email).enqueue(new SimpleCallback<PhotosResponse>() {
            @Override
            protected void handleResponse(Response<PhotosResponse> response) {
                PhotosResponse body = response.body();
                if (body != null && body.totalPhotos > 0) {
                    Photo photo = body.photos.get(0);

                    GlideApp.with(ProfileFragment.this)
                            .load(photo.thumbnailUrl)
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .into(backgroundImageView);
                }
            }
        });

        isegoria.getAPI().getTasks().enqueue(new SimpleCallback<List<Task>>() {
            @Override
            protected void handleResponse(Response<List<Task>> response) {
                List<Task> tasks = response.body();
                if (tasks != null) setTasks(tasks);
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
        tasksListView = rootView.findViewById(R.id.profile_tasks_list_view);
        tasksListView.setLayoutManager(layoutManager);
        tasksListView.setAdapter(taskAdapter);
        tasksListView.setNestedScrollingEnabled(false);

        tasksListView.setDrawingCacheEnabled(true);
        tasksListView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_LOW);
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

	        getActivity().runOnUiThread(() -> {
	            int heightDp = 40 * tasks.size();

                int heightPx = Math.round(TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, heightDp, getResources().getDisplayMetrics()));

                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, heightPx);

                tasksListView.setLayoutParams(layoutParams);
            });
        }
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
}