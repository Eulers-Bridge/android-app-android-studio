package com.eulersbridge.isegoria;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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

public class PhotoAlbumFragment extends Fragment {
    private TableLayout photosAlbumTableLayout;
	private TableRow tr;

	private int photosPerRow = -1;
	private int fitPerRow = 0;
	private int squareSize;
	private int dividerPadding = 0;

	private boolean insertedFirstRow = false;
	private String photoAlbumName = "";

    private PhotoViewPagerFragment photoViewPagerFragment;

    private Network network;

	public PhotoAlbumFragment() {
		insertedFirstRow = false;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.photo_album_fragment, container, false);
		getActivity().setTitle(getString(R.string.app_name));
		Bundle bundle = this.getArguments();
		photoAlbumName = bundle.getString("Album");

		DisplayMetrics displayMetrics = getActivity().getResources().getDisplayMetrics();
		photosAlbumTableLayout = rootView.findViewById(R.id.photosAlbumTableLayout);

        int paddingMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                (float) 6.666666667, getResources().getDisplayMetrics());

        squareSize = displayMetrics.widthPixels / 4 - (10/4);
        fitPerRow = 4;
        dividerPadding = (10/4);

        tr = new TableRow(getActivity());

        photoViewPagerFragment = new PhotoViewPagerFragment();

        MainActivity mainActivity = (MainActivity) getActivity();
        network = mainActivity.getIsegoriaApplication().getNetwork();
        network.getPhotoAlbum(this, photoAlbumName);

		return rootView;
	}

	public void addPhotoThumb(final String bitmap, final int photoId) {
		addTableRow(bitmap, photoId);
	}

	private void addTableRow(String bitmap, final int photoPath) {
        try {
            int paddingMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                    (float) 6.666666667, getResources().getDisplayMetrics());

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
                photosAlbumTableLayout.addView(tr);
            }

            LinearLayout viewLinearLayout = new LinearLayout(getActivity());
            viewLinearLayout.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
            viewLinearLayout.setBackgroundColor(Color.parseColor("#000000"));

            ImageView view = new ImageView(getActivity());
            //view.setColorFilter(Color.argb(125, 35, 35, 35));
            view.setLayoutParams(new LinearLayout.LayoutParams(squareSize, squareSize, 1.0f));
            view.setScaleType(ScaleType.CENTER_CROP);
            view.setBackgroundColor(Color.GRAY);

            viewLinearLayout.addView(view);

            LinearLayout linearLayout = new LinearLayout(getActivity());
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            linearLayout.setGravity(Gravity.CENTER_VERTICAL);
            linearLayout.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT));
            linearLayout.setPadding(paddingMargin, 0, 0, 0);

            PhotoViewFragment fragment2 = new PhotoViewFragment();
            Bundle args = new Bundle();
            args.putInt("PhotoId", photoPath);
            fragment2.setArguments(args);

            final int index = photoViewPagerFragment.addFragment(fragment2);
            network.getPictureVolley2(bitmap, view, squareSize, fragment2);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    FragmentManager fragmentManager2 = getActivity().getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction2 = fragmentManager2.beginTransaction();
                    PhotoViewFragment fragment2 = new PhotoViewFragment();
                    Bundle args = new Bundle();
                    args.putString("PhotoName", String.valueOf(photoPath));
                    fragment2.setArguments(args);
                    fragmentTransaction2.addToBackStack(null);
                    fragmentTransaction2.add(R.id.photosFrameLayout, photoViewPagerFragment);
                    photoViewPagerFragment.setPosition(index);
                    fragmentTransaction2.commit();
                }
            });



            tr.addView(viewLinearLayout);
            tr.addView(linearLayout);
        } catch(Exception ignored) {

        }
	}
}
