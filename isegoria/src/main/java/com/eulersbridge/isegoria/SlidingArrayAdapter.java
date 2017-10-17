package com.eulersbridge.isegoria;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

class SlidingArrayAdapter extends ArrayAdapter {
	private final Context context;
    private MainActivity mainActivity;
	
	public SlidingArrayAdapter(Context context, int resource) {
		super(context, resource);
		this.context = context;
	}

	public SlidingArrayAdapter(Context context, int resource,
			int textViewResourceId) {
		super(context, resource, textViewResourceId);
		this.context = context;

        mainActivity = (MainActivity) (context);
	}

	public SlidingArrayAdapter(FragmentActivity fragmentActivity, int resource, int text, 
			String[] items) {
		super(fragmentActivity, resource, text, items);
		this.context = fragmentActivity.getApplicationContext();

        mainActivity = ((Isegoria)fragmentActivity.getApplication()).getMainActivity();
	}
	
    public View getView(int position, View convertView, ViewGroup parent) {
        View currentView = super.getView(position, convertView, parent);
        TextView textView = currentView.findViewById(R.id.text1);
        Typeface custom_font = Typeface.createFromAsset(mainActivity.getAssets(),
                "MuseoSansRounded-300.otf");
        textView.setTypeface(custom_font);
        ImageView imageView = currentView.findViewById(R.id.profilePic);
        
        if(position == 0) {
        	imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.feed));
        }
        else if(position == 1) {
        	imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.ribbon));
        }
        else if(position == 2) {
        	imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.poll));
        }
        else if(position == 3) {
        	imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.election));
        }
        else if(position == 4) {
            imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.friends));
        }
        else if(position == 5) {
        	imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.profile));
        }   
        else if(position == 6) {
        	imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.settings));
        }   
        return currentView;
    }
}
