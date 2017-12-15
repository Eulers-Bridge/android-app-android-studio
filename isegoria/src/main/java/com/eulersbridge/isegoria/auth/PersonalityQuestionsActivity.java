package com.eulersbridge.isegoria.auth;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.common.NonSwipeableViewPager;
import com.eulersbridge.isegoria.common.SimpleFragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class PersonalityQuestionsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.personality_questions_activity);

        setupViewPager();
    }

    private void setupViewPager() {
        NonSwipeableViewPager viewPager = findViewById(R.id.personality_view_pager);

        PersonalityScreen1Fragment personalityScreen1Fragment = new PersonalityScreen1Fragment();
        personalityScreen1Fragment.setViewPager(viewPager);

        List<Fragment> fragments = new ArrayList<>();

        fragments.add(personalityScreen1Fragment);
        fragments.add(new PersonalityScreen2Fragment());

        SimpleFragmentPagerAdapter viewPagerAdapter = new SimpleFragmentPagerAdapter(getSupportFragmentManager(), fragments);
        viewPager.setAdapter(viewPagerAdapter);

        viewPager.setCurrentItem(0);
    }
}