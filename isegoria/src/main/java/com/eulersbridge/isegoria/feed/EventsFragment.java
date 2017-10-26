package com.eulersbridge.isegoria.feed;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.v4.app.Fragment;
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
import com.eulersbridge.isegoria.models.Event;
import com.eulersbridge.isegoria.utilities.TimeConverter;
import com.eulersbridge.isegoria.utilities.Utils;

import java.util.ArrayList;

public class EventsFragment extends Fragment {
	private TableLayout newsTableLayout;
    private EventsFragment eventsFragment;

	private EventsDetailFragment detailFragment;
    private android.support.v4.widget.SwipeRefreshLayout swipeContainerEvents;
    private Network network;

	private final Network.FetchEventsListener eventsListener = new Network.FetchEventsListener() {
		@Override
		public void onFetchSuccess(final ArrayList<Event> events) {
			getActivity().runOnUiThread(() -> {
                for (Event event : events) {
                    addTableRow(event);
                }
            });
		}

		@Override
		public void onFetchFailure(VolleyError error) {}
	};

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.events_fragment, container, false);
		newsTableLayout = rootView.findViewById(R.id.eventsTableLayout);
        eventsFragment = this;

		swipeContainerEvents = rootView.findViewById(R.id.swipeContainerEvents);
        swipeContainerEvents.setOnRefreshListener(() -> {
            eventsFragment.clearTable();

            network.getEvents(eventsListener);

            swipeContainerEvents.setRefreshing(true);
            new Handler().postDelayed(() -> swipeContainerEvents.setRefreshing(false), 7000);
        });
	
        MainActivity mainActivity = (MainActivity) getActivity();
        network = mainActivity.getIsegoriaApplication().getNetwork();
        network.getEvents(eventsListener);

		return rootView;
	}

    private void clearTable() {
        newsTableLayout.removeAllViews();
    }

	@UiThread
	private void addTableRow(final Event event) {
		TableRow tr;
		String colour = "#F8F8F8";

        int paddingMargin1 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                (float) 3.333333333, getResources().getDisplayMetrics());
        int paddingMargin2 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                (float) 83.333, getResources().getDisplayMetrics());
        int paddingMargin3 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                (float) 23.3333, getResources().getDisplayMetrics());
        int paddingMargin4 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                (float) 66.66666667, getResources().getDisplayMetrics());
        int imageHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                (float) 170, getResources().getDisplayMetrics());


		
		if(event.getImageUrl() == null) {
			colour = "#000000";
		}
		
		tr = new TableRow(getActivity());
		TableRow.LayoutParams rowParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT);
		tr.setLayoutParams(rowParams);
			
		RelativeLayout relativeLayout = new RelativeLayout(getActivity());
		relativeLayout.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, imageHeight));
		((TableRow.LayoutParams) relativeLayout.getLayoutParams()).span = 2;
		((ViewGroup.MarginLayoutParams) relativeLayout.getLayoutParams()).setMargins(paddingMargin1, paddingMargin1, paddingMargin1, 0);
			
		final ImageView view = new ImageView(getActivity());
		view.setColorFilter(Color.argb(paddingMargin2, paddingMargin3, paddingMargin3, paddingMargin3));
		view.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, imageHeight));
		view.setScaleType(ScaleType.CENTER_CROP);
		network.getPicture(event.getImageUrl(), new Network.PictureDownloadListener() {
			@Override
			public void onDownloadFinished(String url, @Nullable Bitmap bitmap) {
				Bitmap tintedBitmap = Utils.tintBitmap(bitmap, Color.argb(128, 0, 0, 0));
				view.setImageBitmap(tintedBitmap);
			}

			@Override
			public void onDownloadFailed(String url, VolleyError error) {}
		});
		
		view.setOnClickListener(view1 -> {
            detailFragment = new EventsDetailFragment();
            Bundle args = new Bundle();
            args.putParcelable("event", event);
            detailFragment.setArguments(args);

            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.eventsFrameLayout, detailFragment)
                    .addToBackStack(null)
                    .commit();
        });
	        
	    TextView textViewArticle = new TextView(getActivity());
	    textViewArticle.setTextColor(Color.parseColor(colour));
	    textViewArticle.setTextSize(TypedValue.COMPLEX_UNIT_DIP,20.0f);
	    textViewArticle.setText(event.getName());
	    textViewArticle.setGravity(Gravity.CENTER);

		String eventTimeStr = TimeConverter.convertTimestampToString(event.getDate());
	        
	    TextView textViewArticleTime = new TextView(getActivity());
	    textViewArticleTime.setTextColor(Color.parseColor(colour));
	    textViewArticleTime.setTextSize(TypedValue.COMPLEX_UNIT_DIP,12.0f);
	    textViewArticleTime.setText(eventTimeStr);
	    textViewArticleTime.setPadding(0, paddingMargin4, 0, 0);
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