package com.eulersbridge.isegoria;


import java.sql.Timestamp;
import java.util.Date;

import android.app.Application;
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
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
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

public class NewsFragment extends Fragment {
	private View rootView;
	private TableLayout newsTableLayout;
	
	private float dpWidth;
	private float dpHeight;
	
	private Isegoria isegoria;
	private NewsFragment newsFragment;
	
	private int[] drawables = new int[14];
	private int drawableInt = 0;
	
	private int lastArticleId; 
	private int lastInstitutionId; 
	private String lastTitle;
	private String lastContent; 
	private Bitmap lastPicture; 
	private String lastLikers;
	private long lastDate;
	private String lastCreatorEmail; 
	private String lastStudentYear; 
	private String lastLink;
	
	private int doubleCell = 0;
	private int articlesAdded = 0;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {   
		DisplayMetrics displayMetrics = getActivity().getResources().getDisplayMetrics();
		this.isegoria = (Isegoria) getActivity().getApplication();
		rootView = inflater.inflate(R.layout.news_fragment, container, false);
		newsTableLayout = (TableLayout) rootView.findViewById(R.id.newsTableLayout);
		
		dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        dpHeight = displayMetrics.heightPixels / displayMetrics.density;  
        this.newsFragment = this;
        
        MainActivity mainActivity = (MainActivity) getActivity();
        Network network = mainActivity.getIsegoriaApplication().getNetwork();
        network.getNewsArticles(this);
        
		return rootView;
	}
	
	public void addNewsArticle(final int articleId, final int institutionId, final String title, final String content, final Bitmap picture, final String likers, 
			final long date, final String creatorEmail, final String studentYear, final String link) {
		articlesAdded = articlesAdded + 1;

		try {
			getActivity().runOnUiThread(new Runnable() {
			     @Override
			     public void run() {
			    	 try {			    	  
				    	 if(doubleCell == 0) {
				    		 doubleCell = 1;
				    		 newsFragment.addTableRow(articleId, -1, picture, null, false, false, title, TimeConverter.convertTimestampToString(date), "", "");
				     	 }
				    	 else if(doubleCell == 1) {
				    		 doubleCell = 2;
					    	 lastArticleId = articleId;
					    	 lastInstitutionId = institutionId;
					    	 lastTitle = title;
					    	 lastContent = content;
					    	 lastPicture = picture;
					    	 lastLikers = likers;
					    	 lastDate = date;
					    	 lastCreatorEmail = creatorEmail;
					    	 lastStudentYear = studentYear;
					    	 lastLink = link;
				    	 }
				    	 else if(doubleCell == 2) {
				    		 doubleCell = 0;
				    		 newsFragment.addTableRow(lastArticleId, articleId, lastPicture, picture, true, false, lastTitle, TimeConverter.convertTimestampToString(lastDate), title, "");
				    	 }
				     } catch(Exception e) {
				    	 
				     }
			     }
			});
		} catch(Exception e) {
			
		}
	}
	
	public void addTableRow(final int articleId1, final int articleId2, Bitmap drawable1, Bitmap drawable2, boolean doubleCell, boolean lastCell, String articleTitle1, String articleTime1, 
			String articleTitle2, String articleTime2) {
		TableRow tr;
		String colour = "#F8F8F8";
		
		if(doubleCell) {
			tr = new TableRow(getActivity());
			TableRow.LayoutParams rowParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT);
			tr.setLayoutParams(rowParams);
			
			RelativeLayout relativeLayout = new RelativeLayout(getActivity());
			relativeLayout.setLayoutParams(new TableRow.LayoutParams((int)(dpWidth / 2), (int)(278.26)));
			if(lastCell)
				((ViewGroup.MarginLayoutParams) relativeLayout.getLayoutParams()).setMargins(5, 5, 5, 5);
			else
				((ViewGroup.MarginLayoutParams) relativeLayout.getLayoutParams()).setMargins(5, 5, 5, 0);

			if(drawable1 == null) {
				colour = "#000000";
			}
			
	        TextView textViewArticle = new TextView(getActivity());
	        textViewArticle.setTextColor(Color.parseColor(colour));
	        textViewArticle.setTextSize(16.0f);
	        textViewArticle.setText(articleTitle1);
	        textViewArticle.setPadding(10, 0, 10, 0);
	        textViewArticle.setGravity(Gravity.CENTER);
	        
	        TextView textViewArticleTime = new TextView(getActivity());
	        textViewArticleTime.setTextColor(Color.parseColor(colour));
	        textViewArticleTime.setTextSize(12.0f);
	        textViewArticleTime.setText(articleTime1);
	        textViewArticleTime.setPadding(0, 135, 0, 0);
	        textViewArticleTime.setGravity(Gravity.CENTER);
	        
	        RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);  
	        params1.addRule(RelativeLayout.CENTER_HORIZONTAL, textViewArticle.getId());
	        params1.addRule(RelativeLayout.CENTER_VERTICAL, textViewArticle.getId());
	        
