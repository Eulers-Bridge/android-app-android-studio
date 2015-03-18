package com.eulersbridge.isegoria;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;
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

public class PhotoAlbumFragment extends SherlockFragment {
	private View rootView;
	private TableLayout photosAlbumTableLayout;
	private TableRow tr;
	
	private float dpWidth;
	private float dpHeight;
	private int photosPerRow = -1;
	private int fitPerRow = 0;
	private int squareSize = 100;
	private int dividerPadding = 0;
	
	private boolean insertedFirstRow = false;
	private String photoAlbumName = "";

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
        
        fitPerRow = (int) (dpWidth / squareSize);
        dividerPadding = (int)dpWidth - (fitPerRow * squareSize);
        dividerPadding = Math.abs(dividerPadding / fitPerRow);
        
        tr = new TableRow(getActivity());
        
        MainActivity mainActivity = (MainActivity) getActivity();
        network = mainActivity.getIsegoriaApplication().getNetwork();
        network.getPhotoAlbum(this, photoAlbumName);
		
		return rootView;
	}
	
	public void addPhotoThumb(final String bitmap, final String photoId) {
		addTableRow(bitmap, photoId);
	}

	public void addTableRow(String bitmap, final String photoPath) {
		photosPerRow = photosPerRow + 1;
		if(photosPerRow == fitPerRow) {
			photosPerRow = 0;
			if(!insertedFirstRow) {
				insertedFirstRow = true;
				tr.setPadding(dividerPadding, dividerPadding, dividerPadding, dividerPadding);
			}
			else {
				tr.setPadding(dividerPadding, 0, dividerPadding, dividerPadding);
			}
			photosAlbumTableLayout.addView(tr);
			tr = new TableRow(getActivity());
		}

        ImageView view = new ImageView(getActivity());
        view.setColorFilter(Color.argb(125, 35, 35, 35));
        view.setAdjustViewBounds(false);
        view.setLayoutParams(new TableRow.LayoutParams(100, (int)(100)));
        view.setScaleType(ScaleType.CENTER_CROP);
        network.getPictureVolley(bitmap, view);

        LinearLayout linearLayout = new LinearLayout(getActivity());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setGravity(Gravity.CENTER_VERTICAL);
        linearLayout.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT));
        linearLayout.setPadding(10, 0, 0, 0);

        final TextView textViewArticle = new TextView(getActivity());
        textViewArticle.setTextColor(Color.parseColor("#000000"));
        textViewArticle.setTextSize(18.0f);
        textViewArticle.setGravity(Gravity.LEFT);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fragmentManager2 = getFragmentManager();
                FragmentTransaction fragmentTransaction2 = fragmentManager2.beginTransaction();
                PhotoViewFragment fragment2 = new PhotoViewFragment();
                Bundle args = new Bundle();
                args.putString("PhotoName", (String) String.valueOf(photoPath));
                fragment2.setArguments(args);
                fragmentTransaction2.addToBackStack(null);
                fragmentTransaction2.replace(android.R.id.content, fragment2);
                fragmentTransaction2.commit();
            }
        });

        TextView textViewArticleTime = new TextView(getActivity());
        textViewArticleTime.setTextColor(Color.parseColor("#000000"));
        textViewArticleTime.setTextSize(12.0f);
        textViewArticleTime.setPadding(0, 0, 0, 0);
        textViewArticleTime.setGravity(Gravity.LEFT);

        linearLayout.addView(textViewArticle);
        linearLayout.addView(textViewArticleTime);

        tr.addView(view);
        tr.addView(linearLayout);
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
