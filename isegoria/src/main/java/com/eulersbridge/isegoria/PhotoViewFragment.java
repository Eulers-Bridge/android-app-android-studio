package com.eulersbridge.isegoria;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;

import java.io.InputStream;

public class PhotoViewFragment extends SherlockFragment {
	private View rootView;
	
	private float dpWidth;
	private float dpHeight;
	
	private DisplayMetrics displayMetrics;
	private int photoPath;

    private Network network;
    private Bitmap imageBitmap;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {   
		rootView = inflater.inflate(R.layout.photo_view_fragment, container, false);
		getActivity().setTitle("Isegoria");
		Bundle bundle = this.getArguments();
		photoPath = (int) bundle.getInt("PhotoId");
		
		displayMetrics = getActivity().getResources().getDisplayMetrics();
		dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        dpHeight = displayMetrics.heightPixels / displayMetrics.density;

        addPhoto("", imageBitmap);

        MainActivity mainActivity = (MainActivity) getActivity();
        network = mainActivity.getIsegoriaApplication().getNetwork();
        network.getPhoto(this, photoPath);

		return rootView;
	}

    public void setImageBitmap(Bitmap bitmap) {
        this.imageBitmap = bitmap;
    }

    public void setData(String title, long date, final boolean inappropriateContent) {
        String dateStr = TimeConverter.convertTimestampToString(date);

        TextView photoTileTextView = (TextView) rootView.findViewById(R.id.photoTitle);
        TextView photoDateTextView = (TextView) rootView.findViewById(R.id.photoDate);

        ImageView flagged = (ImageView) rootView.findViewById(R.id.photoFlag);

        if(!inappropriateContent) {
            flagged.setImageResource(R.drawable.flag);
        }

        photoTileTextView.setText(title);
        photoDateTextView.setText(dateStr);
    }
	
	public void addPhoto(final String title, final Bitmap bitmap) {
		try {
			getActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					AssetManager assetManager = getActivity().getAssets();
					TextView photoTitle = (TextView) rootView.findViewById(R.id.photoTitle);
					photoTitle.setText(title);
					ImageView photoImageView = (ImageView) rootView.findViewById(R.id.profilePic);
					try {
						photoImageView.setScaleType(ScaleType.CENTER_CROP);
						photoImageView.setImageBitmap(bitmap);
						photoImageView.getLayoutParams().width = (int) displayMetrics.widthPixels;
						photoImageView.getLayoutParams().height = (int) (displayMetrics.heightPixels / 2.5);
						photoImageView.setPadding(0, 0, 0, (displayMetrics.heightPixels / 20));
					} catch (Exception e) {
						e.printStackTrace();
					}   
				}
			});
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
