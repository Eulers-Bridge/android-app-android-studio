package com.eulersbridge.isegoria.profile;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.eulersbridge.isegoria.GlideApp;
import com.eulersbridge.isegoria.IsegoriaApp;
import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.network.api.API;
import com.eulersbridge.isegoria.network.api.models.Contact;
import com.eulersbridge.isegoria.network.api.models.GenericUser;
import com.eulersbridge.isegoria.network.api.models.User;
import com.eulersbridge.isegoria.personality.PersonalityQuestionsActivity;
import com.eulersbridge.isegoria.util.Constants;
import com.eulersbridge.isegoria.util.transformation.BlurTransformation;
import com.eulersbridge.isegoria.util.transformation.TintTransformation;
import com.eulersbridge.isegoria.util.ui.TitledFragment;

import org.parceler.Parcels;

import java.lang.reflect.Field;

public class ProfileOverviewFragment extends Fragment implements TitledFragment {

    private ImageView photoImageView;
    private ImageView backgroundImageView;

    private TextView nameTextView;
    private TextView institutionTextView;
    private TextView personalityTestButton;

    private TextView friendsNumTextView;

    private CircleProgressBar experienceProgressCircle;
    private CircleProgressBar badgesProgressCircle;
    private CircleProgressBar tasksProgressCircle;

    private RecyclerView tasksListView;
    private TaskAdapter taskAdapter;

    private ProfileViewModel viewModel;