	        RelativeLayout.LayoutParams params2 = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);  
	        params2.addRule(RelativeLayout.CENTER_HORIZONTAL, textViewArticle.getId());
	        params2.addRule(RelativeLayout.CENTER_VERTICAL, textViewArticle.getId());
			
			ImageView view = new ImageView(getActivity());
			view.setColorFilter(Color.argb(125, 35, 35, 35));
			view.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT));
			view.setScaleType(ScaleType.CENTER_CROP);
			view.setImageBitmap(drawable1);
	        view.setOnClickListener(new View.OnClickListener() {        
	            @Override
	            public void onClick(View view) {
			    		FragmentManager fragmentManager2 = getFragmentManager();
			    		FragmentTransaction fragmentTransaction2 = fragmentManager2.beginTransaction();
			    		NewsArticleFragment fragment2 = new NewsArticleFragment();
			    		Bundle args = new Bundle();
			    		args.putInt("ArticleId", articleId1);
			    		fragment2.setArguments(args);
			    		fragmentTransaction2.addToBackStack(null);
			    		fragmentTransaction2.replace(android.R.id.content, fragment2);
			    		fragmentTransaction2.commit();
	            }
	         });
	        
	        relativeLayout.addView(view);
	        relativeLayout.addView(textViewArticle, params1);
	        relativeLayout.addView(textViewArticleTime, params2);
	        tr.addView(relativeLayout);
	        
			relativeLayout = new RelativeLayout(getActivity());
			relativeLayout.setLayoutParams(new TableRow.LayoutParams((int)(dpWidth / 2), (int)(278.26)));
			if(lastCell)
				((ViewGroup.MarginLayoutParams) relativeLayout.getLayoutParams()).setMargins(0, 5, 5, 5);
			else
				((ViewGroup.MarginLayoutParams) relativeLayout.getLayoutParams()).setMargins(0, 5, 5, 0);
			
			colour = "#F8F8F8";
			if(drawable2 == null) {
				colour = "#000000";
			}
			
	        textViewArticle = new TextView(getActivity());
	        textViewArticle.setTextColor(Color.parseColor(colour));
	        textViewArticle.setTextSize(16.0f);
	        textViewArticle.setText(articleTitle2);
	        textViewArticle.setPadding(10, 0, 10, 0);
	        textViewArticle.setGravity(Gravity.CENTER);
	        
	        textViewArticleTime = new TextView(getActivity());
	        textViewArticleTime.setTextColor(Color.parseColor(colour));
	        textViewArticleTime.setTextSize(12.0f);
	        textViewArticleTime.setText(articleTime2);
	        textViewArticleTime.setPadding(0, 135, 0, 0);
	        textViewArticleTime.setGravity(Gravity.CENTER);
	        
	        params1 = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);  
	        params1.addRule(RelativeLayout.CENTER_HORIZONTAL, textViewArticle.getId());
	        params1.addRule(RelativeLayout.CENTER_VERTICAL, textViewArticle.getId());
	        
	        params2 = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);  
	        params2.addRule(RelativeLayout.CENTER_HORIZONTAL, textViewArticle.getId());
	        params2.addRule(RelativeLayout.CENTER_VERTICAL, textViewArticle.getId());
			
			view = new ImageView(getActivity());
			view.setColorFilter(Color.argb(125, 35, 35, 35));
			view.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT));
			view.setScaleType(ScaleType.CENTER_CROP);
	        view.setImageBitmap(drawable2);
	        view.setOnClickListener(new View.OnClickListener() {        
	            @Override
	            public void onClick(View view) {
			    		FragmentManager fragmentManager2 = getFragmentManager();
			    		FragmentTransaction fragmentTransaction2 = fragmentManager2.beginTransaction();
			    		NewsArticleFragment fragment2 = new NewsArticleFragment();
			    		Bundle args = new Bundle();
			    		args.putInt("ArticleId", articleId2);
			    		fragment2.setArguments(args);
			    		fragmentTransaction2.addToBackStack(null);
			    		fragmentTransaction2.replace(android.R.id.content, fragment2);
			    		fragmentTransaction2.commit();
	            }
	         });
	        
	        relativeLayout.addView(view);
	        relativeLayout.addView(textViewArticle, params1);
	        relativeLayout.addView(textViewArticleTime, params2);
	        tr.addView(relativeLayout);
	        
	        newsTableLayout.addView(tr);
		}
		else {
			if(drawable1 == null) {
				colour = "#000000";
			}
			
			tr = new TableRow(getActivity());
			TableRow.LayoutParams rowParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT);
			tr.setLayoutParams(rowParams);
			
			RelativeLayout relativeLayout = new RelativeLayout(getActivity());
			relativeLayout.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, (int)(278.26)));
			((TableRow.LayoutParams) relativeLayout.getLayoutParams()).span = 2;
			if(lastCell)
				((ViewGroup.MarginLayoutParams) relativeLayout.getLayoutParams()).setMargins(5, 5, 5, 5);
			else
				((ViewGroup.MarginLayoutParams) relativeLayout.getLayoutParams()).setMargins(5, 5, 5, 0);
			
			ImageView view = new ImageView(getActivity());
			view.setColorFilter(Color.argb(125, 35, 35, 35));
			view.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, (int)(dpHeight / 2.3)));
			view.setScaleType(ScaleType.CENTER_CROP);
			view.setImageBitmap(drawable1);

	        view.setOnClickListener(new View.OnClickListener() {        
	            @Override
	            public void onClick(View view) {
			    		FragmentManager fragmentManager2 = getFragmentManager();
			    		FragmentTransaction fragmentTransaction2 = fragmentManager2.beginTransaction();
			    		NewsArticleFragment fragment2 = new NewsArticleFragment();
			    		Bundle args = new Bundle();
			    		args.putInt("ArticleId", articleId1);
			    		fragment2.setArguments(args);
			    		fragmentTransaction2.addToBackStack(null);
			    		fragmentTransaction2.add(android.R.id.content, fragment2);
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