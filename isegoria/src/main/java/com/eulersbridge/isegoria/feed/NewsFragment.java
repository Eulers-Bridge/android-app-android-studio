package com.eulersbridge.isegoria.feed;


import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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

import com.android.volley.VolleyError;
import com.eulersbridge.isegoria.MainActivity;
import com.eulersbridge.isegoria.Network;
import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.models.NewsArticle;
import com.eulersbridge.isegoria.utilities.TimeConverter;
import com.eulersbridge.isegoria.utilities.Utils;

import java.util.ArrayList;

public class NewsFragment extends Fragment {
	private TableLayout newsTableLayout;
	
	private float dpWidth;
	
	private int doubleCell = 0;

    private android.support.v4.widget.SwipeRefreshLayout swipeContainerNews;
    private Network network;

    private final Network.NewsArticlesListener listener = new Network.NewsArticlesListener() {
		@Override
		public void onFetchSuccess(ArrayList<NewsArticle> articles) {
			setNewsArticles(articles);
		}

		@Override
		public void onFetchFailure(Exception e) { }
	};

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		DisplayMetrics displayMetrics = getActivity().getResources().getDisplayMetrics();
		View rootView = inflater.inflate(R.layout.news_fragment, container, false);
		newsTableLayout = rootView.findViewById(R.id.newsTableLayout);

        swipeContainerNews = rootView.findViewById(R.id.swipeContainerNews);
		swipeContainerNews.setColorSchemeResources(R.color.lightBlue);
        swipeContainerNews.setOnRefreshListener(() -> {
            swipeContainerNews.setRefreshing(true);

            NewsFragment.this.clearTable();
            network.getNewsArticles(listener);

            (new android.os.Handler()).postDelayed(() -> swipeContainerNews.setRefreshing(false), 7000);
        });
		
		dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        
        MainActivity mainActivity = (MainActivity) getActivity();
        network = mainActivity.getIsegoriaApplication().getNetwork();
        network.getNewsArticles(listener);
        
