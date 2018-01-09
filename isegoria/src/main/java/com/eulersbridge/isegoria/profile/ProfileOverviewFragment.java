package com.eulersbridge.isegoria.profile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.eulersbridge.isegoria.GlideApp;
import com.eulersbridge.isegoria.Isegoria;
import com.eulersbridge.isegoria.MainActivity;
import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.auth.PersonalityQuestionsActivity;
import com.eulersbridge.isegoria.common.BlurTransformation;
import com.eulersbridge.isegoria.common.Constant;
import com.eulersbridge.isegoria.common.TintTransformation;
import com.eulersbridge.isegoria.common.TitledFragment;
import com.eulersbridge.isegoria.models.Badge;
import com.eulersbridge.isegoria.models.Contact;
import com.eulersbridge.isegoria.models.GenericUser;
import com.eulersbridge.isegoria.models.Institution;
import com.eulersbridge.isegoria.models.Photo;
import com.eulersbridge.isegoria.models.Task;
import com.eulersbridge.isegoria.models.User;
import com.eulersbridge.isegoria.network.PhotosResponse;
import com.eulersbridge.isegoria.network.SimpleCallback;
import com.eulersbridge.isegoria.views.CircleProgressBar;

import org.parceler.Parcels;

import java.lang.reflect.Field;
import java.util.List;

import retrofit2.Response;

public class ProfileOverviewFragment extends Fragment implements TitledFragment {

    private View rootView;

    private TextView friendsNumTextView;
    private TextView groupNumTextView;
    private TextView rewardsNumTextView;

    private CircleProgressBar experienceProgressCircle;
    private CircleProgressBar badgesProgressCircle;
    private CircleProgressBar tasksProgressCircle;
    private CircleProgressBar circleProgressBar4;

    private @Nullable ViewPager parentViewPager;

    private final TaskAdapter taskAdapter = new TaskAdapter(this);

    private Isegoria isegoria;

    private GenericUser user = null;
    private boolean isLoggedInUser = false;

    @Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.profile_overview_fragment, container, false);

        if (getActivity() != null)
            isegoria = (Isegoria) getActivity().getApplication();

        View.OnClickListener friendsClickListener = view -> {
            if (getActivity() != null && isLoggedInUser)
                ((MainActivity)getActivity()).showFriends();
        };

        friendsNumTextView = rootView.findViewById(R.id.profile_friends_num);
        friendsNumTextView.setOnClickListener(friendsClickListener);

        rootView.findViewById(R.id.profile_friends_label).setOnClickListener(friendsClickListener);

        groupNumTextView = rootView.findViewById(R.id.profile_group_num);
        rewardsNumTextView = rootView.findViewById(R.id.profile_rewards_num);

        TextView showProgressButton = rootView.findViewById(R.id.profile_show_progress);
        if (parentViewPager == null) {
            showProgressButton.setVisibility(View.GONE);
        } else {
            showProgressButton.setOnClickListener(view -> parentViewPager.setCurrentItem(1, true));
        }

        experienceProgressCircle = rootView.findViewById(R.id.profile_experience_progress_circle);
        badgesProgressCircle = rootView.findViewById(R.id.profile_badges_progress_circle);
        tasksProgressCircle = rootView.findViewById(R.id.profile_tasks_progress_circle);
        circleProgressBar4 = rootView.findViewById(R.id.circularSeekBar4);

        setupTasksListView(rootView);

        if (getArguments() != null) {
            @Nullable Parcelable userParcelable = getArguments().getParcelable(Constant.FRAGMENT_EXTRA_CONTACT);
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

        if (!(user instanceof User) || (isLoggedInUser  && user instanceof User && ((User)user).hasPersonality)) {
            // Don't show the personality test button if visiting someone else's profile,
            // or if showing the logged in user's profile and they've completed personality Qs already
            personalityTestButton.setVisibility(View.GONE);

        } else {
            personalityTestButton.setOnClickListener(view -> startActivity(new Intent(getActivity(), PersonalityQuestionsActivity.class)));
        }

        updateCompletedTasksCount(user.completedTasksCount);
        updateExperience(user.level, user.experience);

        Long userId = user instanceof User? ((User)user).getId() : null;

        if (userId != null) {
            isegoria.getAPI().getRemainingBadges(userId).enqueue(new SimpleCallback<List<Badge>>() {
                @Override
                protected void handleResponse(Response<List<Badge>> response) {
                    List<Badge> remainingBadges = response.body();
                    if (remainingBadges != null) {
                        updateBadgesCount(user.completedBadgesCount, remainingBadges.size());
                    }
                }
            });
        }

        TextView institutionTextView = rootView.findViewById(R.id.profile_institution);

        if (user.institutionId != null) {
            isegoria.getAPI().getInstitution(user.institutionId).enqueue(new SimpleCallback<Institution>() {
                @Override
                protected void handleResponse(Response<Institution> response) {
                    Institution institution = response.body();
                    if (institution != null){
                        institutionTextView.setText(institution.getName());
                    }
                }
            });
        }

        ImageView photoImageView = rootView.findViewById(R.id.profile_image_small);

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

                    GlideApp.with(ProfileOverviewFragment.this)
                            .load(photo.thumbnailUrl)
                            .transforms(new BlurTransformation(getContext()), new TintTransformation(0.1))
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
        RecyclerView tasksListView = rootView.findViewById(R.id.profile_tasks_list_view);
        tasksListView.setAdapter(taskAdapter);
        tasksListView.setNestedScrollingEnabled(false);

        tasksListView.setDrawingCacheEnabled(true);
        tasksListView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_LOW);
    }

    @UiThread
    private void updateBadgesCount(long completedCount, long remainingCount) {
        badgesProgressCircle.setTopLine(String.valueOf(completedCount));
        badgesProgressCircle.setBottomLine("/" + String.valueOf(remainingCount));
        badgesProgressCircle.setMaximumValue((int)remainingCount);
        badgesProgressCircle.setValue((int)completedCount, true);
    }

    @UiThread
    private void updateCompletedTasksCount(long count) {
        tasksProgressCircle.setTopLine(String.valueOf(count));
        tasksProgressCircle.setValue((int)count, true);
    }

    @UiThread
    private void updateExperience(long level, long experience) {
        experienceProgressCircle.setTopLine(String.valueOf(level));

        long max = 1000;
        long progress = experience % 1000;

        experienceProgressCircle.setBottomLine("NEED " + progress);
        experienceProgressCircle.setMaximumValue((int)max);
        experienceProgressCircle.setValue((int)progress, true);
    }

    @UiThread
    private void updateStats(long numOfContacts, long totalTasks) {
        friendsNumTextView.setText(String.valueOf(numOfContacts));
        groupNumTextView.setText(String.valueOf("0"));
        rewardsNumTextView.setText(String.valueOf("0"));

        tasksProgressCircle.setBottomLine("PER DAY");
        circleProgressBar4.setTopLine(String.valueOf("0"));
        circleProgressBar4.setBottomLine("ATTENDED");

        tasksProgressCircle.setMaximumValue((int)totalTasks);

        circleProgressBar4.setValue(30, true);
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
        taskAdapter.replaceItems(tasks);
    }

    @Override
    public String getTitle(Context context) {
        return context.getString(R.string.profile_overview_section_title);
    }

    void setViewPager(ViewPager viewPager) {
        parentViewPager = viewPager;
    }
}