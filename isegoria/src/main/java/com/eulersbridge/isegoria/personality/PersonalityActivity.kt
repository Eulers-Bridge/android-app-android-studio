package com.eulersbridge.isegoria.personality

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import com.eulersbridge.isegoria.R
import com.eulersbridge.isegoria.observe
import com.eulersbridge.isegoria.util.ui.NonSwipeableViewPager
import com.eulersbridge.isegoria.util.ui.SimpleFragmentPagerAdapter
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.android.synthetic.main.personality_questions_activity.*
import javax.inject.Inject

class PersonalityActivity : DaggerAppCompatActivity() {

    @Inject
    lateinit var modelFactory: ViewModelProvider.Factory
    private lateinit var viewModel: PersonalityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.personality_questions_activity)

        viewModel = ViewModelProviders.of(this, modelFactory)[PersonalityViewModel::class.java]

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
        val permissionFragment = PersonalityPermissionFragment()
        permissionFragment.setViewModel(viewModel)

        val questionsFragment = PersonalityQuestionsFragment()
        questionsFragment.setViewModel(viewModel)

        val fragments = listOf(
            permissionFragment,
            questionsFragment
        )

        val viewPagerAdapter = SimpleFragmentPagerAdapter(supportFragmentManager, fragments)

        getViewPager().apply {
            adapter = viewPagerAdapter
            currentItem = 0
        }
    }
}