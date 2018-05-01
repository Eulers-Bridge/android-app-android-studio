package com.eulersbridge.isegoria.poll

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.support.annotation.UiThread
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import com.eulersbridge.isegoria.MainActivity
import com.eulersbridge.isegoria.R
import com.eulersbridge.isegoria.network.api.model.Poll
import com.eulersbridge.isegoria.util.extension.observe
import com.eulersbridge.isegoria.util.extension.runOnUiThread
import com.eulersbridge.isegoria.util.ui.SimpleFragmentPagerAdapter
import com.eulersbridge.isegoria.util.ui.TitledFragment
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.poll_vote_fragment.*
import java.util.*
import javax.inject.Inject

class PollsFragment : Fragment(), TitledFragment, MainActivity.TabbedFragment {

    @Inject
    internal lateinit var modelFactory: ViewModelProvider.Factory
    private lateinit var viewModel: PollsViewModel

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

    override fun onAttach(context: Context?) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
        viewModel = ViewModelProviders.of(this, modelFactory)[PollsViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.poll_vote_fragment, container, false)

        // Ensure options menu from another fragment is not carried over
        activity?.invalidateOptionsMenu()

        observe(viewModel.getPolls()) { polls ->
            if (polls != null)
                addPolls(polls)
        }

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupViewPager()
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

            setupWithViewPager(viewPager)
            addOnTabSelectedListener(onTabSelectedListener)
        }
    }

    @UiThread
    private fun updateTabs() {
        pagerAdapter.notifyDataSetChanged()

        tabLayout?.isVisible = fragments.size >= 2
    }

    override fun onPause() {
        tabLayout?.removeOnTabSelectedListener(onTabSelectedListener)
        super.onPause()
    }

    private fun addPolls(polls: List<Poll>) {
        val newFragments = polls.map {
            PollVoteFragment().apply {
                arguments = bundleOf(FRAGMENT_EXTRA_POLL to it)
            }
        }

        fragments.addAll(newFragments)

        runOnUiThread {
            updateTabs()
        }
    }
}