package com.eulersbridge.isegoria;


import android.content.res.Resources;
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
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TableRow.LayoutParams;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;

public class CandidatePositionsFragment extends SherlockFragment {
	private View rootView;
	private TableLayout positionsTableLayout;
    private Network network;
	
	private float dpWidth;
	private float dpHeight;

    private int lastElectionId;
    private int lastPositionId;
    private String lastName;
    private String lastDesc;

    boolean addRow = false;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {   
		DisplayMetrics displayMetrics = getActivity().getResources().getDisplayMetrics();
		
		rootView = inflater.inflate(R.layout.election_positions_fragment, container, false);
		positionsTableLayout = (TableLayout) rootView.findViewById(R.id.positionsTableLayout);

		dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        dpHeight = displayMetrics.heightPixels / displayMetrics.density;  
        
        /*addTableRow(R.drawable.photo0, R.drawable.photo1, true, false, "President", "Secretary");
        addTableRow(R.drawable.photo2, R.drawable.photo3, true, false, "Women's Officer", "LGBT Officer");
        addTableRow(R.drawable.photo4, R.drawable.photopaddingMargin, true, false, "Clubs and Societies", "Environment Officer");
        addTableRow(R.drawable.photo6, R.drawable.photo7, true, false, "Welfare Officer", "Creative Arts Officer");
        addTableRow(R.drawable.photo8, R.drawable.photo9, true, false, "Faculty Liaison", "");*/

        MainActivity mainActivity = (MainActivity) getActivity();
        network = mainActivity.getIsegoriaApplication().getNetwork();
        network.getPositions(this);
        
		return rootView;
	}

    public void addPosition(int electionId, int positionId, String name, String desc) {
        if(addRow) {
            addTableRow(lastElectionId, electionId, lastPositionId, positionId, true, false, lastName, name);
        }

        this.lastElectionId = electionId;
        this.lastPositionId = positionId;
        this.lastName = name;
        this.lastDesc = desc;

        if(addRow) {
            addRow = false;
        }
        else {
            addRow = true;
        }
    }
	
