package com.eulersbridge.isegoria;

import com.actionbarsherlock.app.SherlockFragment;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.ImageView.ScaleType;
import android.widget.TableRow.LayoutParams;

public class CandidateTicketFragment extends SherlockFragment {
	private View rootView;
	private TableLayout positionsTableLayout;
	
	private float dpWidth;
	private float dpHeight;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {   
		DisplayMetrics displayMetrics = getActivity().getResources().getDisplayMetrics();
		
		rootView = inflater.inflate(R.layout.election_positions_fragment, container, false);
		rootView = inflater.inflate(R.layout.election_positions_fragment, container, false);
		positionsTableLayout = (TableLayout) rootView.findViewById(R.id.positionsTableLayout);

		dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        dpHeight = displayMetrics.heightPixels / displayMetrics.density;  
        
        addTableRow("#78BB88", "#697AB2", true, false, "Green Students", "Liberty", "1240 supporters", "1240 supporters");
        addTableRow("#D0A86A", "#C15650", true, false, "STAR", "Young Labor", "1240 supporters", "1240 supporters");
        addTableRow("#7A7981", "#53589A", true, true, "Young Liberal", "Socialist Alternative", "1240 supporters", "1240 supporters");
        
		return rootView;
	}
	
	public void addTableRow(String colour1, String colour2, boolean doubleCell, boolean lastCell, String title1, String title2, String supporters1, String supporters2) {
		TableRow tr;
		
		if(doubleCell) {
			tr = new TableRow(getActivity());
			TableRow.LayoutParams rowParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT);
			tr.setLayoutParams(rowParams);
			
			RelativeLayout relativeLayout = new RelativeLayout(getActivity());
			relativeLayout.setLayoutParams(new TableRow.LayoutParams((int)(dpWidth / 2), (int)(dpHeight / 2.3)));
			if(lastCell)
				((ViewGroup.MarginLayoutParams) relativeLayout.getLayoutParams()).setMargins(5, 5, 5, 5);
			else
				((ViewGroup.MarginLayoutParams) relativeLayout.getLayoutParams()).setMargins(5, 5, 5, 0);

	        TextView textViewTitle = new TextView(getActivity());
	        textViewTitle.setTextColor(Color.parseColor("#3A3F43"));
	        textViewTitle.setTextSize(16.0f);
	        textViewTitle.setText(title1);
	        textViewTitle.setPadding(10, 0, 10, 0);
	        textViewTitle.setGravity(Gravity.CENTER);
	        
	        TextView textViewTitleSuport1 = new TextView(getActivity());
	        textViewTitleSuport1.setTextColor(Color.parseColor(colour1));
	        textViewTitleSuport1.setTextSize(16.0f);
	        textViewTitleSuport1.setText(supporters1);
	        textViewTitleSuport1.setPadding(10, 0, 10, 0);
	        textViewTitleSuport1.setGravity(Gravity.CENTER);
	        
	        RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);  
	        params1.addRule(RelativeLayout.CENTER_HORIZONTAL, textViewTitle.getId());
	        params1.addRule(RelativeLayout.CENTER_VERTICAL, textViewTitle.getId());
			
	        RectShape rect = new RectShape();
	        ShapeDrawable rectShapeDrawable = new ShapeDrawable(rect);
	        Paint paint = rectShapeDrawable.getPaint();
	        paint.setColor(Color.parseColor(colour1));
	        paint.setStyle(Style.STROKE);
	        paint.setStrokeWidth(5);	        
	        
