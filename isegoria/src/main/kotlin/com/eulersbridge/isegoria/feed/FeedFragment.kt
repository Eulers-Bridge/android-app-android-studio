package com.eulersbridge.isegoria.feed

import android.app.Activity
import android.app.ActivityManager
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import android.support.annotation.ColorInt
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.view.*
import androidx.core.graphics.toColorFilter
import androidx.core.view.isVisible
import com.eulersbridge.isegoria.MainActivity
import com.eulersbridge.isegoria.R
import com.eulersbridge.isegoria.util.extension.setKeyboardVisible
import com.eulersbridge.isegoria.util.ui.TitledFragment
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.feed_fragment.*
import javax.inject.Inject


class FeedFragment : Fragment(), TitledFragment, MainActivity.TabbedFragment {

    private var tabLayout: TabLayout? = null

    @Inject
    internal lateinit var modelFactory: ViewModelProvider.Factory
    private lateinit var viewModel: FeedViewModel

    override fun onAttach(context: Context?) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
        viewModel = ViewModelProviders.of(this, modelFactory)[FeedViewModel::class.java]
    }

    private val onTabSelectedListener = object : TabLayout.OnTabSelectedListener {
        override fun onTabSelected(tab: TabLayout.Tab) {
            viewPager?.currentItem = tab.position
        }

        override fun onTabUnselected(tab: TabLayout.Tab) {}
        override fun onTabReselected(tab: TabLayout.Tab) {}
    }

    private fun Activity.setStatusBarColour(colour: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            window.statusBarColor = colour
    }

    private fun Activity.setMultitaskColour(@ColorInt colour: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            setMultitaskDescription(this, getString(R.string.app_name), colour)
    }

    private fun setMultitaskDescription(activity: Activity, title: String, @ColorInt colour: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val icon = BitmapFactory.decodeResource(activity.resources, R.drawable.app_icon)
            activity.setTaskDescription(ActivityManager.TaskDescription(title, icon, colour))
            icon.recycle()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.feed_fragment, container, false)

        activity?.apply {
            val colour = ContextCompat.getColor(this, R.color.darkBlue)
            setStatusBarColour(colour)
            setMultitaskColour(colour)

            // Ensure options menu from another fragment is not carried over
            invalidateOptionsMenu()

            // Hide keyboard if navigating from login
            setKeyboardVisible(false)
        }

        setHasOptionsMenu(true)

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupViewPager()
    }

    override fun getTitle(context: Context?) = context?.getString(R.string.section_title_feed)

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.feed, menu)
        super.onCreateOptionsMenu(menu, inflater)

        val friendsItem = menu?.findItem(R.id.feed_menu_item_friends)
        friendsItem?.icon?.let {
            it.mutate()
            it.colorFilter = PorterDuff.Mode.SRC_ATOP.toColorFilter(Color.WHITE)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
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
            tabLayout.isVisible = true
            tabLayout.addOnTabSelectedListener(onTabSelectedListener)

            if (viewPager != null)
                setupWithViewPager(viewPager)
        }
    }

    override fun onPause() {
        tabLayout?.removeOnTabSelectedListener(onTabSelectedListener)
        super.onPause()
    }

    override fun onDestroy() {
        tabLayout?.removeOnTabSelectedListener(onTabSelectedListener)
        super.onDestroy()
    }
}