	public void addTableRow(int lastElectionId, int electionId, final int lastPositionId, final int positionId, boolean doubleCell, boolean lastCell, String title1, String title2) {
		TableRow tr;

        int paddingMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                (float) 3.2, getResources().getDisplayMetrics());
        int paddingMargin2 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                (float) 90, getResources().getDisplayMetrics());
        int paddingMargin3 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                (float) 23.333, getResources().getDisplayMetrics());
        int paddingMargin4 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                (float) 83.33, getResources().getDisplayMetrics());
        int imageHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                (float) 170, getResources().getDisplayMetrics());
		
		if(doubleCell) {
			tr = new TableRow(getActivity());
			TableRow.LayoutParams rowParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT);
			tr.setLayoutParams(rowParams);
			
			RelativeLayout relativeLayout = new RelativeLayout(getActivity());
			relativeLayout.setLayoutParams(new TableRow.LayoutParams((int)(dpWidth / 2), imageHeight));
			if(lastCell)
				((ViewGroup.MarginLayoutParams) relativeLayout.getLayoutParams()).setMargins(paddingMargin, paddingMargin, paddingMargin, paddingMargin);
			else
				((ViewGroup.MarginLayoutParams) relativeLayout.getLayoutParams()).setMargins(paddingMargin, paddingMargin, paddingMargin, 0);

	        TextView textViewTitle = new TextView(getActivity());
	        textViewTitle.setTextColor(Color.parseColor("#F8F8F8"));
	        textViewTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP,16.0f);
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
			view.setColorFilter(Color.argb(paddingMargin4, paddingMargin3, paddingMargin3, paddingMargin3));
			view.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT));
			view.setScaleType(ScaleType.CENTER_CROP);
            network.getFirstPhoto(this.lastElectionId, this.lastPositionId, view);
	        view.setOnClickListener(new View.OnClickListener() {        
	            @Override
	            public void onClick(View view) {
                    FragmentManager fragmentManager2 = getSherlockActivity().getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction2 = fragmentManager2.beginTransaction();
                    CandidatePositionFragment fragment2 = new CandidatePositionFragment();
                    Bundle args = new Bundle();
                    args.putInt("PositionId", positionId);
                    fragment2.setArguments(args);
                    fragmentTransaction2.addToBackStack(null);
                    fragmentTransaction2.add(R.id.candidate_frame1, fragment2);
                    fragmentTransaction2.commit();
	            }
	         });
	        relativeLayout.addView(view);
	        relativeLayout.addView(textViewTitle, params1);
	        tr.addView(relativeLayout);
	        
			relativeLayout = new RelativeLayout(getActivity());
			relativeLayout.setLayoutParams(new TableRow.LayoutParams((int)(dpWidth / 2), imageHeight));
			if(lastCell)
				((ViewGroup.MarginLayoutParams) relativeLayout.getLayoutParams()).setMargins(0, paddingMargin, paddingMargin, paddingMargin);
			else
				((ViewGroup.MarginLayoutParams) relativeLayout.getLayoutParams()).setMargins(0, paddingMargin, paddingMargin, 0);
			
	        textViewTitle = new TextView(getActivity());
	        textViewTitle.setTextColor(Color.parseColor("#F8F8F8"));
	        textViewTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP,16.0f);
	        textViewTitle.setText(title2);
	        textViewTitle.setPadding(10, 0, 10, 0);
	        textViewTitle.setGravity(Gravity.CENTER);
	        
	        params1 = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);  
	        params1.addRule(RelativeLayout.CENTER_HORIZONTAL, textViewTitle.getId());
	        params1.addRule(RelativeLayout.CENTER_VERTICAL, textViewTitle.getId());
	        
	        params2 = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);  
	        params2.addRule(RelativeLayout.CENTER_HORIZONTAL, textViewTitle.getId());
	        params2.addRule(RelativeLayout.CENTER_VERTICAL, textViewTitle.getId());
			
			ImageView view2 = new ImageView(getActivity());
            view2.setColorFilter(Color.argb(paddingMargin4, paddingMargin3, paddingMargin3, paddingMargin3));
            view2.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT));
            view2.setScaleType(ScaleType.CENTER_CROP);
            network.getFirstPhoto(electionId, positionId, view2);
            view2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    FragmentManager fragmentManager2 = getSherlockActivity().getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction2 = fragmentManager2.beginTransaction();
                    CandidatePositionFragment fragment2 = new CandidatePositionFragment();
                    Bundle args = new Bundle();
                    args.putInt("PositionId", positionId);
                    fragment2.setArguments(args);
                    fragmentTransaction2.addToBackStack(null);
                    fragmentTransaction2.add(R.id.candidate_frame1, fragment2);
                    fragmentTransaction2.commit();
                }
            });
	        relativeLayout.addView(view2);
	        relativeLayout.addView(textViewTitle, params1);
	        tr.addView(relativeLayout);
	        
	        positionsTableLayout.addView(tr);
		}
		else {
			tr = new TableRow(getActivity());
			TableRow.LayoutParams rowParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT);
			tr.setLayoutParams(rowParams);
			
			RelativeLayout relativeLayout = new RelativeLayout(getActivity());
			relativeLayout.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, imageHeight));
			((TableRow.LayoutParams) relativeLayout.getLayoutParams()).span = 2;
			if(lastCell)
				((ViewGroup.MarginLayoutParams) relativeLayout.getLayoutParams()).setMargins(paddingMargin, paddingMargin, paddingMargin, paddingMargin);
			else
				((ViewGroup.MarginLayoutParams) relativeLayout.getLayoutParams()).setMargins(paddingMargin, paddingMargin, paddingMargin, 0);
			
			ImageView view = new ImageView(getActivity());
			view.setColorFilter(Color.argb(paddingMargin4, paddingMargin3, paddingMargin3, paddingMargin3));
			view.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, imageHeight));
			view.setScaleType(ScaleType.CENTER_CROP);
	        
	        TextView textViewTitle = new TextView(getActivity());
	        textViewTitle.setTextColor(Color.parseColor("#F8F8F8"));
	        textViewTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP,20.0f);
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
