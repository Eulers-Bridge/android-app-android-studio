package com.eulersbridge.isegoria.personality

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import com.eulersbridge.isegoria.R
import com.eulersbridge.isegoria.util.extension.ifTrue
import com.eulersbridge.isegoria.util.extension.observe
import com.eulersbridge.isegoria.util.ui.SimpleFragmentPagerAdapter
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.android.synthetic.main.personality_questions_activity.*
import javax.inject.Inject

class PersonalityActivity : DaggerAppCompatActivity() {

    @Inject
    internal lateinit var modelFactory: ViewModelProvider.Factory
    private lateinit var viewModel: PersonalityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.personality_questions_activity)

        viewModel = ViewModelProviders.of(this, modelFactory)[PersonalityViewModel::class.java]

        ifTrue(viewModel.questionsContinued) {
            viewPager.currentItem = 1
        }

        observe(viewModel.questionsComplete) { finish() }

        setupViewPager()
    }

    override fun onBackPressed() {
        // Disable back to exit personality screen
    }

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

        viewPager.apply {
            adapter = viewPagerAdapter
            currentItem = 0
        }
    }
}