		return rootView;
	}

    private void clearTable() {
        newsTableLayout.removeAllViews();
    }

    private void setNewsArticles(final ArrayList<NewsArticle> articles) {
		Activity activity = getActivity();
		if (activity != null) {
			activity.runOnUiThread(() -> {

				NewsArticle lastArticle = null;

				for (NewsArticle article : articles) {
					if (doubleCell == 0) {
						doubleCell = 1;
						addTableRow(article, null, article.getPhotoURL(), null, false);

					} else if(doubleCell == 1) {
						doubleCell = 2;
						lastArticle = article;

					} else if (doubleCell == 2) {
						doubleCell = 0;
						NewsFragment.this.addTableRow(lastArticle, article, lastArticle.getPhotoURL(), article.getPhotoURL(), true);
					}
				}
			});
		}
	}
	
	private void addTableRow(final NewsArticle article1, final @Nullable NewsArticle article2, String drawable1, String drawable2, boolean doubleCell) {
		TableRow tableRow = new TableRow(getContext());
		TableRow.LayoutParams rowParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT);
		tableRow.setLayoutParams(rowParams);

		String colour = "#F8F8F8";

        int paddingMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                (float) 4, getResources().getDisplayMetrics());
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
			RelativeLayout relativeLayout = new RelativeLayout(getContext());
			relativeLayout.setLayoutParams(new TableRow.LayoutParams((int)(dpWidth / 2), imageHeight));
			((ViewGroup.MarginLayoutParams) relativeLayout.getLayoutParams()).setMargins(paddingMargin, paddingMargin, paddingMargin, paddingMargin);

			if(drawable1 == null) {
				colour = "#000000";
			}
			
	        TextView titleTextView = new TextView(getContext());
			titleTextView.setTextColor(Color.parseColor(colour));
			titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16.0f);
			titleTextView.setText(article1.getTitle());
			titleTextView.setPadding(paddingMargin5, 0, paddingMargin5, 0);
			titleTextView.setGravity(Gravity.CENTER);
	        
	        TextView titleTextViewTime = new TextView(getContext());
	        titleTextViewTime.setTextColor(Color.parseColor(colour));
	        titleTextViewTime.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12.0f);
	        titleTextViewTime.setText(TimeConverter.convertTimestampToString(article1.getDateTimestamp()));
	        titleTextViewTime.setPadding(0, paddingMargin2, 0, 0);
	        titleTextViewTime.setGravity(Gravity.CENTER);
	        
	        RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);  
	        params1.addRule(RelativeLayout.CENTER_HORIZONTAL, titleTextView.getId());
	        params1.addRule(RelativeLayout.CENTER_VERTICAL, titleTextView.getId());
	        
	        RelativeLayout.LayoutParams params2 = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);  
	        params2.addRule(RelativeLayout.CENTER_HORIZONTAL, titleTextView.getId());
	        params2.addRule(RelativeLayout.CENTER_VERTICAL, titleTextView.getId());
			
			final ImageView view = new ImageView(getContext());
			view.setColorFilter(Color.argb(paddingMargin4, paddingMargin3, paddingMargin3, paddingMargin3));
			view.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT));
			view.setScaleType(ScaleType.CENTER_CROP);
			network.getPicture(drawable1, new Network.PictureDownloadListener() {
				@Override
				public void onDownloadFinished(String url, @Nullable Bitmap bitmap) {
					Bitmap tintedBitmap = Utils.tintBitmap(bitmap, Color.argb(128, 0, 0, 0));
					view.setImageBitmap(tintedBitmap);
				}

				@Override
				public void onDownloadFailed(String url, VolleyError error) {}
			});
	        view.setOnClickListener(view13 -> {
				NewsArticleFragment detailFragment = new NewsArticleFragment();
				Bundle args = new Bundle();
				args.putParcelable("article", article1);
				detailFragment.setArguments(args);

				getActivity().getSupportFragmentManager()
						.beginTransaction()
						.addToBackStack(null)
						.add(R.id.newsFrameLayout, detailFragment)
						.commit();
            });
	        
	        relativeLayout.addView(view);
	        relativeLayout.addView(titleTextView, params1);
	        relativeLayout.addView(titleTextViewTime, params2);
            relativeLayout.setBackgroundColor(Color.GRAY);
			tableRow.addView(relativeLayout);
	        
			relativeLayout = new RelativeLayout(getContext());
			relativeLayout.setLayoutParams(new TableRow.LayoutParams((int)(dpWidth / 2), imageHeight));
			((ViewGroup.MarginLayoutParams) relativeLayout.getLayoutParams()).setMargins(0, paddingMargin, paddingMargin, paddingMargin);
			
			colour = "#F8F8F8";
			if(drawable2 == null) {
				colour = "#000000";
			}
			
	        titleTextView = new TextView(getContext());
	        titleTextView.setTextColor(Color.parseColor(colour));
	        titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16.0f);
	        titleTextView.setText(article2.getTitle());
	        titleTextView.setPadding(paddingMargin5, 0, paddingMargin5, 0);
	        titleTextView.setGravity(Gravity.CENTER);
	        
	        titleTextViewTime = new TextView(getContext());
	        titleTextViewTime.setTextColor(Color.parseColor(colour));
	        titleTextViewTime.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12.0f);
	        titleTextViewTime.setText("");
	        titleTextViewTime.setPadding(0, paddingMargin2, 0, 0);
	        titleTextViewTime.setGravity(Gravity.CENTER);
	        
	        params1 = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);  
	        params1.addRule(RelativeLayout.CENTER_HORIZONTAL, titleTextView.getId());
	        params1.addRule(RelativeLayout.CENTER_VERTICAL, titleTextView.getId());
	        
	        params2 = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);  
	        params2.addRule(RelativeLayout.CENTER_HORIZONTAL, titleTextView.getId());
	        params2.addRule(RelativeLayout.CENTER_VERTICAL, titleTextView.getId());
			
			ImageView view2 = new ImageView(getContext());
			view2.setColorFilter(Color.argb(paddingMargin4, paddingMargin3, paddingMargin3, paddingMargin3));
			view2.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT));
			view2.setScaleType(ScaleType.CENTER_CROP);
            network.getPicture(drawable2, new Network.PictureDownloadListener() {
				@Override
				public void onDownloadFinished(String url, @Nullable Bitmap bitmap) {
					Bitmap tintedBitmap = Utils.tintBitmap(bitmap, Color.argb(128, 0, 0, 0));
					view2.setImageBitmap(tintedBitmap);
				}

				@Override
				public void onDownloadFailed(String url, VolleyError error) {}
			});
			view2.setOnClickListener(view12 -> {
				NewsArticleFragment detailFragment = new NewsArticleFragment();
				Bundle args = new Bundle();
				args.putParcelable("article", article2);
				detailFragment.setArguments(args);

				getActivity().getSupportFragmentManager()
						.beginTransaction()
						.addToBackStack(null)
						.add(R.id.newsFrameLayout, detailFragment)
						.commit();
            });
	        
	        relativeLayout.addView(view2);
	        relativeLayout.addView(titleTextView, params1);
	        relativeLayout.addView(titleTextViewTime, params2);
			tableRow.addView(relativeLayout);
	        
	        newsTableLayout.addView(tableRow);
		}
		else {
			if(drawable1 == null) {
				colour = "#000000";
			}
			
			RelativeLayout relativeLayout = new RelativeLayout(getContext());
			relativeLayout.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, imageHeight));
			((TableRow.LayoutParams) relativeLayout.getLayoutParams()).span = 2;
			((ViewGroup.MarginLayoutParams) relativeLayout.getLayoutParams()).setMargins(paddingMargin, paddingMargin, paddingMargin, paddingMargin);
			
			ImageView imageView = new ImageView(getContext());
