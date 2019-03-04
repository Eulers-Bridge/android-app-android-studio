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
import androidx.core.view.isGone
import com.eulersbridge.isegoria.R
import com.eulersbridge.isegoria.util.extension.observe
import com.eulersbridge.isegoria.util.ui.SimpleFragmentPagerAdapter
import com.eulersbridge.isegoria.util.ui.TabbedFragment
import com.eulersbridge.isegoria.util.ui.TitledFragment
import com.eulersbridge.isegoria.vote.pages.VoteDoneFragment
import com.eulersbridge.isegoria.vote.pages.VoteFragment
import com.eulersbridge.isegoria.vote.pages.VotePledgeFragment
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.vote_view_pager_fragment.*
import java.util.*
import javax.inject.Inject

class VoteViewPagerFragment : Fragment(), TitledFragment, TabbedFragment {

    @Inject
    internal lateinit var modelFactory: ViewModelProvider.Factory
    private lateinit var viewModel: VoteViewModel

    private lateinit var tabLayout: TabLayout
    private lateinit var viewPagerAdapter: SimpleFragmentPagerAdapter

    override fun onAttach(context: Context?) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
        viewModel = ViewModelProviders.of(this, modelFactory)[VoteViewModel::class.java]
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.vote_view_pager_fragment, container, false)

        // Ensure options menu from another fragment is not carried over
        activity?.invalidateOptionsMenu()

        observe(viewModel.pageIndex) {
            viewPager.currentItem = it?.value ?: 0
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
            isGone = true
        }
    }
}