package com.eulersbridge.isegoria;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

public class SlidingArrayAdapter extends ArrayAdapter {
	private Context context;
	
	public SlidingArrayAdapter(Context context, int resource) {
		super(context, resource);
		this.context = context;
	}

	public SlidingArrayAdapter(Context context, int resource,
			int textViewResourceId) {
		super(context, resource, textViewResourceId);
		this.context = context;
	}

	public SlidingArrayAdapter(FragmentActivity fragmentActivity, int resource, int text, 
			String[] items) {
		super(fragmentActivity, resource, text, items);
		this.context = fragmentActivity.getApplicationContext();
	}
	
    public View getView(int position, View convertView, ViewGroup parent) {
        View currentView = super.getView(position, convertView, parent);
        ImageView imageView = (ImageView) currentView.findViewById(R.id.profilePic);
        
        if(position == 0) {
        	imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.feedblue));
        }
        else if(position == 1) {
        	imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.electionblue));
        }
        else if(position == 2) {
        	imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.pollblue));
        }
        else if(position == 3) {
        	imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.voteblue));
        }
        else if(position == 4) {
            imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.profileblue));
        }
        else if(position == 5) {
        	imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.profileblue));
        }   
        else if(position == 6) {
        	imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.settings));
        }   
        return currentView;
    }
}
