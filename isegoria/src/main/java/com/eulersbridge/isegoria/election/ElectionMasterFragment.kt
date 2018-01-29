package com.eulersbridge.isegoria.election

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.eulersbridge.isegoria.MainActivity
import com.eulersbridge.isegoria.R
import com.eulersbridge.isegoria.election.candidates.CandidateFragment
import com.eulersbridge.isegoria.election.efficacy.SelfEfficacyQuestionsFragment
import com.eulersbridge.isegoria.util.ui.TitledFragment
import kotlinx.android.synthetic.main.election_master_layout.*
import java.lang.ref.WeakReference

class ElectionMasterFragment : Fragment(), TitledFragment, MainActivity.TabbedFragment {

    private var overviewFragment: ElectionOverviewFragment? = null
    private var candidateFragment: CandidateFragment? = null

    private var weakActivity: WeakReference<AppCompatActivity>? = null
    private var tabLayout: TabLayout? = null

    private var viewModel: ElectionViewModel? = null

    private val onTabSelectedListener = object : TabLayout.OnTabSelectedListener {
        override fun onTabSelected(tab: TabLayout.Tab) {
            var subFragment: Fragment? = null

            if (tab.position == 0) {
                subFragment = overviewFragment

            } else if (tab.position == 1) {
                subFragment = candidateFragment
            }

            if (subFragment != null && activity != null)
                showTabFragment(subFragment)
        }

        override fun onTabUnselected(tab: TabLayout.Tab) {}
        override fun onTabReselected(tab: TabLayout.Tab) {}
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.election_master_layout, container, false)

        viewModel = ViewModelProviders.of(this).get(ElectionViewModel::class.java)

        weakActivity = WeakReference<AppCompatActivity>(activity as AppCompatActivity?)

        activity?.invalidateOptionsMenu()

        overviewFragment = ElectionOverviewFragment()
        candidateFragment = CandidateFragment()

        showFirstTab()

        return rootView
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

        tabLayout?.removeOnTabSelectedListener(onTabSelectedListener)
    }

    private fun showFirstTab() {
        showTabFragment(overviewFragment!!)
    }

    private fun showTabFragment(fragment: Fragment) {
        val activity = weakActivity!!.get()

        activity?.supportFragmentManager
            ?.beginTransaction()
            ?.replace(R.id.election_frame, fragment)
            ?.commitAllowingStateLoss()

        viewModel!!.userCompletedEfficacyQuestions().observe(this, Observer { completed ->
            if (completed != null && (!completed)) {
                overlayView.visibility = View.VISIBLE

                overlaySurveyButton.setOnClickListener {
                    val innerActivity = weakActivity!!.get()

                    innerActivity?.supportFragmentManager
                        ?.beginTransaction()
                        ?.addToBackStack(null)
                        ?.add(R.id.container, SelfEfficacyQuestionsFragment())
                        ?.commit()
                }
            }
        })
    }
}