			View view = new View(getActivity());
			view.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT));
			view.setBackgroundDrawable(rectShapeDrawable);
	        view.setOnClickListener(new View.OnClickListener() {        
	            @Override
	            public void onClick(View view) {
			    		FragmentManager fragmentManager2 = getActivity().getSupportFragmentManager();
			    		FragmentTransaction fragmentTransaction2 = fragmentManager2.beginTransaction();
			    		CandidateTicketDetailFragment fragment2 = new CandidateTicketDetailFragment();
			    		Bundle args = new Bundle();
			    		fragment2.setArguments(args);
			    		fragmentTransaction2.addToBackStack(null);
			    		fragmentTransaction2.replace(R.id.content_frame, fragment2);
			    		fragmentTransaction2.commit();
	            }
	         });

	        LinearLayout linLayout = new LinearLayout(getActivity());
	        linLayout.setOrientation(LinearLayout.VERTICAL);
	        LayoutParams linLayoutParam = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT); 
	        linLayout.addView(textViewTitle);
	        linLayout.addView(textViewTitleSuport1);
			
	        relativeLayout.addView(view);
	        relativeLayout.addView(linLayout, params1);
	        tr.addView(relativeLayout);
	        
			relativeLayout = new RelativeLayout(getActivity());
			relativeLayout.setLayoutParams(new TableRow.LayoutParams((int)(dpWidth / 2), (int)(dpHeight / 2.3)));
			if(lastCell)
				((ViewGroup.MarginLayoutParams) relativeLayout.getLayoutParams()).setMargins(0, 5, 5, 5);
			else
				((ViewGroup.MarginLayoutParams) relativeLayout.getLayoutParams()).setMargins(0, 5, 5, 0);
			
	        textViewTitle = new TextView(getActivity());
	        textViewTitle.setTextColor(Color.parseColor("#3A3F43"));
	        textViewTitle.setTextSize(16.0f);
	        textViewTitle.setText(title2);
	        textViewTitle.setPadding(10, 0, 10, 0);
	        textViewTitle.setGravity(Gravity.CENTER);
	        
	        TextView textViewTitleSuport2 = new TextView(getActivity());
	        textViewTitleSuport2.setTextColor(Color.parseColor(colour2));
	        textViewTitleSuport2.setTextSize(16.0f);
	        textViewTitleSuport2.setText(supporters1);
	        textViewTitleSuport2.setPadding(10, 0, 10, 0);
	        textViewTitleSuport2.setGravity(Gravity.CENTER);
	        
	        params1 = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);  
	        params1.addRule(RelativeLayout.CENTER_HORIZONTAL, textViewTitle.getId());
	        params1.addRule(RelativeLayout.CENTER_VERTICAL, textViewTitle.getId());
			
	        RectShape rect2 = new RectShape();
	        ShapeDrawable rect2ShapeDrawable = new ShapeDrawable(rect2);
	        Paint paint2 = rect2ShapeDrawable.getPaint();
	        paint2.setColor(Color.parseColor(colour2));
	        paint2.setStyle(Style.STROKE);
	        paint2.setStrokeWidth(5);
	        
	        linLayout = new LinearLayout(getActivity());
	        linLayout.setOrientation(LinearLayout.VERTICAL);
	        linLayoutParam = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT); 
	        linLayout.addView(textViewTitle);
	        linLayout.addView(textViewTitleSuport2);
	        
			view = new View(getActivity());
			view.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT));
			view.setBackgroundDrawable(rect2ShapeDrawable);
	        view.setOnClickListener(new View.OnClickListener() {        
	            @Override
	            public void onClick(View view) {
			    		FragmentManager fragmentManager2 = getActivity().getSupportFragmentManager();
			    		FragmentTransaction fragmentTransaction2 = fragmentManager2.beginTransaction();
			    		CandidateTicketDetailFragment fragment2 = new CandidateTicketDetailFragment();
			    		Bundle args = new Bundle();
			    		fragment2.setArguments(args);
			    		fragmentTransaction2.addToBackStack(null);
			    		fragmentTransaction2.replace(R.id.content_frame, fragment2);
			    		fragmentTransaction2.commit();
	            }
	         });
	        
	        relativeLayout.addView(view);
	        relativeLayout.addView(linLayout, params1);
	        tr.addView(relativeLayout);
	        
	        positionsTableLayout.addView(tr);
		}
		else {
			tr = new TableRow(getActivity());
			TableRow.LayoutParams rowParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT);
			tr.setLayoutParams(rowParams);
			
			RelativeLayout relativeLayout = new RelativeLayout(getActivity());
			relativeLayout.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, (int)(dpHeight / 2.3)));
			((TableRow.LayoutParams) relativeLayout.getLayoutParams()).span = 2;
			if(lastCell)
				((ViewGroup.MarginLayoutParams) relativeLayout.getLayoutParams()).setMargins(5, 5, 5, 5);
			else
				((ViewGroup.MarginLayoutParams) relativeLayout.getLayoutParams()).setMargins(5, 5, 5, 0);
			
			View view = new View(getActivity());
			view.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, (int)(dpHeight / 2.3)));

	        TextView textViewTitle = new TextView(getActivity());
	        textViewTitle.setTextColor(Color.parseColor("#F8F8F8"));
	        textViewTitle.setTextSize(20.0f);
	        textViewTitle.setText(title1);
	        textViewTitle.setGravity(Gravity.CENTER);
	        
	        RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);  
	        params1.addRule(RelativeLayout.CENTER_HORIZONTAL, textViewTitle.getId());
	        params1.addRule(RelativeLayout.CENTER_VERTICAL, textViewTitle.getId());
	        
	        RelativeLayout.LayoutParams params2 = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);  
	        params2.addRule(RelativeLayout.CENTER_HORIZONTAL, textViewTitle.getId());
	        params2.addRule(RelativeLayout.CENTER_VERTICAL, textViewTitle.getId());
	        
	        relativeLayout.addView(view);
	        relativeLayout.addView(textViewTitle, params1);
	        
	        tr.addView(relativeLayout);	
	        positionsTableLayout.addView(tr);
		}
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
