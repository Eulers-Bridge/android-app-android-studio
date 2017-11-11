package com.eulersbridge.isegoria.profile;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.MemoryCategory;
import com.eulersbridge.isegoria.Isegoria;
import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.models.Badge;
import com.eulersbridge.isegoria.network.SimpleCallback;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Response;

public class ProfileBadgesFragment extends Fragment {

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

    /*private void addTableRow(final String name, final String description, final long badgeId,
                             final int maxLevel, final boolean remaining) {
        try {
            int paddingMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                    (float)  6.666666667, getResources().getDisplayMetrics());
            int textSize1 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                    (float)  8.0, getResources().getDisplayMetrics());
            int textSize2 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                    (float)  6.0, getResources().getDisplayMetrics());
            int imageSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                    (float)  70.0, getResources().getDisplayMetrics());
            int boxHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                    (float)  15.0, getResources().getDisplayMetrics());

            photosPerRow = photosPerRow + 1;
            if (photosPerRow == fitPerRow) {
                photosPerRow = 0;
                tr = new TableRow(getActivity());

                if (!insertedFirstRow) {
                    insertedFirstRow = true;
                    tr.setPadding(dividerPadding, dividerPadding, dividerPadding, dividerPadding);
                } else {
                    tr.setPadding(dividerPadding, 0, dividerPadding, dividerPadding);
                }
                badgesTableLayout.addView(tr);
            }

            LinearLayout viewLinearLayout = new LinearLayout(getActivity());
            LinearLayout.LayoutParams layoutParams =
                    new LinearLayout.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                            TableRow.LayoutParams.WRAP_CONTENT, 1.0f);
            layoutParams.gravity = Gravity.CENTER;
            viewLinearLayout.setLayoutParams(layoutParams);
            viewLinearLayout.setGravity(Gravity.CENTER);
            //viewLinearLayout.setBackgroundColor(Color.GRAY);

            ImageView view = new ImageView(getActivity());
            if(remaining) {
                view.setColorFilter(Color.argb(125, 35, 35, 35));
            }
            layoutParams = new LinearLayout.LayoutParams(imageSize, imageSize, 1.0f);
            layoutParams.gravity = Gravity.CENTER;
            view.setLayoutParams(layoutParams);
            view.setScaleType(ScaleType.FIT_XY);
            //view.setBackgroundColor(Color.GRAY);

            isegoria.getAPI().getPhotos(badgeId).enqueue(new SimpleCallback<PhotosResponse>() {
                @Override
                protected void handleResponse(Response<PhotosResponse> response) {
                    PhotosResponse body = response.body();
                    if (body != null && body.totalPhotos > 0) {
                        GlideApp.with(ProfileBadgesFragment.this)
                                .load(body.photos.get(0).thumbnailUrl)
                                .into(view);
                    }
                }
            });

            viewLinearLayout.setPadding(0, 0, 0, paddingMargin);
            viewLinearLayout.addView(view);

            LinearLayout linearLayout = new LinearLayout(getActivity());
            //linearLayout.setBackgroundColor(Color.GRAY);
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            linearLayout.setGravity(Gravity.CENTER_VERTICAL);
            layoutParams = new TableRow.LayoutParams(squareSize, TableRow.LayoutParams.WRAP_CONTENT);
            //layoutParams.gravity = Gravity.CENTER;
            linearLayout.setLayoutParams(layoutParams);
            linearLayout.setPadding(paddingMargin, 0, 0, 0);
            linearLayout.addView(viewLinearLayout);

            TextView nameTextView = new TextView(getActivity());
            nameTextView.setText(name);
            nameTextView.setGravity(Gravity.CENTER);
            nameTextView.setTypeface(Typeface.DEFAULT_BOLD);
            nameTextView.setTextSize(textSize1);

            TextView descTextView = new TextView(getActivity());
            descTextView.setText(description);
            descTextView.setGravity(Gravity.CENTER);
            descTextView.setTextSize(textSize2);

            LinearLayout linearLayout2 = new LinearLayout(getActivity());
            linearLayout2.setOrientation(LinearLayout.VERTICAL);
            linearLayout2.setGravity(Gravity.CENTER_VERTICAL);
            layoutParams = new TableRow.LayoutParams(boxHeight, boxHeight);
            //layoutParams.gravity = Gravity.CENTER;
            linearLayout2.setLayoutParams(layoutParams);
            linearLayout2.setPadding(paddingMargin, 0, 0, 0);

            linearLayout.addView(nameTextView);
            linearLayout.addView(descTextView);
            linearLayout.addView(linearLayout2);

            if (targetLevel < maxLevel) {
                view.setOnClickListener(view1 -> {

                    ProfileBadgesFragment badgesFragment = new ProfileBadgesFragment();
                    Bundle args = new Bundle();
                    args.putString("name", name);
                    args.putInt("level", targetLevel + 1);
                    badgesFragment.setArguments(args);

                    getActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .addToBackStack(null)
                            .add(R.id.profileFrameLayout, badgesFragment)
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                            .commit();
                });
            }

            tr.addView(linearLayout);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }*/
}
