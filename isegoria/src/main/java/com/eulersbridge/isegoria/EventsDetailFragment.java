package com.eulersbridge.isegoria;

import java.util.Calendar;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract.Events;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TableRow.LayoutParams;
import android.widget.TextView;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.ScaleDrawable;

public class EventsDetailFragment extends Fragment {
	private View rootView;
	private float dpWidth;
	private float dpHeight;
	private DisplayMetrics displayMetrics;
	private Isegoria isegoria;
	private long timestamp;
	private String eventTitle;
	private String eventDesc;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {   
		rootView = inflater.inflate(R.layout.events_detail_fragment, container, false);
		this.isegoria = (Isegoria) getActivity().getApplication();
		Bundle bundle = this.getArguments();
		
		Button button = (Button) rootView.findViewById(R.id.addToCalendar);
			button.setOnClickListener(new OnClickListener() {
				@Override
			    public void onClick(View v) {
					addToCalendar(v);
			    } 
			}); 

		displayMetrics = getActivity().getResources().getDisplayMetrics();
		dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        dpHeight = displayMetrics.heightPixels / displayMetrics.density;  
        
        isegoria.getNetwork().getEventDetails(this, bundle.getInt("EventId"));
		
		return rootView;
	}
	
	public void populateContent(final String title, final String content, final String location, final String likes, final Bitmap picture, final long timestamp) {
		try {
			this.timestamp = timestamp;
			getActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					LinearLayout backgroundLinearLayout = (LinearLayout) rootView.findViewById(R.id.topBackgroundNews);
					backgroundLinearLayout.getLayoutParams().height = (int) (displayMetrics.heightPixels / 2.7);
					//Bitmap original = BitmapFactory.decodeResource(getActivity().getResources(), backgroundDrawableResource);
					//Bitmap b = Bitmap.createScaledBitmap(original, (int)dpWidth, (int)dpHeight/2, false);
					Drawable d = new BitmapDrawable(getActivity().getResources(), picture);
					d.setColorFilter(Color.argb(125, 35, 35, 35), Mode.DARKEN);
					backgroundLinearLayout.setBackgroundDrawable(d);

					TextView eventTitleField = (TextView) rootView.findViewById(R.id.eventTitle);
					eventTitleField.setText(title);
					
					TextView eventTime = (TextView) rootView.findViewById(R.id.eventTime);
					eventTime.setText(TimeConverter.convertTimestampToString(timestamp));
					
					TextView eventLocationLine1 = (TextView) rootView.findViewById(R.id.eventLocationLine1);
					eventLocationLine1.setText(location);
					
					TextView eventsTextField = (TextView) rootView.findViewById(R.id.eventDetails);
					eventsTextField.setText(content);
					
					eventTitle = title;
					eventDesc = content;
				}
			});
		} catch(Exception e) {
			
		}
	}
	
	public void addToCalendar(View v) {
	    Calendar cal = Calendar.getInstance();     
        Intent intent = new Intent(Intent.ACTION_EDIT);
        intent.setType("vnd.android.cursor.item/event");
        intent.putExtra("beginTime", timestamp);
        intent.putExtra("allDay", false);
        intent.putExtra("endTime", timestamp+60*60*1000);
        intent.putExtra("title", eventTitle);
        intent.putExtra("description", eventDesc);
        isegoria.getMainActivity().startActivity(intent);
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