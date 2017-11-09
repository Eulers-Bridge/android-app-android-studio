package com.eulersbridge.isegoria.feed;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TableRow.LayoutParams;
import android.widget.TextView;

import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.eulersbridge.isegoria.GlideApp;
import com.eulersbridge.isegoria.MainActivity;
import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.models.NewsArticle;

import com.eulersbridge.isegoria.utilities.TintTransformation;
import com.eulersbridge.isegoria.utilities.Utils;

import org.parceler.Parcels;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NewsFragment extends Fragment {
	private TableLayout newsTableLayout;
	
	private float dpWidth;
	
	private int doubleCell = 0;

    private android.support.v4.widget.SwipeRefreshLayout swipeContainerNews;

	private final Callback<List<NewsArticle>> callback = new Callback<List<NewsArticle>>() {
		@Override
		public void onResponse(Call<List<NewsArticle>> call, Response<List<NewsArticle>> response) {
			if (response.isSuccessful()) {
				List<NewsArticle> articles = response.body();

				if (articles != null) {
					setNewsArticles(articles);
				}
			}
		}

		@Override
		public void onFailure(Call<List<NewsArticle>> call, Throwable t) {
			t.printStackTrace();
		}
	};

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		DisplayMetrics displayMetrics = getActivity().getResources().getDisplayMetrics();
		View rootView = inflater.inflate(R.layout.news_fragment, container, false);
		newsTableLayout = rootView.findViewById(R.id.newsTableLayout);

		MainActivity mainActivity = (MainActivity) getActivity();

		long institutionId = mainActivity.getIsegoriaApplication().getLoggedInUser().institutionId;

        swipeContainerNews = rootView.findViewById(R.id.swipeContainerNews);
		swipeContainerNews.setColorSchemeResources(R.color.lightBlue);
        swipeContainerNews.setOnRefreshListener(() -> {
            swipeContainerNews.setRefreshing(true);

            NewsFragment.this.clearTable();
			mainActivity.getIsegoriaApplication().getAPI().getNewsArticles(institutionId).enqueue(callback);

            (new android.os.Handler()).postDelayed(() -> swipeContainerNews.setRefreshing(false), 7000);
        });
		
		dpWidth = displayMetrics.widthPixels / displayMetrics.density;

		mainActivity.getIsegoriaApplication().getAPI().getNewsArticles(institutionId).enqueue(callback);
        
		return rootView;
	}

    private void clearTable() {
        newsTableLayout.removeAllViews();
    }

    private void setNewsArticles(List<NewsArticle> articles) {
		Activity activity = getActivity();
		if (activity != null) {
			activity.runOnUiThread(() -> {

				NewsArticle lastArticle = null;

				for (NewsArticle article : articles) {
					if (doubleCell == 0) {
						doubleCell = 1;
						addTableRow(article, null, article.photos.get(0).thumbnailUrl, null, false);

					} else if(doubleCell == 1) {
						doubleCell = 2;
						lastArticle = article;

					} else if (doubleCell == 2) {
						doubleCell = 0;
						addTableRow(lastArticle, article, lastArticle.photos.get(0).thumbnailUrl, article.photos.get(0).thumbnailUrl, true);
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
			titleTextView.setText(article1.title);
			titleTextView.setPadding(paddingMargin5, 0, paddingMargin5, 0);
			titleTextView.setGravity(Gravity.CENTER);
	        
	        TextView titleTextViewTime = new TextView(getContext());
	        titleTextViewTime.setTextColor(Color.parseColor(colour));
	        titleTextViewTime.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12.0f);
	        titleTextViewTime.setText(Utils.convertTimestampToString(getContext(), article1.dateTimestamp));
	        titleTextViewTime.setPadding(0, paddingMargin2, 0, 0);
	        titleTextViewTime.setGravity(Gravity.CENTER);
	        
	        RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);  
	        params1.addRule(RelativeLayout.CENTER_HORIZONTAL, titleTextView.getId());
	        params1.addRule(RelativeLayout.CENTER_VERTICAL, titleTextView.getId());
	        
	        RelativeLayout.LayoutParams params2 = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);  
	        params2.addRule(RelativeLayout.CENTER_HORIZONTAL, titleTextView.getId());
	        params2.addRule(RelativeLayout.CENTER_VERTICAL, titleTextView.getId());
			
			final ImageView view = new ImageView(getContext());
			view.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT));

			GlideApp.with(this)
					.load(drawable1)
					.transforms(new CenterCrop(), new TintTransformation())
					.into(view);

	        view.setOnClickListener(innerView -> {
                int[] location = new int[] {0,0};
                innerView.getLocationOnScreen(location);

                Intent activityIntent = new Intent(getActivity(), NewsDetailActivity.class);

                Bundle extras = new Bundle();
                extras.putParcelable("article", Parcels.wrap(article1));
                activityIntent.putExtras(extras);

                //Animate with a scale-up transition between the activities
                Bundle options = ActivityOptionsCompat.makeScaleUpAnimation(tableRow, location[0],
                        location[1], innerView.getWidth(), innerView.getHeight()).toBundle();

                ActivityCompat.startActivity(getContext(), activityIntent, options);
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
	        titleTextView.setText(article2.title);
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
			view2.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT));

			GlideApp.with(this)
					.load(drawable2)
					.transforms(new CenterCrop(), new TintTransformation())
					.into(view2);

			view2.setOnClickListener(innerView -> {
                int[] location = new int[] {0,0};
                innerView.getLocationOnScreen(location);

                Intent activityIntent = new Intent(getActivity(), NewsDetailActivity.class);

                Bundle extras = new Bundle();
                extras.putParcelable("article", Parcels.wrap(article2));

                activityIntent.putExtras(extras);

                //Animate with a scale-up transition between the activities
                Bundle options = ActivityOptionsCompat.makeScaleUpAnimation(tableRow, location[0], location[1],
                        innerView.getWidth(),innerView.getHeight()).toBundle();

                ActivityCompat.startActivity(getContext(), activityIntent, options);
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
			imageView.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, imageHeight));

			GlideApp.with(this)
					.load(drawable1)
					.transforms(new CenterCrop(), new TintTransformation())
					.into(imageView);

			imageView.setOnClickListener(innerView -> {
                int[] location = new int[] {0,0};
                innerView.getLocationOnScreen(location);

                Intent activityIntent = new Intent(getActivity(), NewsDetailActivity.class);

                Bundle extras = new Bundle();
                extras.putParcelable("article", Parcels.wrap(article1));
                activityIntent.putExtras(extras);

                //Animate with a scale-up transition between the activities
                Bundle options = ActivityOptionsCompat.makeScaleUpAnimation(tableRow, location[0],
                        location[1], innerView.getWidth(), innerView.getHeight()).toBundle();

                ActivityCompat.startActivity(getContext(), activityIntent, options);
            });
	        
	        TextView titleTextView = new TextView(getContext());
	        titleTextView.setTextColor(Color.parseColor(colour));
	        titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20.0f);
	        titleTextView.setText(article1.title);
	        titleTextView.setGravity(Gravity.CENTER);
	        
	        TextView titleTextViewTime = new TextView(getContext());
	        titleTextViewTime.setTextColor(Color.parseColor(colour));
	        titleTextViewTime.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12.0f);
	        titleTextViewTime.setText(Utils.convertTimestampToString(getContext(), article1.dateTimestamp));
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