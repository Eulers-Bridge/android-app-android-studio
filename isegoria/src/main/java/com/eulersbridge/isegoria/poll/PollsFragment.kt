package com.eulersbridge.isegoria.poll

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.support.annotation.UiThread
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.eulersbridge.isegoria.ACTIVITY_EXTRA_POLL
import com.eulersbridge.isegoria.MainActivity
import com.eulersbridge.isegoria.R
import com.eulersbridge.isegoria.network.api.models.Poll
import com.eulersbridge.isegoria.util.ui.SimpleFragmentPagerAdapter
import com.eulersbridge.isegoria.util.ui.TitledFragment
import kotlinx.android.synthetic.main.poll_vote_fragment.*
import java.util.*

class PollsFragment : Fragment(), TitledFragment, MainActivity.TabbedFragment {

    private var tabLayout: TabLayout? = null
    private lateinit var pagerAdapter: SimpleFragmentPagerAdapter
    private var fragments: MutableList<Fragment> = Vector()

    private val onTabSelectedListener = object : TabLayout.OnTabSelectedListener {
        override fun onTabSelected(tab: TabLayout.Tab) {
            viewPager.currentItem = tab.position
        }

        override fun onTabUnselected(tab: TabLayout.Tab) {}
        override fun onTabReselected(tab: TabLayout.Tab) {}
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.poll_vote_fragment, container, false)

        // Ensure options menu from another fragment is not carried over
        activity?.invalidateOptionsMenu()

        val viewModel = ViewModelProviders.of(this).get(PollsViewModel::class.java)
        viewModel.getPolls().observe(this, Observer { polls ->
            if (polls != null)
                addPolls(polls)
        })

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupViewPager()
        setupTabLayout()
    }

    override fun getTitle(context: Context?) = context?.getString(R.string.section_title_poll)

    private fun setupViewPager() {
        pagerAdapter = object : SimpleFragmentPagerAdapter(childFragmentManager, fragments) {
            override fun getPageTitle(position: Int)
                    = getString(R.string.poll_title, position + 1)
        }

        viewPager.apply {
            adapter = pagerAdapter
            currentItem = 0
        }
    }

    override fun setupTabLayout(tabLayout: TabLayout) {
        this.tabLayout = tabLayout

        tabLayout.apply {
            removeAllTabs()
            visibility = View.VISIBLE
        }
    }

    @UiThread
    private fun updateTabs() {
        pagerAdapter.notifyDataSetChanged()

        tabLayout?.visibility = if (fragments.size < 2) View.GONE else View.VISIBLE
    }

    override fun onPause() {
        super.onPause()

        tabLayout?.removeOnTabSelectedListener(onTabSelectedListener)
    }

    private fun setupTabLayout() {
        tabLayout?.apply {
            setupWithViewPager(viewPager)
            addOnTabSelectedListener(onTabSelectedListener)
        }
    }

    private fun addPolls(polls: List<Poll>) {
        val fragments = polls.map {
            val args = Bundle()
            args.putParcelable(ACTIVITY_EXTRA_POLL, it)

            val pollVoteFragment = PollVoteFragment()
            pollVoteFragment.arguments = args

            pollVoteFragment
        }

        this.fragments.addAll(fragments)

        activity?.runOnUiThread {
            updateTabs()
        }
    }
}