    @Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.profile_overview_fragment, container, false);

        photoImageView = rootView.findViewById(R.id.profile_image_small);
        backgroundImageView = rootView.findViewById(R.id.profile_image_background);

        nameTextView = rootView.findViewById(R.id.profile_name);
        institutionTextView = rootView.findViewById(R.id.profile_institution);
        personalityTestButton = rootView.findViewById(R.id.profile_personality_test_button);

        View.OnClickListener friendsClickListener = view -> viewModel.showFriends();

        friendsNumTextView = rootView.findViewById(R.id.profile_friends_num);
        friendsNumTextView.setOnClickListener(friendsClickListener);

        rootView.findViewById(R.id.profile_friends_label).setOnClickListener(friendsClickListener);

        TextView showProgressButton = rootView.findViewById(R.id.profile_show_progress);
        showProgressButton.setOnClickListener(view -> viewModel.showTasksProgress());

        experienceProgressCircle = rootView.findViewById(R.id.profile_experience_progress_circle);
        badgesProgressCircle = rootView.findViewById(R.id.profile_badges_progress_circle);
        tasksProgressCircle = rootView.findViewById(R.id.profile_tasks_progress_circle);

        tasksListView = rootView.findViewById(R.id.profile_tasks_list_view);

		return rootView;
	}

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //noinspection ConstantConditions: getActivity() cannot be null in onActivityCreated()
        IsegoriaApp app = (IsegoriaApp) getActivity().getApplication();

        API api = app.getAPI();

        taskAdapter = new TaskAdapter(Glide.with(this), api);
        tasksListView.setAdapter(taskAdapter);
        tasksListView.setNestedScrollingEnabled(false);
        tasksListView.setDrawingCacheEnabled(true);
        tasksListView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_LOW);

        //noinspection ConstantConditions
        Fragment lifecycleOwner = getParentFragment();
        if (lifecycleOwner == null)
            lifecycleOwner = this;

        viewModel = ViewModelProviders.of(lifecycleOwner).get(ProfileViewModel.class);

        app.loggedInUser.observe(this, user -> {
            if (user != null && viewModel.user.getValue() == null)
                viewModel.setUser(user);
        });

        setupViewModelObservers();

        GenericUser user = null;

        if (getArguments() != null) {
            @Nullable Parcelable userParcelable = getArguments().getParcelable(Constants.FRAGMENT_EXTRA_CONTACT);

            if (userParcelable != null) {
                user = Parcels.unwrap(userParcelable);

            } else {
                long userId = getArguments().getLong(Constants.FRAGMENT_EXTRA_PROFILE_ID);
            }
        }

        if (user != null)
            viewModel.setUser(user);
    }

    @Override
    public void onDestroyView() {
        GlideApp.with(this).onDestroy();

        super.onDestroyView();
    }

    private void setupViewModelObservers() {
        viewModel.user.observe(this, user -> {
            if (user == null) {
                personalityTestButton.setVisibility(View.GONE);
                return;
            }

            viewModel.getUserPhoto().observe(this, photo -> {
                if (photo != null)
                    GlideApp.with(ProfileOverviewFragment.this)
                            .load(photo.thumbnailUrl)
                            .transforms(new BlurTransformation(getContext()), new TintTransformation(0.1))
                            .priority(Priority.HIGH)
                            .placeholder(R.color.profileImageBackground)
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .into(backgroundImageView);
            });

            viewModel.getInstitutionName().observe(this, institutionName -> {
                if (institutionName != null)
                    institutionTextView.setText(institutionName);
            });

            viewModel.getRemainingBadges().observe(this, remainingBadges -> {
                if (remainingBadges != null)
                    updateBadgesCount(user.completedBadgesCount, remainingBadges.size());
            });

            viewModel.getTasks().observe(this, tasks -> {
                if (tasks != null)
                    taskAdapter.setItems(tasks);
            });

            viewModel.fetchUserStats();

            GlideApp.with(this)
                    .load(user.profilePhotoURL)
                    .priority(Priority.HIGH)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(photoImageView);

            nameTextView.setText(user.getFullName());

            boolean loggedInUserWithPersonality = user instanceof User && ((User)user).hasPersonality;
            boolean externalUser = user instanceof Contact;
            if (externalUser || loggedInUserWithPersonality) {
                personalityTestButton.setVisibility(View.GONE);

            } else {
                personalityTestButton.setOnClickListener(view ->
                        startActivity(new Intent(getActivity(), PersonalityQuestionsActivity.class)));
            }

            updateCompletedTasksCount(user.completedTasksCount);
            updateExperience(user.level, user.experience);
        });

        viewModel.contactsCount.observe(this, contactsCount -> {
            if (contactsCount != null)
                updateContactsCount(contactsCount);
        });

        viewModel.totalTasksCount.observe(this, totalTasksCount -> {
            if (totalTasksCount != null)
                updateTotalTasksCount(totalTasksCount);
        });
    }

    private void updateBadgesCount(long completedCount, long remainingCount) {
        badgesProgressCircle.post(() -> {
            badgesProgressCircle.setTopLine(String.valueOf(completedCount));
            badgesProgressCircle.setBottomLine("/" + String.valueOf(remainingCount));
            badgesProgressCircle.setMaximumValue((int)remainingCount);
            badgesProgressCircle.setValue((int)completedCount, true);
        });
    }

    private void updateCompletedTasksCount(long count) {
        tasksProgressCircle.post(() -> {
            tasksProgressCircle.setTopLine(String.valueOf(count));
            tasksProgressCircle.setValue((int)count, true);
        });
    }

    private void updateExperience(long level, long experience) {
        experienceProgressCircle.post(() -> {
            experienceProgressCircle.setTopLine(String.valueOf(level));

            long progress = experience % 1000;
            long max = 1000;

            experienceProgressCircle.setBottomLine("NEED " + progress);
            experienceProgressCircle.setMaximumValue((int)max);
            experienceProgressCircle.setValue((int)progress, true);
        });
    }

    private void updateContactsCount(long contactsCount) {
        friendsNumTextView.setText(String.valueOf(contactsCount));
    }

    private void updateTotalTasksCount(long totalTasksCount) {
        tasksProgressCircle.post(() -> tasksProgressCircle.setMaximumValue((int)totalTasksCount));
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

    @Override
    public String getTitle(Context context) {
        return context.getString(R.string.profile_overview_section_title);
    }
}