package com.eulersbridge.isegoria.vote

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.eulersbridge.isegoria.MainActivity
import com.eulersbridge.isegoria.R
import com.eulersbridge.isegoria.ifTrue
import com.eulersbridge.isegoria.util.ui.SimpleFragmentPagerAdapter
import com.eulersbridge.isegoria.util.ui.TitledFragment
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.vote_view_pager_fragment.*
import java.util.*
import javax.inject.Inject

class VoteViewPagerFragment : Fragment(), TitledFragment, MainActivity.TabbedFragment {

    @Inject
    lateinit var modelFactory: ViewModelProvider.Factory
    private lateinit var viewModel: VoteViewModel

    private lateinit var tabLayout: TabLayout
    private lateinit var viewPagerAdapter: SimpleFragmentPagerAdapter

    private val onTabSelectedListener = object : TabLayout.OnTabSelectedListener {
        override fun onTabSelected(tab: TabLayout.Tab) {}
        override fun onTabUnselected(tab: TabLayout.Tab) {}
        override fun onTabReselected(tab: TabLayout.Tab) {}
    }

    override fun onAttach(context: Context?) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
        viewModel = ViewModelProviders.of(this, modelFactory)[VoteViewModel::class.java]
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.vote_view_pager_fragment, container, false)

        // Ensure options menu from another fragment is not carried over
        activity?.invalidateOptionsMenu()

        ifTrue(viewModel.locationAndDateComplete) {
            viewPager.currentItem = 1
        }

        ifTrue(viewModel.pledgeComplete) {
            viewPager.currentItem = 2
        }

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupViewPager()
    }

    override fun getTitle(context: Context?) = context?.getString(R.string.section_title_vote)

    private fun setupViewPager() {
        val fragments = ArrayList<Fragment>()

        val voteFragment = VoteFragment()
        voteFragment.setViewModel(viewModel)

        val pledgeFragment = VotePledgeFragment()
        pledgeFragment.setViewModel(viewModel)

        val doneFragment = VoteDoneFragment()
        doneFragment.setViewModel(viewModel)

        fragments.apply {
            add(voteFragment)
            add(pledgeFragment)
            add(doneFragment)
        }

        viewPagerAdapter = object : SimpleFragmentPagerAdapter(childFragmentManager, fragments) {
            override fun getPageTitle(position: Int): CharSequence? {
                val fragment = viewPagerAdapter.getItem(position)
                return if (fragment is TitledFragment) fragment.getTitle(context!!) else null
            }
        }

        viewPager.apply {
            adapter = viewPagerAdapter
            currentItem = 0
        }
    }

    override fun setupTabLayout(tabLayout: TabLayout) {
        this.tabLayout = tabLayout

        tabLayout.apply {
            removeAllTabs()
            visibility = View.VISIBLE
            setupWithViewPager(viewPager)
            addOnTabSelectedListener(onTabSelectedListener)
            isEnabled = false
        }
    }

    override fun onPause() {
        super.onPause()

        tabLayout.removeOnTabSelectedListener(onTabSelectedListener)
    }
}