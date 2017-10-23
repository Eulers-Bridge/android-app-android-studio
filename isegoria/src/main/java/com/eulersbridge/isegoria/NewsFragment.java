package com.eulersbridge.isegoria;


import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
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

public class NewsFragment extends Fragment {
	private TableLayout newsTableLayout;
	
	private float dpWidth;

	private NewsFragment newsFragment;
	
	private int[] drawables = new int[14];
	private int drawableInt = 0;
	
	private int lastArticleId; 
	private int lastInstitutionId; 
	private String lastTitle;
	private String lastContent; 
	private Bitmap lastPicture;
    private String lastPictureURL;
	private String lastLikes;
	private long lastDate;
	private String lastCreatorEmail; 
	private String lastStudentYear; 
	private String lastLink;
	
	private int doubleCell = 0;
	private int articlesAdded = 0;

    private android.support.v4.widget.SwipeRefreshLayout swipeContainerNews;
    private Network network;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		DisplayMetrics displayMetrics = getActivity().getResources().getDisplayMetrics();
		View rootView = inflater.inflate(R.layout.news_fragment, container, false);
		newsTableLayout = rootView.findViewById(R.id.newsTableLayout);
        swipeContainerNews = rootView.findViewById(R.id.swipeContainerNews);
        swipeContainerNews.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                newsFragment.clearTable();
                network.getNewsArticles(newsFragment);
                swipeContainerNews.setRefreshing(true);
                ( new android.os.Handler()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        swipeContainerNews.setRefreshing(false);
                    }
                }, 7000);
            }
        });
		
		dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        this.newsFragment = this;
        
        MainActivity mainActivity = (MainActivity) getActivity();
        network = mainActivity.getIsegoriaApplication().getNetwork();
        network.getNewsArticles(this);
        
		return rootView;
	}

    private void clearTable() {
        newsTableLayout.removeAllViews();
    }
	
	public void addNewsArticle(final int articleId, final int institutionId, final String title, final String content, final String pictureURL, final String likes,
			final long date, final String creatorEmail, final String studentYear, final String link) {
		articlesAdded = articlesAdded + 1;

		try {
			getActivity().runOnUiThread(new Runnable() {
			     @Override
			     public void run() {
			    	 try {			    	  
				    	 if(doubleCell == 0) {
				    		 doubleCell = 1;
				    		 newsFragment.addTableRow(articleId, -1, pictureURL, null, false, false, title, TimeConverter.convertTimestampToString(date), "", "");
				     	 }
				    	 else if(doubleCell == 1) {
				    		 doubleCell = 2;
					    	 lastArticleId = articleId;
					    	 lastInstitutionId = institutionId;
					    	 lastTitle = title;
					    	 lastContent = content;
					    	 lastPictureURL = pictureURL;
					    	 lastLikes = likes;
					    	 lastDate = date;
					    	 lastCreatorEmail = creatorEmail;
					    	 lastStudentYear = studentYear;
					    	 lastLink = link;
				    	 }
				    	 else if(doubleCell == 2) {
				    		 doubleCell = 0;
				    		 newsFragment.addTableRow(lastArticleId, articleId, lastPictureURL, pictureURL, true, false, lastTitle, TimeConverter.convertTimestampToString(lastDate), title, "");
				    	 }
				     } catch(Exception ignored) {
				    	 
				     }
			     }
			});
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private void addTableRow(final int articleId1, final int articleId2, String drawable1, String drawable2, boolean doubleCell, boolean lastCell, String articleTitle1, String articleTime1,
							 String articleTitle2, String articleTime2) {
		TableRow tr;
		String colour = "#F8F8F8";

        int paddingMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                (float) 3.2, getResources().getDisplayMetrics());
        int paddingMargin2 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                (float) 90, getResources().getDisplayMetrics());
        int paddingMargin3 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                (float) 35, getResources().getDisplayMetrics());
        int paddingMargin4 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                (float) 83.33, getResources().getDisplayMetrics());
        int paddingMargin5 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                (float)  6.666666667, getResources().getDisplayMetrics());
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

			if(drawable1 == null) {
				colour = "#000000";
			}
			
	        TextView textViewArticle = new TextView(getActivity());
	        textViewArticle.setTextColor(Color.parseColor(colour));
	        textViewArticle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16.0f);
	        textViewArticle.setText(articleTitle1);
	        textViewArticle.setPadding(paddingMargin5, 0, paddingMargin5, 0);
	        textViewArticle.setGravity(Gravity.CENTER);
	        
	        TextView textViewArticleTime = new TextView(getActivity());
	        textViewArticleTime.setTextColor(Color.parseColor(colour));
	        textViewArticleTime.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12.0f);
	        textViewArticleTime.setText(articleTime1);
	        textViewArticleTime.setPadding(0, paddingMargin2, 0, 0);
	        textViewArticleTime.setGravity(Gravity.CENTER);
	        
	        RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);  
	        params1.addRule(RelativeLayout.CENTER_HORIZONTAL, textViewArticle.getId());
	        params1.addRule(RelativeLayout.CENTER_VERTICAL, textViewArticle.getId());
	        
	        RelativeLayout.LayoutParams params2 = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);  
	        params2.addRule(RelativeLayout.CENTER_HORIZONTAL, textViewArticle.getId());
	        params2.addRule(RelativeLayout.CENTER_VERTICAL, textViewArticle.getId());
			
			ImageView view = new ImageView(getActivity());
			view.setColorFilter(Color.argb(paddingMargin4, paddingMargin3, paddingMargin3, paddingMargin3));
			view.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT));
			view.setScaleType(ScaleType.CENTER_CROP);
			network.getPictureVolley(drawable1, view);
	        view.setOnClickListener(new View.OnClickListener() {        
	            @Override
	            public void onClick(View view) {
                        FragmentManager fragmentManager2 = getActivity().getSupportFragmentManager();
			    		FragmentTransaction fragmentTransaction2 = fragmentManager2.beginTransaction();
			    		NewsArticleFragment fragment2 = new NewsArticleFragment();
			    		Bundle args = new Bundle();
			    		args.putInt("ArticleId", articleId1);
			    		fragment2.setArguments(args);
			    		fragmentTransaction2.addToBackStack(null);
                        fragmentTransaction2.add(R.id.newsFrameLayout, fragment2);
			    		fragmentTransaction2.commit();
	            }
	         });
	        
	        relativeLayout.addView(view);
	        relativeLayout.addView(textViewArticle, params1);
	        relativeLayout.addView(textViewArticleTime, params2);
            relativeLayout.setBackgroundColor(Color.GRAY);
	        tr.addView(relativeLayout);
	        
			relativeLayout = new RelativeLayout(getActivity());
			relativeLayout.setLayoutParams(new TableRow.LayoutParams((int)(dpWidth / 2), imageHeight));
			if(lastCell)
				((ViewGroup.MarginLayoutParams) relativeLayout.getLayoutParams()).setMargins(0, paddingMargin, paddingMargin, paddingMargin);
			else
				((ViewGroup.MarginLayoutParams) relativeLayout.getLayoutParams()).setMargins(0, paddingMargin, paddingMargin, 0);
			
			colour = "#F8F8F8";
			if(drawable2 == null) {
				colour = "#000000";
			}
			
	        textViewArticle = new TextView(getActivity());
	        textViewArticle.setTextColor(Color.parseColor(colour));
	        textViewArticle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16.0f);
	        textViewArticle.setText(articleTitle2);
	        textViewArticle.setPadding(paddingMargin5, 0, paddingMargin5, 0);
	        textViewArticle.setGravity(Gravity.CENTER);
	        
	        textViewArticleTime = new TextView(getActivity());
	        textViewArticleTime.setTextColor(Color.parseColor(colour));
	        textViewArticleTime.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12.0f);
	        textViewArticleTime.setText(articleTime2);
	        textViewArticleTime.setPadding(0, paddingMargin2, 0, 0);
	        textViewArticleTime.setGravity(Gravity.CENTER);
	        
	        params1 = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);  
	        params1.addRule(RelativeLayout.CENTER_HORIZONTAL, textViewArticle.getId());
	        params1.addRule(RelativeLayout.CENTER_VERTICAL, textViewArticle.getId());
	        
	        params2 = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);  
	        params2.addRule(RelativeLayout.CENTER_HORIZONTAL, textViewArticle.getId());
	        params2.addRule(RelativeLayout.CENTER_VERTICAL, textViewArticle.getId());
			
			view = new ImageView(getActivity());
			view.setColorFilter(Color.argb(paddingMargin4, paddingMargin3, paddingMargin3, paddingMargin3));
			view.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT));
			view.setScaleType(ScaleType.CENTER_CROP);
            network.getPictureVolley(drawable2, view);
	        view.setOnClickListener(new View.OnClickListener() {        
	            @Override
	            public void onClick(View view) {
                        FragmentManager fragmentManager2 = getActivity().getSupportFragmentManager();
			    		FragmentTransaction fragmentTransaction2 = fragmentManager2.beginTransaction();
			    		NewsArticleFragment fragment2 = new NewsArticleFragment();
			    		Bundle args = new Bundle();
			    		args.putInt("ArticleId", articleId2);
			    		fragment2.setArguments(args);
			    		fragmentTransaction2.addToBackStack(null);
			    		fragmentTransaction2.add(R.id.newsFrameLayout, fragment2);
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
			network.getPictureVolley(drawable1, view);

	        view.setOnClickListener(new View.OnClickListener() {        
	            @Override
	            public void onClick(View view) {
                        FragmentManager fragmentManager2 = getActivity().getSupportFragmentManager();
			    		FragmentTransaction fragmentTransaction2 = fragmentManager2.beginTransaction();
			    		NewsArticleFragment fragment2 = new NewsArticleFragment();
			    		Bundle args = new Bundle();
			    		args.putInt("ArticleId", articleId1);
			    		fragment2.setArguments(args);
			    		fragmentTransaction2.addToBackStack(null);
                        fragmentTransaction2.add(R.id.newsFrameLayout, fragment2);
			    		fragmentTransaction2.commit();
	            }
	         });
	        
	        TextView textViewArticle = new TextView(getActivity());
	        textViewArticle.setTextColor(Color.parseColor(colour));
	        textViewArticle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20.0f);
	        textViewArticle.setText(articleTitle1);
	        textViewArticle.setGravity(Gravity.CENTER);
	        
	        TextView textViewArticleTime = new TextView(getActivity());
	        textViewArticleTime.setTextColor(Color.parseColor(colour));
	        textViewArticleTime.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12.0f);
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
}