//			imageView.setColorFilter(Color.argb(paddingMargin4, paddingMargin3, paddingMargin3, paddingMargin3));
			imageView.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, imageHeight));
			imageView.setScaleType(ScaleType.CENTER_CROP);
			network.getPicture(drawable1, new Network.PictureDownloadListener() {
				@Override
				public void onDownloadFinished(String url, @Nullable Bitmap bitmap) {
					Bitmap tintedBitmap = Utils.tintBitmap(bitmap, Color.argb(128, 0, 0, 0));
					imageView.setImageBitmap(tintedBitmap);
				}

				@Override
				public void onDownloadFailed(String url, VolleyError error) {}
			});

			imageView.setOnClickListener(view1 -> {
				NewsArticleFragment detailFragment = new NewsArticleFragment();
				Bundle args = new Bundle();
				args.putParcelable("article", article1);
				detailFragment.setArguments(args);

				getActivity().getSupportFragmentManager()
						.beginTransaction()
						.addToBackStack(null)
						.add(R.id.newsFrameLayout, detailFragment)
						.commit();
            });
	        
	        TextView titleTextView = new TextView(getContext());
	        titleTextView.setTextColor(Color.parseColor(colour));
	        titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20.0f);
	        titleTextView.setText(article1.getTitle());
	        titleTextView.setGravity(Gravity.CENTER);
	        
	        TextView titleTextViewTime = new TextView(getContext());
	        titleTextViewTime.setTextColor(Color.parseColor(colour));
	        titleTextViewTime.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12.0f);
	        titleTextViewTime.setText(TimeConverter.convertTimestampToString(article1.getDateTimestamp()));
	        titleTextViewTime.setPadding(0, 100, 0, 0);
	        titleTextViewTime.setGravity(Gravity.CENTER);
	        
	        RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);  
	        params1.addRule(RelativeLayout.CENTER_HORIZONTAL, titleTextView.getId());
	        params1.addRule(RelativeLayout.CENTER_VERTICAL, titleTextView.getId());
	        
	        RelativeLayout.LayoutParams params2 = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);  
	        params2.addRule(RelativeLayout.CENTER_HORIZONTAL, titleTextView.getId());
	        params2.addRule(RelativeLayout.CENTER_VERTICAL, titleTextView.getId());
	        
	        relativeLayout.addView(imageView);
	        relativeLayout.addView(titleTextView, params1);
	        relativeLayout.addView(titleTextViewTime, params2);

			tableRow.addView(relativeLayout);
	        newsTableLayout.addView(tableRow);
		}
	}
}