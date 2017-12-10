package com.eulersbridge.isegoria.profile;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.MemoryCategory;
import com.eulersbridge.isegoria.Isegoria;
import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.models.Badge;
import com.eulersbridge.isegoria.network.SimpleCallback;
import com.eulersbridge.isegoria.utilities.TitledFragment;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Response;

public class ProfileBadgesFragment extends Fragment implements TitledFragment {

    private int targetLevel = 0;

    private final BadgeAdapter badgeAdapter = new BadgeAdapter(this);

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.profile_badges_fragment, container, false);

        String targetName = null;

        Bundle bundle = getArguments();
        if (bundle != null) {
            targetName = bundle.getString("name");
            targetLevel = bundle.getInt("level");
        }

        Glide.get(getContext()).setMemoryCategory(MemoryCategory.HIGH);

        RecyclerView badgesGridView = rootView.findViewById(R.id.profile_badges_grid_view);
        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 3, LinearLayoutManager.VERTICAL, false);
        badgesGridView.setLayoutManager(layoutManager);
        badgesGridView.setAdapter(badgeAdapter);
        badgesGridView.setItemViewCacheSize(21);
        badgesGridView.setDrawingCacheEnabled(true);
        badgesGridView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_AUTO);

        Isegoria isegoria = (Isegoria) getActivity().getApplication();

        long userId = isegoria.getLoggedInUser().getId();

        isegoria.getAPI().getCompletedBadges(userId).enqueue(new SimpleCallback<List<Badge>>() {
            @Override
            protected void handleResponse(Response<List<Badge>> response) {
                List<Badge> completedBadges = response.body();
                if (completedBadges != null) {
                    addCompletedBadges(completedBadges);
                }
            }
        });

        isegoria.getAPI().getRemainingBadges(userId).enqueue(new SimpleCallback<List<Badge>>() {
            @Override
            protected void handleResponse(Response<List<Badge>> response) {
                List<Badge> remainingBadges = response.body();
                if (remainingBadges != null) {
                    addRemainingBadges(remainingBadges);
                }
            }
        });

        return rootView;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        Glide.get(getContext()).setMemoryCategory(MemoryCategory.NORMAL);
    }

    private void addRemainingBadges(List<Badge> badges) {
        if (getActivity() != null) {

            List<Badge> remainingBadges = new ArrayList<>();

            for (Badge badge : badges) {
                if (badge.level == targetLevel) {
                    remainingBadges.add(badge);
                }
            }

            badgeAdapter.replaceRemainingItems(remainingBadges);
            badgeAdapter.notifyDataSetChanged();
        }
    }

    private void addCompletedBadges(List<Badge> badges) {
        if (getActivity() != null) {
            List<Badge> completedBadges = new ArrayList<>();

            for (Badge badge : badges) {
                if (badge.level == targetLevel) {
                    completedBadges.add(badge);
                }
            }

            badgeAdapter.replaceCompletedItems(completedBadges);
            badgeAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public String getTitle() {
        return getString(R.string.profile_badges_section_title);
    }
}
