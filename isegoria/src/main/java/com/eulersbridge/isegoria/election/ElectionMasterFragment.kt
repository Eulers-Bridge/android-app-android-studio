package com.eulersbridge.isegoria.election

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.eulersbridge.isegoria.MainActivity
import com.eulersbridge.isegoria.R
import com.eulersbridge.isegoria.election.candidates.CandidateFragment
import com.eulersbridge.isegoria.election.efficacy.SelfEfficacyQuestionsFragment
import com.eulersbridge.isegoria.util.extension.ifTrue
import com.eulersbridge.isegoria.util.extension.observeBoolean
import com.eulersbridge.isegoria.util.ui.TitledFragment
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.election_master_layout.*
import javax.inject.Inject

class ElectionMasterFragment : Fragment(), TitledFragment, MainActivity.TabbedFragment {

    @Inject
    lateinit var modelFactory: ViewModelProvider.Factory
    private lateinit var viewModel: ElectionViewModel

    private val tabFragments = listOf(ElectionOverviewFragment(), CandidateFragment())
    private lateinit var tabLayout: TabLayout

    private val onTabSelectedListener = object : TabLayout.OnTabSelectedListener {
        override fun onTabSelected(tab: TabLayout.Tab) {
            showTabFragment(tabFragments[tab.position])
        }

        override fun onTabUnselected(tab: TabLayout.Tab) {}
        override fun onTabReselected(tab: TabLayout.Tab) {}
    }

    override fun onAttach(context: Context?) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
        viewModel = ViewModelProviders.of(this, modelFactory)[ElectionViewModel::class.java]
        createViewModelObserver()
    }

    private fun createViewModelObserver() {
        observeBoolean(viewModel.surveyPromptVisible) {
            if (it) {
                surveyPrompt.isVisible = true
            }
        }

        ifTrue(viewModel.surveyVisible) {
            activity!!.supportFragmentManager
                    .beginTransaction()
                    .add(R.id.container, SelfEfficacyQuestionsFragment())
                    .commit()
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.election_master_layout, container, false)

        activity?.invalidateOptionsMenu()

        showTabFragment(tabFragments[0])

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        overlaySurveyButton.setOnClickListener { viewModel.onSurveyPromptNext() }
    }

    override fun getTitle(context: Context?): String?
            = context?.getString(R.string.section_title_election)

    override fun setupTabLayout(tabLayout: TabLayout) {
        this.tabLayout = tabLayout

        tabLayout.apply {
            removeAllTabs()
            visibility = View.VISIBLE

            val tabNames = arrayOf(
                getString(R.string.election_section_title_overview),
                getString(R.string.election_section_title_candidates)
            )

            for (tabName in tabNames)
                addTab(tabLayout.newTab().setText(tabName))

            addOnTabSelectedListener(onTabSelectedListener)
        }
    }

    override fun onPause() {
        super.onPause()

        tabLayout.removeOnTabSelectedListener(onTabSelectedListener)
    }

    private fun showTabFragment(fragment: Fragment) {
        activity?.supportFragmentManager
            ?.beginTransaction()
            ?.replace(R.id.election_frame, fragment)
            ?.commitAllowingStateLoss()
    }
}
