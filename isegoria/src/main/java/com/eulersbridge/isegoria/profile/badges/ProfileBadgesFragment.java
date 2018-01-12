package com.eulersbridge.isegoria.profile.badges;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.MemoryCategory;
import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.network.api.models.Badge;
import com.eulersbridge.isegoria.profile.ProfileViewModel;
import com.eulersbridge.isegoria.util.ui.TitledFragment;

import java.util.ArrayList;
import java.util.List;

public class ProfileBadgesFragment extends Fragment implements TitledFragment {

    private ProfileViewModel profileViewModel;

    private int targetBadgeLevel = 0;

    private final BadgeAdapter badgeAdapter = new BadgeAdapter(this);

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.profile_badges_fragment, container, false);

        //noinspection ConstantConditions
        profileViewModel = ViewModelProviders.of(getParentFragment()).get(ProfileViewModel.class);

        Bundle bundle = getArguments();
        if (bundle != null)
            targetBadgeLevel = bundle.getInt("level");

        Glide.get(getContext()).setMemoryCategory(MemoryCategory.HIGH);

        RecyclerView badgesGridView = rootView.findViewById(R.id.profile_badges_grid_view);
        badgesGridView.setAdapter(badgeAdapter);
        badgesGridView.setItemViewCacheSize(21);
        badgesGridView.setDrawingCacheEnabled(true);
        badgesGridView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_AUTO);

        getBadges();

        return rootView;
    }

    private void getBadges() {
        profileViewModel.getRemainingBadges().observe(this, remainingBadges -> {
            if (remainingBadges != null)
                addRemainingBadges(remainingBadges);
        });

        profileViewModel.getCompletedBadges().observe(this, completedBadges -> {
            if (completedBadges != null)
                addCompletedBadges(completedBadges);
        });
    }

    @Override
    public void onDetach() {
        super.onDetach();

        Glide.get(getContext()).setMemoryCategory(MemoryCategory.NORMAL);
    }

    private void addRemainingBadges(@NonNull List<Badge> badges) {
        List<Badge> remainingBadges = new ArrayList<>();

        for (Badge badge : badges) {
            if (badge.level == targetBadgeLevel) {
                remainingBadges.add(badge);
            }
        }

        badgeAdapter.replaceRemainingItems(remainingBadges);
    }

    private void addCompletedBadges(@NonNull List<Badge> badges) {
        List<Badge> completedBadges = new ArrayList<>();

        for (Badge badge : badges) {
            if (badge.level == targetBadgeLevel) {
                completedBadges.add(badge);
            }
        }

        badgeAdapter.replaceCompletedItems(completedBadges);
    }

    @Override
    public String getTitle(Context context) {
        return context.getString(R.string.profile_badges_section_title);
    }
}
