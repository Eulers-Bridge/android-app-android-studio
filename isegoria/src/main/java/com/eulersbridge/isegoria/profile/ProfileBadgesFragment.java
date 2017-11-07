package com.eulersbridge.isegoria.profile;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.eulersbridge.isegoria.MainActivity;
import com.eulersbridge.isegoria.Network;
import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.models.Badge;

import java.util.ArrayList;

public class ProfileBadgesFragment extends Fragment {
    private TableLayout badgesTableLayout;
    private TableRow tr;

    private int photosPerRow = -1;
    private int fitPerRow = 0;
    private int squareSize;
    private int dividerPadding = 0;

    private boolean insertedFirstRow = false;

    private Network network;

    private int targetLevel = 0;

    public ProfileBadgesFragment() {
        insertedFirstRow = false;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.profile_badges_layout, container, false);

        String targetName = null;

        Bundle bundle = getArguments();
        if (bundle != null) {
            targetName = bundle.getString("name");
            targetLevel = bundle.getInt("level");
        }

        DisplayMetrics displayMetrics = getActivity().getResources().getDisplayMetrics();
        badgesTableLayout = rootView.findViewById(R.id.profileBadgesTableLayout);

        squareSize = displayMetrics.widthPixels / 3 - (10/3);
        fitPerRow = 3;
        dividerPadding = (10/3);

        tr = new TableRow(getActivity());
        badgesTableLayout.addView(tr);

        MainActivity mainActivity = (MainActivity) getActivity();
        network = mainActivity.getIsegoriaApplication().getNetwork();
        network.getProfileBadgesComplete(targetLevel, new Network.ProfileBadgesListener() {
            @Override
            public void onFetchSuccess(ArrayList<Badge> badges) {
                addCompletedBadges(badges);
            }

            @Override
            public void onFetchFailure(Exception e) {}
        });

        network.getProfileBadgesRemaining(targetLevel, new Network.ProfileBadgesListener() {
            @Override
            public void onFetchSuccess(ArrayList<Badge> badges) {
                addRemainingBadges(badges);
            }

            @Override
            public void onFetchFailure(Exception e) {}
        });

        return rootView;
    }

    private void addRemainingBadges(ArrayList<Badge> badges) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                for (Badge badge : badges) {
                    addTableRow(badge.getName(), badge.getDescription(), badge.getId(), badge.getLevel(), true);
                }
            });
        }
    }

    private void addCompletedBadges(ArrayList<Badge> badges) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                for (Badge badge : badges) {
                    addTableRow(badge.getName(), badge.getDescription(), badge.getId(), badge.getLevel(), false);
                }
            });
        }
    }

    private void addTableRow(final String name, final String description, final long badgeId,
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
            network.getFirstPhoto((int)badgeId, view);

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
                            .commit();
                });
            }

            tr.addView(linearLayout);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}