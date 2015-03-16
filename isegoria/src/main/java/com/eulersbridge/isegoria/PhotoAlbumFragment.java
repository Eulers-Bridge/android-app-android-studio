package com.eulersbridge.isegoria;

import java.io.IOException;
import java.io.InputStream;

import com.actionbarsherlock.app.SherlockFragment;


import android.content.res.AssetManager;
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
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.ImageView.ScaleType;

public class PhotoAlbumFragment extends SherlockFragment {
	private View rootView;
	private TableLayout photosAlbumTableLayout;
	private TableRow tr;
	
	private float dpWidth;
	private float dpHeight;
	private int photosPerRow = -1;
	private int fitPerRow = 0;
	private int squareSize = 125;
	private int dividerPadding = 0;
	
	private boolean insertedFirstRow = false;
	private String photoAlbumName = "";
	
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
        
        fitPerRow = (int) (dpWidth / squareSize) + 2;
        dividerPadding = (int)dpWidth - (fitPerRow * squareSize);
        dividerPadding = Math.abs(dividerPadding / fitPerRow) / 4;
        
        tr = new TableRow(getActivity());
        
        MainActivity mainActivity = (MainActivity) getActivity();
        Network network = mainActivity.getIsegoriaApplication().getNetwork();
        network.getPhotoAlbum(this, photoAlbumName);
		
		return rootView;
	}
	
	public void addPhotoThumb(final Bitmap bitmap, final String photoId) {
		try {
			getActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					addTableRow(bitmap, photoId);
				}
			});
		} catch(Exception e) {
			
		}
	}

	public void addTableRow(Bitmap bitmap, final String photoPath) {
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
		TableRow.LayoutParams rowParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT);
		tr.setLayoutParams(rowParams);
		
		ImageView view = new ImageView(getActivity());
		view.setLayoutParams(new TableRow.LayoutParams(squareSize, squareSize));
		view.setScaleType(ScaleType.CENTER_CROP);
        view.setImageBitmap(bitmap);
        
        view.setOnClickListener(new View.OnClickListener() {        
            @Override
            public void onClick(View view) {
		    		FragmentManager fragmentManager2 = getFragmentManager();
		    		FragmentTransaction fragmentTransaction2 = fragmentManager2.beginTransaction();
		    		PhotoViewFragment fragment2 = new PhotoViewFragment();
		    		Bundle args = new Bundle();
		    		args.putString("PhotoName", (String) photoPath);
		    		fragment2.setArguments(args);
		    		fragmentTransaction2.addToBackStack(null);
		    		fragmentTransaction2.replace(android.R.id.content, fragment2);
		    		fragmentTransaction2.commit();
            }
         });
		
		LinearLayout linearLayout = new LinearLayout(getActivity());
		linearLayout.setOrientation(LinearLayout.VERTICAL);
		linearLayout.setGravity(Gravity.CENTER_VERTICAL);
		linearLayout.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT));
		linearLayout.setPadding(dividerPadding, 0, 0, 0);
        
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
