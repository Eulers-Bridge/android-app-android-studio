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
import com.eulersbridge.isegoria.profile.ProfileViewModel;
import com.eulersbridge.isegoria.util.ui.TitledFragment;

public class ProfileBadgesFragment extends Fragment implements TitledFragment {

    private ProfileViewModel viewModel;

    private final BadgeAdapter badgeAdapter = new BadgeAdapter(this);

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.profile_badges_fragment, container, false);

        //noinspection ConstantConditions
        viewModel = ViewModelProviders.of(getParentFragment()).get(ProfileViewModel.class);

        Bundle bundle = getArguments();
        if (bundle != null)
            viewModel.setTargetBadgeLevel(bundle.getInt("level"));

        //noinspection ConstantConditions
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
        viewModel.getRemainingBadges(true).observe(this, remainingBadges -> {
            if (remainingBadges != null)
                badgeAdapter.replaceRemainingItems(remainingBadges);
        });

        viewModel.getCompletedBadges().observe(this, completedBadges -> {
            if (completedBadges != null)
                badgeAdapter.replaceCompletedItems(completedBadges);
        });
    }

    @Override
    public void onDetach() {
        super.onDetach();

        if (getContext() != null)
            Glide.get(getContext()).setMemoryCategory(MemoryCategory.NORMAL);
    }

    @Override
    public String getTitle(Context context) {
        return context.getString(R.string.profile_badges_section_title);
    }
}
