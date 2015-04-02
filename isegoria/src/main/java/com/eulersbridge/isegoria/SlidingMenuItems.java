package com.eulersbridge.isegoria;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockFragment;

public class SlidingMenuItems extends ListFragment {
	
	SherlockFragment feedFragment = new FeedFragment();
	SherlockFragment electionFragment = new ElectionMasterFragment();
	SherlockFragment pollFragment = new PollFragment();
	SherlockFragment voteViewPagerFragment = new VoteViewPagerFragment();
    SherlockFragment findAddContactFragment = new FindAddContactFragment();
	SherlockFragment profileViewPagerFragment = new ProfileViewPagerFragment();
	SherlockFragment userSettingsFragment = new UserSettingsFragment();
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.list, null);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		String[] items = getResources().getStringArray(R.array.menu_array);
		SlidingArrayAdapter itemAdapter = new SlidingArrayAdapter(getActivity(), 
				R.layout.sliding_menu_item, android.R.id.text1, items);
		setListAdapter(itemAdapter);
	}

	@Override
	public void onListItemClick(ListView lv, View v, int position, long id) {
		SherlockFragment newContent = null;
		switch (position) {
		case 0:
			newContent = feedFragment;
			break;
		case 1:
			newContent = electionFragment;
			break;
		case 2:
			newContent = pollFragment;
			break;
		case 3:
			newContent = voteViewPagerFragment;
			break;
        case 4:
            newContent = findAddContactFragment;
            break;
		case 5:
			newContent = profileViewPagerFragment;
			break;
		case 6:
			newContent = userSettingsFragment;
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