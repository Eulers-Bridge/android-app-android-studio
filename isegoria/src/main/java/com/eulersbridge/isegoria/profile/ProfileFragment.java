package com.eulersbridge.isegoria.profile;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
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

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.eulersbridge.isegoria.common.Constant;
import com.eulersbridge.isegoria.GlideApp;
import com.eulersbridge.isegoria.Isegoria;
import com.eulersbridge.isegoria.MainActivity;
import com.eulersbridge.isegoria.auth.PersonalityQuestionsActivity;
import com.eulersbridge.isegoria.models.Badge;
import com.eulersbridge.isegoria.models.Contact;
import com.eulersbridge.isegoria.models.Institution;
import com.eulersbridge.isegoria.models.Photo;
import com.eulersbridge.isegoria.models.User;
import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.models.Task;
import com.eulersbridge.isegoria.network.PhotosResponse;
import com.eulersbridge.isegoria.network.SimpleCallback;
import com.eulersbridge.isegoria.common.TitledFragment;
import com.eulersbridge.isegoria.views.CircularSeekBar;

import org.parceler.Parcels;

import java.lang.reflect.Field;
import java.util.List;

import retrofit2.Response;

public class ProfileFragment extends Fragment implements TitledFragment {

    private View rootView;

    private TextView friendsNumTextView;
    private TextView groupNumTextView;
    private TextView rewardsNumTextView;

    private CircularSeekBar circularSeekBar1;
    private CircularSeekBar circularSeekBar2;
    private CircularSeekBar circularSeekBar3;
    private CircularSeekBar circularSeekBar4;

    private @Nullable ViewPager parentViewPager;

    private final TaskAdapter taskAdapter = new TaskAdapter(this);
    private RecyclerView tasksListView;

    private Isegoria isegoria;

    private User user = null;
    private boolean isLoggedInUser = false;

    @Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.profile_fragment, container, false);

        isegoria = (Isegoria) getActivity().getApplication();

        View.OnClickListener friendsClickListener = view -> {
            if (getActivity() != null) ((MainActivity)getActivity()).showFriends();
        };

        friendsNumTextView = rootView.findViewById(R.id.friendsNum);
        friendsNumTextView.setOnClickListener(friendsClickListener);

        rootView.findViewById(R.id.friendsTitle).setOnClickListener(friendsClickListener);

        groupNumTextView = rootView.findViewById(R.id.groupNum);
        rewardsNumTextView = rootView.findViewById(R.id.rewardsNum);

        TextView showProgressButton = rootView.findViewById(R.id.profile_show_progress);
        if (parentViewPager == null) {
            showProgressButton.setVisibility(View.GONE);
        } else {
            showProgressButton.setOnClickListener(view -> parentViewPager.setCurrentItem(1, true));
        }

        circularSeekBar1 = rootView.findViewById(R.id.circularSeekBar1);
        circularSeekBar2 = rootView.findViewById(R.id.circularSeekBar2);
        circularSeekBar3 = rootView.findViewById(R.id.circularSeekBar3);
        circularSeekBar4 = rootView.findViewById(R.id.circularSeekBar4);

        circularSeekBar1.setCircleProgressColor(Color.parseColor("#2C9F47"));
        circularSeekBar2.setCircleProgressColor(Color.parseColor("#FFB400"));
        circularSeekBar3.setCircleProgressColor(Color.parseColor("#B61B1B"));

        setupTasksListView(rootView);

        if (getArguments() != null) {
            @Nullable Parcelable userParcelable = getArguments().getParcelable(Constant.FRAGMENT_EXTRA_USER);
            if (userParcelable != null) {
                user = Parcels.unwrap(userParcelable);

            } else {
                long userId = getArguments().getLong(Constant.FRAGMENT_EXTRA_PROFILE_ID);
                fetchUser(userId);
            }
        }

        if (user == null) {
            user = isegoria.getLoggedInUser();
            isLoggedInUser = true;
        }

        if (user != null) setupUI();

		return rootView;
	}

	private void fetchUser(long id) {
        isegoria.getAPI().getContact(user.email).enqueue(new SimpleCallback<Contact>() {
            @Override
            protected void handleResponse(Response<Contact> response) {
                Contact contact = response.body();
                if (contact != null) {

                    if (user == null) {
                        user = new User(contact);

                        setupUI();
                    }

                    updateStats(contact.contactsCount, contact.totalTasksCount);
                }
            }
        });
    }

    private void setupUI() {
        TextView name = rootView.findViewById(R.id.profile_name);
        name.setText(user.getFullName());

        TextView personalityTestButton = rootView.findViewById(R.id.profile_personality_test_button);

        if (isLoggedInUser || user.hasPersonality) {
            // Don't show the personality test button if visiting someone else's profile,
            // or if showing the logged in user's profile and they've completed personality Qs already
            personalityTestButton.setVisibility(View.GONE);

        } else {
            personalityTestButton.setOnClickListener(view -> startActivity(new Intent(getActivity(), PersonalityQuestionsActivity.class)));
        }

        updateCompletedBadgesCount(user.completedBadgesCount);
        updateCompletedTasksCount(user.completedTasksCount);
        updateExperience(user.level, user.experience);

        long userId = user.getId();

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
                    institutionTextView.setText(institution.getName());
                }
            }
        });

        int imageHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                (float) 100.00, getResources().getDisplayMetrics());

        ImageView photoImageView = rootView.findViewById(R.id.profile_image_small);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(imageHeight, imageHeight);
        photoImageView.setLayoutParams(layoutParams);

        GlideApp.with(this)
                .load(user.profilePhotoURL)
                .priority(Priority.HIGH)
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
                            .priority(Priority.HIGH)
                            .placeholder(R.color.profileImageBackground)
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
    private void updateExperience(long level, long experience) {
        circularSeekBar1.setTopLine(String.valueOf(level));

        long max = 1000;
        long progress = experience % 1000;

        circularSeekBar1.setBottomLine("NEED " + progress);

        circularSeekBar1.setMax((int)max);

        circularSeekBar1.setProgress((int)progress);
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

    private void setTasks(@NonNull List<Task> tasks) {
        if (getActivity() != null) {
            taskAdapter.replaceItems(tasks);
            taskAdapter.notifyDataSetChanged();

            // Calculate rough new list view size to 'autosize' it
            getActivity().runOnUiThread(() -> {
                int heightDp = 44 * tasks.size();

                int heightPx = Math.round(TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, heightDp, getResources().getDisplayMetrics()));

                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, heightPx);

                tasksListView.setLayoutParams(layoutParams);
            });
        }
    }

    @Override
    public String getTitle(Context context) {
        return context.getString(R.string.profile_overview_section_title);
    }

    void setViewPager(ViewPager viewPager) {
        parentViewPager = viewPager;
    }
}