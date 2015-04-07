package com.eulersbridge.isegoria;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
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

import com.actionbarsherlock.app.SherlockFragment;

import java.io.InputStream;

public class PhotoAlbumFragment extends SherlockFragment {
	private View rootView;
	private TableLayout photosAlbumTableLayout;
	private TableRow tr;
	
	private float dpWidth;
	private float dpHeight;
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
		rootView = inflater.inflate(R.layout.photo_album_fragment, container, false);
		getActivity().setTitle("Isegoria");
		Bundle bundle = this.getArguments();
		photoAlbumName = (String) bundle.getString("Album");

		DisplayMetrics displayMetrics = getActivity().getResources().getDisplayMetrics();
		photosAlbumTableLayout = (TableLayout) rootView.findViewById(R.id.photosAlbumTableLayout);

		dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        dpHeight = displayMetrics.heightPixels / displayMetrics.density;

        int paddingMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                (float) 6.666666667, getResources().getDisplayMetrics());

        squareSize = (int) (displayMetrics.widthPixels / 4) - (10/4);
        fitPerRow = (int) 4;
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

	public void addTableRow(String bitmap, final int photoPath) {
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
            view.setLayoutParams(new LinearLayout.LayoutParams(squareSize, (int) (squareSize), 1.0f));
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
                    FragmentManager fragmentManager2 = getSherlockActivity().getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction2 = fragmentManager2.beginTransaction();
                    PhotoViewFragment fragment2 = new PhotoViewFragment();
                    Bundle args = new Bundle();
                    args.putString("PhotoName", (String) String.valueOf(photoPath));
                    fragment2.setArguments(args);
                    fragmentTransaction2.addToBackStack(null);
                    fragmentTransaction2.add(R.id.photosFrameLayout, photoViewPagerFragment);
                    photoViewPagerFragment.setPosition(index);
                    fragmentTransaction2.commit();
                }
            });



            tr.addView(viewLinearLayout);
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
