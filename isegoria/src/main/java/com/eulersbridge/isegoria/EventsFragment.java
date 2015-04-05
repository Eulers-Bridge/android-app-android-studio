package com.eulersbridge.isegoria;

import android.content.res.Resources;
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
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TableRow.LayoutParams;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;

public class EventsFragment extends SherlockFragment {
	private View rootView;
	private TableLayout newsTableLayout;
	
	private float dpWidth;
	private float dpHeight;

    private EventsDetailFragment fragment2;
    private Network network;
	
	public EventsFragment() {
	
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {   
		DisplayMetrics displayMetrics = getActivity().getResources().getDisplayMetrics();
		rootView = inflater.inflate(R.layout.events_fragment, container, false);
		newsTableLayout = (TableLayout) rootView.findViewById(R.id.eventsTableLayout);
		
		dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        dpHeight = displayMetrics.heightPixels;  
	
        MainActivity mainActivity = (MainActivity) getActivity();
        network = mainActivity.getIsegoriaApplication().getNetwork();
        network.getEvents(this);

		return rootView;
	}
	
	public void addEvent(final int eventId, final String eventName, final long eventTime, final String bitmapPicture) {
        String eventTimeStr = TimeConverter.convertTimestampToString(eventTime);
        addTableRow(eventId, bitmapPicture, false, eventName, eventTimeStr);
	}
	
	public void addTableRow(final int eventId, String bitmapPicture, boolean lastCell, String articleTitle1, String articleTime1) {
		TableRow tr;
		String colour = "#F8F8F8";
		
		if(bitmapPicture == null) {
			colour = "#000000";
		}
		
		tr = new TableRow(getActivity());
		TableRow.LayoutParams rowParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT);
		tr.setLayoutParams(rowParams);
			
		RelativeLayout relativeLayout = new RelativeLayout(getActivity());
		relativeLayout.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, (int)(dpHeight / 3.5)));
		((TableRow.LayoutParams) relativeLayout.getLayoutParams()).span = 2;
		if(lastCell)
				((ViewGroup.MarginLayoutParams) relativeLayout.getLayoutParams()).setMargins(5, 5, 5, 5);
		else
			((ViewGroup.MarginLayoutParams) relativeLayout.getLayoutParams()).setMargins(5, 5, 5, 0);
			
		ImageView view = new ImageView(getActivity());
		view.setColorFilter(Color.argb(125, 35, 35, 35));
		view.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, (int)(dpHeight / 3.5)));
		view.setScaleType(ScaleType.CENTER_CROP);
		network.getPictureVolley(bitmapPicture, view);
		
		view.setOnClickListener(new View.OnClickListener() {        
            @Override
            public void onClick(View view) {
		    		FragmentManager fragmentManager2 = getSherlockActivity().getSupportFragmentManager();
		    		FragmentTransaction fragmentTransaction2 = fragmentManager2.beginTransaction();
		    		fragment2 = new EventsDetailFragment();
		    		Bundle args = new Bundle();
		    		args.putInt("EventId", eventId);
		    		fragment2.setArguments(args);
		    		fragmentTransaction2.add(R.id.eventsFrameLayout, fragment2);
                    fragmentTransaction2.addToBackStack("");
		    		fragmentTransaction2.commit();
            }
         });
	        
	    TextView textViewArticle = new TextView(getActivity());
	    textViewArticle.setTextColor(Color.parseColor(colour));
	    textViewArticle.setTextSize(20.0f);
	    textViewArticle.setText(articleTitle1);
	    textViewArticle.setGravity(Gravity.CENTER);
	        
	    TextView textViewArticleTime = new TextView(getActivity());
	    textViewArticleTime.setTextColor(Color.parseColor(colour));
	    textViewArticleTime.setTextSize(12.0f);
	    textViewArticleTime.setText(articleTime1);
	    textViewArticleTime.setPadding(0, 100, 0, 0);
	    textViewArticleTime.setGravity(Gravity.CENTER);
	        
	    RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);  
	    params1.addRule(RelativeLayout.CENTER_HORIZONTAL, textViewArticle.getId());
	    params1.addRule(RelativeLayout.CENTER_VERTICAL, textViewArticle.getId());
	        
	    RelativeLayout.LayoutParams params2 = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);  
	    params2.addRule(RelativeLayout.CENTER_HORIZONTAL, textViewArticle.getId());
	    params2.addRule(RelativeLayout.CENTER_VERTICAL, textViewArticle.getId());
	        
	    relativeLayout.addView(view);
	    relativeLayout.addView(textViewArticle, params1);
	    relativeLayout.addView(textViewArticleTime, params2);
	        
	    tr.addView(relativeLayout);	
	    newsTableLayout.addView(tr);
	}
	
	public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
	    // Raw height and width of image
	    final int height = options.outHeight;
	    final int width = options.outWidth;
	    int inSampleSize = 1;
	
	    if (height > reqHeight || width > reqWidth) {
	
	        final int halfHeight = height / 2;
	        final int halfWidth = width / 2;
	
	        // Calculate the largest inSampleSize value that is a power of 2 and keeps both
	        // height and width larger than the requested height and width.
	        while ((halfHeight / inSampleSize) > reqHeight
	                && (halfWidth / inSampleSize) > reqWidth) {
	            inSampleSize *= 2;
	        }
	    }
	
	    return inSampleSize;
	}
	
	public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
	        int reqWidth, int reqHeight) {

	    // First decode with inJustDecodeBounds=true to check dimensions
	    final BitmapFactory.Options options = new BitmapFactory.Options();
	    options.inJustDecodeBounds = true;
	    BitmapFactory.decodeResource(res, resId, options);

	    // Calculate inSampleSize
	    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

	    // Decode bitmap with inSampleSize set
	    options.inJustDecodeBounds = false;
	    return BitmapFactory.decodeResource(res, resId, options);
	}
}