package com.eulersbridge.isegoria;

import com.actionbarsherlock.app.SherlockFragment;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
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
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.ImageView.ScaleType;
import android.widget.TableRow.LayoutParams;

public class ElectionPositionsFragment extends SherlockFragment {
	private View rootView;
	private TableLayout positionsTableLayout;
	
	private float dpWidth;
	private float dpHeight;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {   
		DisplayMetrics displayMetrics = getActivity().getResources().getDisplayMetrics();
		
		rootView = inflater.inflate(R.layout.election_positions_fragment, container, false);
		positionsTableLayout = (TableLayout) rootView.findViewById(R.id.positionsTableLayout);

		dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        dpHeight = displayMetrics.heightPixels / displayMetrics.density;  
        
        addTableRow(R.drawable.photo0, R.drawable.photo1, true, false, "President", "Secretary");
        addTableRow(R.drawable.photo2, R.drawable.photo3, true, false, "Women's Officer", "LGBT Officer");
        addTableRow(R.drawable.photo4, R.drawable.photo5, true, false, "Clubs and Societies", "Environment Officer");
        addTableRow(R.drawable.photo6, R.drawable.photo7, true, false, "Welfare Officer", "Creative Arts Officer");
        addTableRow(R.drawable.photo8, R.drawable.photo9, true, false, "Faculty Liaison", "");
        
		return rootView;
	}
	
	public void addTableRow(int drawable1, int drawable2, boolean doubleCell, boolean lastCell, String title1, String title2) {
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
	        textViewTitle.setTextColor(Color.parseColor("#F8F8F8"));
	        textViewTitle.setTextSize(16.0f);
	        textViewTitle.setText(title1);
	        textViewTitle.setPadding(10, 0, 10, 0);
	        textViewTitle.setGravity(Gravity.CENTER);
	        
	        RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);  
	        params1.addRule(RelativeLayout.CENTER_HORIZONTAL, textViewTitle.getId());
	        params1.addRule(RelativeLayout.CENTER_VERTICAL, textViewTitle.getId());
	        
	        RelativeLayout.LayoutParams params2 = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);  
	        params2.addRule(RelativeLayout.CENTER_HORIZONTAL, textViewTitle.getId());
	        params2.addRule(RelativeLayout.CENTER_VERTICAL, textViewTitle.getId());
			
			ImageView view = new ImageView(getActivity());
			view.setColorFilter(Color.argb(125, 35, 35, 35));
			view.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT));
			view.setScaleType(ScaleType.CENTER_CROP);
	        view.setImageBitmap(decodeSampledBitmapFromResource(getResources(), drawable1, 100, 100));
	        view.setOnClickListener(new View.OnClickListener() {        
	            @Override
	            public void onClick(View view) {
			    		FragmentManager fragmentManager2 = getActivity().getSupportFragmentManager();
			    		FragmentTransaction fragmentTransaction2 = fragmentManager2.beginTransaction();
			    		ElectionPositionFragment fragment2 = new ElectionPositionFragment();
			    		Bundle args = new Bundle();
			    		fragment2.setArguments(args);
			    		fragmentTransaction2.addToBackStack(null);
			    		fragmentTransaction2.replace(R.id.content_election_frame1, fragment2);
			    		fragmentTransaction2.commit();
	            }
	         });
	        relativeLayout.addView(view);
	        relativeLayout.addView(textViewTitle, params1);
	        tr.addView(relativeLayout);
	        
			relativeLayout = new RelativeLayout(getActivity());
			relativeLayout.setLayoutParams(new TableRow.LayoutParams((int)(dpWidth / 2), (int)(dpHeight / 2.3)));
			if(lastCell)
				((ViewGroup.MarginLayoutParams) relativeLayout.getLayoutParams()).setMargins(0, 5, 5, 5);
			else
				((ViewGroup.MarginLayoutParams) relativeLayout.getLayoutParams()).setMargins(0, 5, 5, 0);
			
	        textViewTitle = new TextView(getActivity());
	        textViewTitle.setTextColor(Color.parseColor("#F8F8F8"));
	        textViewTitle.setTextSize(16.0f);
	        textViewTitle.setText(title2);
	        textViewTitle.setPadding(10, 0, 10, 0);
	        textViewTitle.setGravity(Gravity.CENTER);
	        
	        params1 = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);  
	        params1.addRule(RelativeLayout.CENTER_HORIZONTAL, textViewTitle.getId());
	        params1.addRule(RelativeLayout.CENTER_VERTICAL, textViewTitle.getId());
	        
	        params2 = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);  
	        params2.addRule(RelativeLayout.CENTER_HORIZONTAL, textViewTitle.getId());
	        params2.addRule(RelativeLayout.CENTER_VERTICAL, textViewTitle.getId());
			
			view = new ImageView(getActivity());
			view.setColorFilter(Color.argb(125, 35, 35, 35));
			view.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT));
			view.setScaleType(ScaleType.CENTER_CROP);
	        view.setImageBitmap(decodeSampledBitmapFromResource(getResources(),drawable2, 100, 100));
	        relativeLayout.addView(view);
	        relativeLayout.addView(textViewTitle, params1);
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
			
			ImageView view = new ImageView(getActivity());
			view.setColorFilter(Color.argb(125, 35, 35, 35));
			view.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, (int)(dpHeight / 2.3)));
			view.setScaleType(ScaleType.CENTER_CROP);
	        view.setImageBitmap(decodeSampledBitmapFromResource(getResources(),drawable1, 100, 100));
	        
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
