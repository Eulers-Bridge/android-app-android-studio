package com.eulersbridge.isegoria;

import com.actionbarsherlock.app.SherlockFragment;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Typeface;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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

public class CandidateAllFragment extends SherlockFragment {
	private View rootView;
	private TableLayout candidateAllTableLayout;
	
	private float dpWidth;
	private float dpHeight;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {   
		DisplayMetrics displayMetrics = getActivity().getResources().getDisplayMetrics();
		
		rootView = inflater.inflate(R.layout.candidate_all_fragment, container, false);
		candidateAllTableLayout = (TableLayout) rootView.findViewById(R.id.candidateAllTable);

		dpWidth = displayMetrics.widthPixels;
        dpHeight = displayMetrics.heightPixels / displayMetrics.density;
        
        View dividierView = new View(getActivity());
        dividierView.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, 1));
        dividierView.setBackgroundColor(Color.parseColor("#676475"));
        candidateAllTableLayout.addView(dividierView);
        
        addTableRow(R.drawable.head1, "GRN", "#4FBE3E", "Lillian Adams", "President");
        addTableRow(R.drawable.head1, "GRN", "#4FBE3E", "Lillian Adams", "President");
        addTableRow(R.drawable.head1, "GRN", "#4FBE3E", "Lillian Adams", "President");
        addTableRow(R.drawable.head1, "GRN", "#4FBE3E", "Lillian Adams", "President");
        addTableRow(R.drawable.head1, "GRN", "#4FBE3E", "Lillian Adams", "President");
        
		return rootView;
	}
	
	public void addTableRow(int profileDrawable, String partyAbr, String colour, String candidateName, String candidatePosition) {
		TableRow tr;
		
		LinearLayout layout = new LinearLayout(getActivity());
		layout.setOrientation(LinearLayout.HORIZONTAL);
		
		tr = new TableRow(getActivity());
		TableRow.LayoutParams rowParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
		tr.setLayoutParams(rowParams);
		tr.setPadding(0, 10, 0, 10);
		
		ImageView candidateProfileView = new ImageView(getActivity());
		candidateProfileView.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
		candidateProfileView.setScaleType(ScaleType.CENTER_CROP);
		candidateProfileView.setImageBitmap(decodeSampledBitmapFromResource(getResources(), profileDrawable, 80, 80));
		candidateProfileView.setPadding(10, 0, 10, 0);
		
		ImageView candidateProfileImage = new ImageView(getActivity());
		candidateProfileImage.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, Gravity.RIGHT));
		candidateProfileImage.setScaleType(ScaleType.CENTER_CROP);
		candidateProfileImage.setImageBitmap(decodeSampledBitmapFromResource(getResources(), R.drawable.profilelight, 80, 80));
		candidateProfileImage.setPadding(10, 0, 10, 0);
		
        TextView textViewParty = new TextView(getActivity());
        textViewParty.setTextColor(Color.parseColor("#FFFFFF"));
        textViewParty.setTextSize(12.0f);
        textViewParty.setText(partyAbr);
        textViewParty.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        textViewParty.setGravity(Gravity.CENTER);
        textViewParty.setTypeface(null, Typeface.BOLD);
		
        RectShape rect = new RectShape();
        ShapeDrawable rectShapeDrawable = new ShapeDrawable(rect);
        Paint paint = rectShapeDrawable.getPaint();
        paint.setColor(Color.parseColor(colour));
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setStrokeWidth(5);	     
        
		LinearLayout partyLayout = new LinearLayout(getActivity());
		partyLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
        		80, 40);
        params.gravity = Gravity.CENTER_VERTICAL;
        partyLayout.setLayoutParams(params);
		partyLayout.setBackgroundDrawable(rectShapeDrawable);
		partyLayout.addView(textViewParty);
		
        TextView textViewCandidate = new TextView(getActivity());
        textViewCandidate.setTextColor(Color.parseColor("#3A3F43"));
        textViewCandidate.setTextSize(16.0f);
        textViewCandidate.setText(candidateName);
        textViewCandidate.setPadding(10, 0, 10, 0);
        textViewCandidate.setGravity(Gravity.LEFT);
        
        TextView textViewPosition = new TextView(getActivity());
        textViewPosition.setTextColor(Color.parseColor("#3A3F43"));
        textViewPosition.setTextSize(12.0f);
        textViewPosition.setText(candidatePosition);
        textViewPosition.setPadding(10, 0, 10, 0);
        textViewPosition.setGravity(Gravity.LEFT);
        
        View dividierView = new View(getActivity());
        dividierView.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, 1));
        dividierView.setBackgroundColor(Color.parseColor("#676475"));

        RelativeLayout relLayoutMaster = new RelativeLayout(getActivity());
        TableRow.LayoutParams relLayoutMasterParam = new TableRow.LayoutParams((int)dpWidth, TableRow.LayoutParams.WRAP_CONTENT); 
        relLayoutMaster.setLayoutParams(relLayoutMasterParam);
        
        RelativeLayout.LayoutParams relativeParamsLeft = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        relativeParamsLeft.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        
        RelativeLayout.LayoutParams relativeParamsRight = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        relativeParamsRight.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        
        LinearLayout linLayout = new LinearLayout(getActivity());
        linLayout.setOrientation(LinearLayout.VERTICAL);
        LayoutParams linLayoutParam = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT); 
        linLayout.addView(textViewCandidate);
        linLayout.addView(textViewPosition);
        
        LinearLayout linLayout2 = new LinearLayout(getActivity());
        linLayout2.setOrientation(LinearLayout.VERTICAL);
        LayoutParams linLayoutParam2 = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT); 
        linLayout2.addView(candidateProfileImage);
        linLayout2.setGravity(Gravity.RIGHT);
        linLayout2.setLayoutParams(relativeParamsRight); 
        
		layout.addView(candidateProfileView);
		layout.addView(partyLayout);
		layout.addView(linLayout);
		layout.setLayoutParams(relativeParamsLeft);
		
		relLayoutMaster.addView(layout);
		relLayoutMaster.addView(linLayout2);
        
        tr.addView(relLayoutMaster);
        
		candidateAllTableLayout.addView(tr);
		candidateAllTableLayout.addView(dividierView);
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
