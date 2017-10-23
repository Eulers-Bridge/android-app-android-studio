package com.eulersbridge.isegoria;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
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

public class EventsFragment extends Fragment {
	private TableLayout newsTableLayout;
    private EventsFragment eventsFragment;

	private EventsDetailFragment fragment2;
    private android.support.v4.widget.SwipeRefreshLayout swipeContainerEvents;
    private Network network;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.events_fragment, container, false);
		newsTableLayout = rootView.findViewById(R.id.eventsTableLayout);
        eventsFragment = this;

		swipeContainerEvents = rootView.findViewById(R.id.swipeContainerEvents);
        swipeContainerEvents.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                eventsFragment.clearTable();
                network.getEvents(eventsFragment);
                swipeContainerEvents.setRefreshing(true);
                ( new android.os.Handler()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        swipeContainerEvents.setRefreshing(false);
                    }
                }, 7000);
            }
        });
	
        MainActivity mainActivity = (MainActivity) getActivity();
        network = mainActivity.getIsegoriaApplication().getNetwork();
        network.getEvents(this);

		return rootView;
	}

    private void clearTable() {
        newsTableLayout.removeAllViews();
    }
	
	public void addEvent(Event event) {
        addTableRow(event);
	}
	
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
			
		ImageView view = new ImageView(getActivity());
		view.setColorFilter(Color.argb(paddingMargin2, paddingMargin3, paddingMargin3, paddingMargin3));
		view.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, imageHeight));
		view.setScaleType(ScaleType.CENTER_CROP);
		network.getPictureVolley(event.getImageUrl(), view);
		
		view.setOnClickListener(new View.OnClickListener() {        
            @Override
            public void onClick(View view) {
		    		FragmentManager fragmentManager2 = getActivity().getSupportFragmentManager();
		    		FragmentTransaction fragmentTransaction2 = fragmentManager2.beginTransaction();
		    		fragment2 = new EventsDetailFragment();
		    		Bundle args = new Bundle();
		    		args.putParcelable("event", event);
		    		fragment2.setArguments(args);
		    		fragmentTransaction2.add(R.id.eventsFrameLayout, fragment2);
                    fragmentTransaction2.addToBackStack("");
		    		fragmentTransaction2.commit();
            }
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