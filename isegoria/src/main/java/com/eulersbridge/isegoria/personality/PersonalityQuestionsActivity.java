package com.eulersbridge.isegoria.personality;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.util.ui.NonSwipeableViewPager;
import com.eulersbridge.isegoria.util.ui.SimpleFragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class PersonalityQuestionsActivity extends AppCompatActivity {

    private NonSwipeableViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.personality_questions_activity);

        PersonalityViewModel viewModel = ViewModelProviders.of(this).get(PersonalityViewModel.class);

        viewModel.userSkippedQuestions.observe(this, skipped -> {
            if (skipped != null && skipped)
                finish();
        });

        viewModel.userContinuedQuestions.observe(this, continued -> {
            if (continued != null && continued)
                viewPager.setCurrentItem(1);
        });

        viewModel.userCompletedQuestions.observe(this, completed -> {
            if (completed != null && completed)
                finish();
        });

        setupViewPager();
    }

    private void setupViewPager() {
        viewPager = findViewById(R.id.personality_view_pager);

        List<Fragment> fragments = new ArrayList<>();
        fragments.add(new PersonalityPermissionFragment());
        fragments.add(new PersonalityQuestionsFragment());

        SimpleFragmentPagerAdapter viewPagerAdapter = new SimpleFragmentPagerAdapter(getSupportFragmentManager(), fragments);
        viewPager.setAdapter(viewPagerAdapter);

        viewPager.setCurrentItem(0);
    }
}