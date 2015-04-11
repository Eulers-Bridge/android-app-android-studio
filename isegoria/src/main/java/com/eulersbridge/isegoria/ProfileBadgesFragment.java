package com.eulersbridge.isegoria;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
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

import com.actionbarsherlock.app.SherlockFragment;

import java.io.InputStream;

public class ProfileBadgesFragment extends SherlockFragment {
    private View rootView;
    private TableLayout badgesTableLayout;
    private TableRow tr;

    private float dpWidth;
    private float dpHeight;
    private int photosPerRow = -1;
    private int fitPerRow = 0;
    private int squareSize;
    private int dividerPadding = 0;

    private boolean insertedFirstRow = false;
    private String photoAlbumName = "";

    private Network network;

    public ProfileBadgesFragment() {
        insertedFirstRow = false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.profile_badges_layout, container, false);
        getActivity().setTitle("Isegoria");
        Bundle bundle = this.getArguments();

        DisplayMetrics displayMetrics = getActivity().getResources().getDisplayMetrics();
        badgesTableLayout = (TableLayout) rootView.findViewById(R.id.profileBadgesTableLayout);

        dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        dpHeight = displayMetrics.heightPixels / displayMetrics.density;

        squareSize = (int) (displayMetrics.widthPixels / 3) - (10/3);
        fitPerRow = (int) 3;
        dividerPadding = (10/3);

        tr = new TableRow(getActivity());

        MainActivity mainActivity = (MainActivity) getActivity();
        network = mainActivity.getIsegoriaApplication().getNetwork();
        network.getProfileBadges(this);

        return rootView;
    }

    public void addBadge(final int badgeId, final String description, final String name) {
        addTableRow(name, description, badgeId);
    }

    public void addTableRow(final String name, final String description, final int badgeId) {
        try {
            int paddingMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                    (float)  6.666666667, getResources().getDisplayMetrics());
            int textSize1 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                    (float)  8.0, getResources().getDisplayMetrics());
            int textSize2 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                    (float)  6.0, getResources().getDisplayMetrics());
            int imageSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                    (float)  70.0, getResources().getDisplayMetrics());

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
            viewLinearLayout.setLayoutParams(new TableRow.LayoutParams(imageSize, (int) (imageSize)));
            //viewLinearLayout.setGravity(Gravity.CENTER);
            //viewLinearLayout.setBackgroundColor(Color.parseColor("#000000"));

            ImageView view = new ImageView(getActivity());
            //view.setColorFilter(Color.argb(125, 35, 35, 35));
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(imageSize, (int) (imageSize), 1.0f);
            layoutParams.gravity = Gravity.CENTER;
            view.setLayoutParams(layoutParams);
            view.setScaleType(ScaleType.CENTER_CROP);
            //view.setBackgroundColor(Color.GRAY);
            network.getFirstPhoto((int) badgeId, (int) badgeId, view);

            viewLinearLayout.addView(view);

            LinearLayout linearLayout = new LinearLayout(getActivity());
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            linearLayout.setGravity(Gravity.CENTER_VERTICAL);
            layoutParams = new TableRow.LayoutParams(squareSize, squareSize);
            layoutParams.gravity = Gravity.CENTER;
            linearLayout.setLayoutParams(layoutParams);
            linearLayout.setPadding(paddingMargin, 0, 0, 0);

            TextView nameTextView = new TextView(getActivity());
            nameTextView.setText(name);
            nameTextView.setGravity(Gravity.CENTER);
            nameTextView.setTypeface(Typeface.DEFAULT_BOLD);
            nameTextView.setTextSize(textSize1);

            TextView descTextView = new TextView(getActivity());
            descTextView.setText(description);
            descTextView.setGravity(Gravity.CENTER);
            descTextView.setTextSize(textSize2);

            linearLayout.addView(viewLinearLayout);
            linearLayout.addView(nameTextView);
            linearLayout.addView(descTextView);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });

            tr.addView(linearLayout);
        } catch(Exception e) {

        }
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmapFromBitmap(InputStream is,
                                                       int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeStream(is);
    }
}
