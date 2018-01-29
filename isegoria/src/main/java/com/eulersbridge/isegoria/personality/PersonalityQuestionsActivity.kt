package com.eulersbridge.isegoria.personality

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.eulersbridge.isegoria.R
import com.eulersbridge.isegoria.util.ui.NonSwipeableViewPager
import com.eulersbridge.isegoria.util.ui.SimpleFragmentPagerAdapter
import kotlinx.android.synthetic.main.personality_questions_activity.*

class PersonalityQuestionsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.personality_questions_activity)

        val viewModel = ViewModelProviders.of(this).get(PersonalityViewModel::class.java)

        viewModel.userSkippedQuestions.observe(this, Observer { skipped ->
            if (skipped == true)
                finish()
        })

        viewModel.userContinuedQuestions.observe(this, Observer { continued ->
            if (continued == true)
                getViewPager().currentItem = 1
        })

        viewModel.userCompletedQuestions.observe(this, Observer { completed ->
            if (completed == true)
                finish()
        })

        setupViewPager()
    }

    private fun getViewPager(): NonSwipeableViewPager {
        return viewPager as NonSwipeableViewPager
    }

    private fun setupViewPager() {
        val fragments = listOf(
            PersonalityPermissionFragment(),
            PersonalityQuestionsFragment()
        )

        val viewPagerAdapter = SimpleFragmentPagerAdapter(supportFragmentManager, fragments)
        getViewPager().adapter = viewPagerAdapter

        getViewPager().currentItem = 0
    }
}