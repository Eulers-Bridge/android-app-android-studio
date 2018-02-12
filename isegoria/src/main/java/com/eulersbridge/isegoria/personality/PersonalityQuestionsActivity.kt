package com.eulersbridge.isegoria.personality

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.eulersbridge.isegoria.R
import com.eulersbridge.isegoria.observe
import com.eulersbridge.isegoria.util.ui.NonSwipeableViewPager
import com.eulersbridge.isegoria.util.ui.SimpleFragmentPagerAdapter
import kotlinx.android.synthetic.main.personality_questions_activity.*

class PersonalityQuestionsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.personality_questions_activity)

        val viewModel = ViewModelProviders.of(this).get(PersonalityViewModel::class.java)

        observe(viewModel.userSkippedQuestions) {
            if (it == true) finish()
        }

        observe(viewModel.userContinuedQuestions) {
            if (it == true) getViewPager().currentItem = 1
        }

        observe(viewModel.userCompletedQuestions) {
            if (it == true) finish()
        }

        setupViewPager()
    }

    private fun getViewPager() = viewPager as NonSwipeableViewPager

    private fun setupViewPager() {
        val fragments = listOf(
            PersonalityPermissionFragment(),
            PersonalityQuestionsFragment()
        )

        val viewPagerAdapter = SimpleFragmentPagerAdapter(supportFragmentManager, fragments)

        getViewPager().apply {
            adapter = viewPagerAdapter
            currentItem = 0
        }
    }
}