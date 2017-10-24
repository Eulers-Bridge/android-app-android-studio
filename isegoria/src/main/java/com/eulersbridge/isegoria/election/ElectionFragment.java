package com.eulersbridge.isegoria.election;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.eulersbridge.isegoria.R;
import com.viewpagerindicator.TabPageIndicator;

import java.util.List;
import java.util.Vector;

public class ElectionFragment extends Fragment {

	@SuppressWarnings("deprecation")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.election_fragment, container, false);

		List<Fragment> fragments = new Vector<>();
        fragments.add(Fragment.instantiate(getActivity(), ElectionOverviewFragment.class.getName()));
        fragments.add(Fragment.instantiate(getActivity(), ElectionProcessFragment.class.getName()));
        fragments.add(Fragment.instantiate(getActivity(), ElectionPositionsFragment.class.getName()));

		ViewPager mViewPager = rootView.findViewById(R.id.electionViewPager);
		ElectionPagerAdapter electionPagerAdapter = new ElectionPagerAdapter(getActivity().getSupportFragmentManager(), fragments);
		mViewPager.setAdapter(electionPagerAdapter);

		TabPageIndicator tabPageIndicator = rootView.findViewById(R.id.tabPageIndicator);
		tabPageIndicator.setViewPager(mViewPager);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			tabPageIndicator.setBackground(new ColorDrawable(Color.parseColor("#313E4D")));
		} else {
			tabPageIndicator.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#313E4D")));
		}

        return rootView;
	}
}