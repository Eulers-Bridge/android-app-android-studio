package com.eulersbridge.isegoria;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockFragment;

public class SlidingMenuItems extends ListFragment {
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.list, null);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		String[] items = getResources().getStringArray(R.array.menu_array);
		SlidingArrayAdapter itemAdapter = new SlidingArrayAdapter(getActivity(), 
				R.layout.sliding_menu_item, R.id.text1, items);
		setListAdapter(itemAdapter);
	}

	@Override
	public void onListItemClick(ListView lv, View v, int position, long id) {
		SherlockFragment newContent = null;
		switch (position) {
		case 0:
			newContent = new FeedFragment();
			break;
		case 1:
			newContent = new ElectionMasterFragment();
			break;
		case 2:
			newContent = new PollFragment();
			break;
		case 3:
			newContent = new VoteViewPagerFragment();
			break;
        case 4:
            newContent = new FindAddContactFragment();
            break;
		case 5:
			newContent = new ProfileViewPagerFragment();
			break;
		case 6:
			newContent = new UserSettingsFragment();
			break;
		}
		if (newContent != null)
			switchFragment(newContent);
	}

	private void switchFragment(final SherlockFragment fragment) {
		if (getActivity() == null)
			return;
		
		if (getActivity() instanceof MainActivity) {
			final MainActivity fca = (MainActivity) getActivity();
			
			fca.runOnUiThread(new Runnable() {
			     @Override
			     public void run() {
			    	 fca.switchContent(fragment);
			     }
			});
		}
	}
}