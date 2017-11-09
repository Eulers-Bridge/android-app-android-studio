package com.eulersbridge.isegoria.feed;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
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

import com.eulersbridge.isegoria.GlideApp;
import com.eulersbridge.isegoria.Isegoria;
import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.models.Event;
import com.eulersbridge.isegoria.utilities.TintTransformation;
import com.eulersbridge.isegoria.utilities.Utils;

import org.parceler.Parcels;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EventsFragment extends Fragment {
	private TableLayout newsTableLayout;

    private android.support.v4.widget.SwipeRefreshLayout swipeContainerEvents;

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.events_fragment, container, false);

		Isegoria isegoria = (Isegoria)getActivity().getApplication();

		newsTableLayout = rootView.findViewById(R.id.eventsTableLayout);

		swipeContainerEvents = rootView.findViewById(R.id.swipeContainerEvents);
        swipeContainerEvents.setOnRefreshListener(() -> {
            clearTable();

            getEvents(isegoria);

            swipeContainerEvents.setRefreshing(true);
            new Handler().postDelayed(() -> swipeContainerEvents.setRefreshing(false), 7000);
        });

		getEvents(isegoria);

		return rootView;
	}

	private final Callback<List<Event>> callback = new Callback<List<Event>>() {
		@Override
		public void onResponse(Call<List<Event>> call, Response<List<Event>> response) {
			List<Event> events = response.body();
			addEvents(events);
		}

		@Override
		public void onFailure(Call<List<Event>> call, Throwable t) {
			t.printStackTrace();
		}
	};

	private void getEvents(Isegoria isegoria) {
		isegoria.getAPI().getEvents(isegoria.getLoggedInUser().institutionId).enqueue(callback);
	}

	private void addEvents(List<Event> events) {
		if (getActivity() != null && events != null && events.size() > 0) {
			getActivity().runOnUiThread(() -> {
				for (Event event : events) {
					addTableRow(event);
				}
			});
		}
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


		
		if (event.photos.size() == 0 || event.photos.get(0).thumbnailUrl == null) {
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

        GlideApp.with(this)
                .load(event.photos.get(0).thumbnailUrl)
                .transform(new TintTransformation())
                .into(view);
		
		view.setOnClickListener(innerView -> {

            int[] location = new int[] {0,0};
            innerView.getLocationOnScreen(location);

            Intent activityIntent = new Intent(getActivity(), EventDetailActivity.class);

            Bundle extras = new Bundle();
            extras.putParcelable("event", Parcels.wrap(event));
            activityIntent.putExtras(extras);

            //Animate with a scale-up transition between the activities
            Bundle options = ActivityOptionsCompat.makeScaleUpAnimation(innerView, location[0],
                    location[1], innerView.getWidth(), innerView.getHeight()).toBundle();

            ActivityCompat.startActivity(getContext(), activityIntent, options);
        });
	        
	    TextView textViewArticle = new TextView(getActivity());
	    textViewArticle.setTextColor(Color.parseColor(colour));
	    textViewArticle.setTextSize(TypedValue.COMPLEX_UNIT_DIP,20.0f);
	    textViewArticle.setText(event.name);
	    textViewArticle.setGravity(Gravity.CENTER);

		String eventTimeStr = Utils.convertTimestampToString(getContext(), event.date);
	        
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