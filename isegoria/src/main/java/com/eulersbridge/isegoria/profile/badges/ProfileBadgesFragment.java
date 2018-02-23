package com.eulersbridge.isegoria.profile.badges;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.eulersbridge.isegoria.GlideApp;
import com.eulersbridge.isegoria.IsegoriaApp;
import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.network.api.API;
import com.eulersbridge.isegoria.profile.ProfileViewModel;
import com.eulersbridge.isegoria.util.ui.TitledFragment;

public class ProfileBadgesFragment extends Fragment implements TitledFragment {

    private ProfileViewModel viewModel;

    private RecyclerView badgesGridView;
    private BadgeAdapter badgeAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.profile_badges_fragment, container, false);

        //noinspection ConstantConditions
        viewModel = ViewModelProviders.of(getParentFragment()).get(ProfileViewModel.class);

        Bundle bundle = getArguments();
        if (bundle != null)
            viewModel.setTargetBadgeLevel(bundle.getInt("level"));

        badgesGridView = rootView.findViewById(R.id.profile_badges_grid_view);

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //noinspection ConstantConditions: getActivity() cannot be null in onActivityCreated()
        API api = ((IsegoriaApp) getActivity().getApplication()).getAPI();

        badgeAdapter = new BadgeAdapter(GlideApp.with(this), api);
        badgesGridView.setAdapter(badgeAdapter);
        badgesGridView.setItemViewCacheSize(21);
        badgesGridView.setDrawingCacheEnabled(true);
        badgesGridView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_AUTO);

        getBadges();
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
    public String getTitle(Context context) {
        return context.getString(R.string.profile_badges_section_title);
    }
}
