package com.eulersbridge.isegoria.feed

import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.view.*
import com.eulersbridge.isegoria.*
import com.eulersbridge.isegoria.util.ui.TitledFragment
import kotlinx.android.synthetic.main.feed_fragment.*


class FeedFragment : Fragment(), TitledFragment, MainActivity.TabbedFragment {

    private var tabLayout: TabLayout? = null

    private val viewModel: FeedViewModel by lazy {
        ViewModelProviders.of(activity!!).get(FeedViewModel::class.java)
    }

    private val onTabSelectedListener = object : TabLayout.OnTabSelectedListener {
        override fun onTabSelected(tab: TabLayout.Tab) {
            viewPager?.currentItem = tab.position
        }

        override fun onTabUnselected(tab: TabLayout.Tab) {}
        override fun onTabReselected(tab: TabLayout.Tab) {}
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.feed_fragment, container, false)

        activity?.apply {
            val colour = ContextCompat.getColor(this, R.color.darkBlue)
            statusBarColour = colour
            setMultitaskColour(colour)

            // Ensure options menu from another fragment is not carried over
            invalidateOptionsMenu()

            // Hide keyboard if navigating from login
            keyboardVisible = false
        }

        setHasOptionsMenu(true)

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupViewPager()
    }

    override fun getTitle(context: Context?) = context?.getString(R.string.section_title_feed)

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater!!.inflate(R.menu.feed, menu)
        super.onCreateOptionsMenu(menu, inflater)

        val friendsItem = menu!!.findItem(R.id.feed_menu_item_friends)
        friendsItem.icon?.let {
            it.mutate()
            it.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item!!.itemId) {
            R.id.feed_menu_item_friends -> {
                viewModel.showFriends()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupViewPager() {
        viewPager.apply {
            adapter = FeedViewPagerAdapter(childFragmentManager)
            offscreenPageLimit = 3
            currentItem = 0
        }

        tabLayout?.setupWithViewPager(viewPager)
    }

    override fun setupTabLayout(tabLayout: TabLayout) {
        this.tabLayout = tabLayout

        tabLayout.apply {
            tabLayout.removeAllTabs()
            tabLayout.visibility = View.VISIBLE
            tabLayout.addOnTabSelectedListener(onTabSelectedListener)

            if (viewPager != null)
                setupWithViewPager(viewPager)
        }
    }

    override fun onPause() {
        super.onPause()

        tabLayout?.removeOnTabSelectedListener(onTabSelectedListener)
    }

    override fun onDestroy() {
        super.onDestroy()

        tabLayout?.removeOnTabSelectedListener(onTabSelectedListener)
    }
}