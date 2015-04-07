package com.eulersbridge.isegoria;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
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
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TableRow.LayoutParams;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;

import java.util.ArrayList;

public class CandidateAllFragment extends SherlockFragment {
	private View rootView;
	private TableLayout candidateAllTableLayout;
    private SearchView searchViewCandidatesAll;
    private ArrayList<String> firstnames = new ArrayList<String>();
    private ArrayList<String> lastnames = new ArrayList<String>();
    private ArrayList<TableRow> rows = new ArrayList<TableRow>();
	
	private float dpWidth;
	private float dpHeight;

    private CandidateAllFragment candidateAllFragment;
    private Network network;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        candidateAllFragment = this;
		DisplayMetrics displayMetrics = getActivity().getResources().getDisplayMetrics();
		
		rootView = inflater.inflate(R.layout.candidate_all_fragment, container, false);
		candidateAllTableLayout = (TableLayout) rootView.findViewById(R.id.candidateAllTable);

		dpWidth = displayMetrics.widthPixels;
        dpHeight = displayMetrics.heightPixels / displayMetrics.density;
        
        View dividierView = new View(getActivity());
        dividierView.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, 1));
        dividierView.setBackgroundColor(Color.parseColor("#676475"));
        candidateAllTableLayout.addView(dividierView);

        MainActivity mainActivity = (MainActivity) getActivity();
        network = mainActivity.getIsegoriaApplication().getNetwork();
        network.getCandidates(this);

        searchViewCandidatesAll = (SearchView) rootView.findViewById(R.id.searchViewCandidatesAll);
        searchViewCandidatesAll.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String query) {
                addAllRows();
                if(query.length() != 0) {
                    int cnt = 0;
                    for(int i=0; i<rows.size(); i++) {
                        View view = rows.get(i);
                        if (view instanceof TableRow) {
                            try {
                                TableRow row = (TableRow) view;
                                String firstname = firstnames.get(i);
                                String lastname = lastnames.get(i);

                                if(firstname.toLowerCase().indexOf(query.toLowerCase()) == -1 && lastname.toLowerCase().indexOf(query.toLowerCase()) == -1) {
                                    candidateAllTableLayout.removeView(row);
                                }
                                cnt = cnt + 1;
                            } catch(Exception e) {}
                        }
                    }
                    candidateAllFragment.rootView.invalidate();

                    return true;
                }
                return false;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                addAllRows();
                if(query.length() != 0) {
                    int cnt = 0;
                    for(int i=0; i<rows.size(); i++) {
                        View view = rows.get(i);
                        if (view instanceof TableRow) {
                            try {
                                TableRow row = (TableRow) view;
                                String firstname = firstnames.get(i);
                                String lastname = lastnames.get(i);

                                if(firstname.toLowerCase().indexOf(query.toLowerCase()) == -1 && lastname.toLowerCase().indexOf(query.toLowerCase()) == -1) {
                                    candidateAllTableLayout.removeView(row);
                                }
                                cnt = cnt + 1;
                            } catch(Exception e) {}
                        }
                    }
                    candidateAllFragment.rootView.invalidate();

                    return true;
                }
                return false;
            }
        });
        
		return rootView;
	}

    public void addAllRows() {
        try {
            candidateAllTableLayout.removeAllViews();
            for (int i = 0; i < rows.size(); i++) {
                candidateAllTableLayout.addView(rows.get(i));
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void addCandidate(int userId, int ticketId, int positionId, int candidateId,
                             String firstName, String lastName) {
        addTableRow(ticketId, userId, "GRN", "#4FBE3E", firstName + " " + lastName, "", positionId,
                firstName, lastName);
    }
	
	public void addTableRow(int ticketId, final int userId, String partyAbr,
                            String colour, String candidateName,
                            String candidatePosition, int positionId,
                            String firstName, String lastName) {
		TableRow tr;
		
		LinearLayout layout = new LinearLayout(getActivity());
		layout.setOrientation(LinearLayout.HORIZONTAL);

        int paddingMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                (float) 6.5, getResources().getDisplayMetrics());
		
		tr = new TableRow(getActivity());
		TableRow.LayoutParams rowParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
		tr.setLayoutParams(rowParams);
		tr.setPadding(0, paddingMargin, 0, paddingMargin);

        int imageSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                (float) 53, getResources().getDisplayMetrics());
		
		ImageView candidateProfileView = new ImageView(getActivity());
		candidateProfileView.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(imageSize, imageSize);
        candidateProfileView.setLayoutParams(layoutParams);
		candidateProfileView.setScaleType(ScaleType.CENTER_CROP);
        network.getFirstPhoto(0, userId, candidateProfileView);
		candidateProfileView.setPadding(paddingMargin, 0, paddingMargin, 0);
		
		ImageView candidateProfileImage = new ImageView(getActivity());
		candidateProfileImage.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, Gravity.RIGHT));
		candidateProfileImage.setScaleType(ScaleType.CENTER_CROP);
		candidateProfileImage.setImageBitmap(decodeSampledBitmapFromResource(getResources(), R.drawable.profilelight, imageSize, imageSize));
		candidateProfileImage.setPadding(paddingMargin, 0, paddingMargin, 0);
        candidateProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fragmentManager2 = getSherlockActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction2 = fragmentManager2.beginTransaction();
                ContactProfileFragment fragment2 = new ContactProfileFragment();
                Bundle args = new Bundle();
                args.putInt("ProfileId", userId);
                fragment2.setArguments(args);
                fragmentTransaction2.addToBackStack(null);
                fragmentTransaction2.replace(R.id.candidate_frame1, fragment2);
                fragmentTransaction2.commit();
            }
        });
		
        TextView textViewParty = new TextView(getActivity());
        textViewParty.setTextColor(Color.parseColor("#FFFFFF"));
        textViewParty.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 10.0f);
        textViewParty.setText(partyAbr);
        textViewParty.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        textViewParty.setGravity(Gravity.CENTER);
        textViewParty.setTypeface(null, Typeface.BOLD);

        network.getTicketLabel(textViewParty, ticketId);
		
        RectShape rect = new RectShape();
        ShapeDrawable rectShapeDrawable = new ShapeDrawable(rect);
        Paint paint = rectShapeDrawable.getPaint();
        paint.setColor(Color.parseColor(colour));
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setStrokeWidth(5);

        int imageSize2 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                (float) 27, getResources().getDisplayMetrics());
        
		LinearLayout partyLayout = new LinearLayout(getActivity());
		partyLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                imageSize, imageSize2);
        params.gravity = Gravity.CENTER_VERTICAL;
        partyLayout.setLayoutParams(params);
		//partyLayout.setBackgroundDrawable(rectShapeDrawable);
		partyLayout.addView(textViewParty);
		
        TextView textViewCandidate = new TextView(getActivity());
        textViewCandidate.setTextColor(Color.parseColor("#3A3F43"));
        textViewCandidate.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14.0f);
        textViewCandidate.setText(candidateName);
        textViewCandidate.setPadding(paddingMargin, 0, paddingMargin, 0);
        textViewCandidate.setGravity(Gravity.LEFT);
        
        TextView textViewPosition = new TextView(getActivity());
        textViewPosition.setTextColor(Color.parseColor("#3A3F43"));
        textViewPosition.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 11.0f);
        textViewPosition.setText(candidatePosition);
        textViewPosition.setPadding(paddingMargin, 0, paddingMargin, 0);
        textViewPosition.setGravity(Gravity.LEFT);

        network.getPositionText(textViewPosition, positionId);
        
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
        firstnames.add(firstName);
        lastnames.add(lastName);
        rows.add(tr);
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
