package com.eulersbridge.isegoria.login;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.eulersbridge.isegoria.MainActivity;
import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.utilities.NonSwipeableViewPager;
import com.eulersbridge.isegoria.utilities.SimpleFragmentPagerAdapter;

import java.util.ArrayList;

public class PersonalityQuestionsFragment extends Fragment {
    private TabLayout tabLayout;
    private NonSwipeableViewPager viewPager;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.personality_questions_fragment, container, false);

        ((MainActivity)getActivity()).setToolbarTitle(getString(R.string.section_title_personality_questions));

        setupViewPager(rootView);
        setupTabLayout();

        return rootView;
    }

    private void setupViewPager(View rootView) {
        if (rootView == null) rootView = getView();

        if (viewPager == null && rootView != null) {
            viewPager = rootView.findViewById(R.id.personalityViewPagerFragment);

            PersonalityScreen1Fragment personalityScreen1Fragment = new PersonalityScreen1Fragment();
            personalityScreen1Fragment.setViewPager(viewPager);

            ArrayList<Fragment> fragments = new ArrayList<>();

            fragments.add(personalityScreen1Fragment);
            fragments.add(new PersonalityScreen2Fragment());

            SimpleFragmentPagerAdapter viewPagerAdapter = new SimpleFragmentPagerAdapter(getChildFragmentManager(), fragments);
            viewPager.setAdapter(viewPagerAdapter);

            viewPager.setCurrentItem(0);
        }
    }

    public void setTabLayout(TabLayout tabLayout) {
        this.tabLayout = tabLayout;
    }

    private void setupTabLayout() {
        if (tabLayout == null) return;

        tabLayout.setVisibility(View.GONE);
        tabLayout.removeAllTabs();
        tabLayout.setupWithViewPager(viewPager);
    }
}