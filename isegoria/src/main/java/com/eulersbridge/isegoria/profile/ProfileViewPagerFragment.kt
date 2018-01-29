package com.eulersbridge.isegoria.profile

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.support.v7.app.AlertDialog
import android.view.*
import com.eulersbridge.isegoria.MainActivity
import com.eulersbridge.isegoria.R
import com.eulersbridge.isegoria.profile.badges.ProfileBadgesFragment
import com.eulersbridge.isegoria.profile.settings.SettingsActivity
import com.eulersbridge.isegoria.util.ui.SimpleFragmentPagerAdapter
import com.eulersbridge.isegoria.util.ui.TitledFragment
import kotlinx.android.synthetic.main.profile_viewpager_fragment.*
import java.util.*

class ProfileViewPagerFragment : Fragment(), TitledFragment, MainActivity.TabbedFragment {

    private var tabLayout: TabLayout? = null
    private lateinit var viewModel: ProfileViewModel

    private val onTabSelectedListener = object : TabLayout.OnTabSelectedListener {
        override fun onTabSelected(tab: TabLayout.Tab) {
            viewPager.currentItem = tab.position
        }

        override fun onTabUnselected(tab: TabLayout.Tab) {}

        override fun onTabReselected(tab: TabLayout.Tab) {}
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.profile_viewpager_fragment, container, false)

        setHasOptionsMenu(true)

        viewModel = ViewModelProviders.of(this).get(ProfileViewModel::class.java)

        viewModel.currentSectionIndex.observe(this, Observer { it ->
            if (it != null && viewPager.currentItem != it)
                viewPager.currentItem = it
        })

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = setupViewPager()

    private fun setupViewPager() {
        val fragmentList = ArrayList<Fragment>()
        fragmentList.apply {
            add(ProfileOverviewFragment())
            add(ProfileTaskProgressFragment())
            add(ProfileBadgesFragment())
        }

        val pagerAdapter = object : SimpleFragmentPagerAdapter(childFragmentManager, fragmentList) {
            override fun getPageTitle(position: Int): CharSequence? {
                return (fragmentList[position] as TitledFragment).getTitle(context!!)
            }
        }

        viewPager.apply {
            adapter = pagerAdapter
            offscreenPageLimit = 3
            currentItem = 0

            addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

                override fun onPageSelected(position: Int) {
                    viewModel.onSectionIndexChanged(position)
                }

                override fun onPageScrollStateChanged(state: Int) {}
            })
        }
    }

    override fun getTitle(context: Context?): String?
            = context?.getString(R.string.section_title_profile)

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater!!.inflate(R.menu.profile, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.profile_settings -> {
                startActivity(Intent(context, SettingsActivity::class.java))
                return true
            }

            R.id.profile_logout -> {
                if (context != null)
                    AlertDialog.Builder(context!!)
                            .setTitle(R.string.log_out_confirmation_title)
                            .setPositiveButton(android.R.string.yes
                            ) { _, _ -> viewModel.logOut() }
                            .setNegativeButton(android.R.string.no, null)
                            .show()

                return true
            }

            else -> return super.onOptionsItemSelected(item)
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

    override fun onPause() {
        super.onPause()

        tabLayout?.removeOnTabSelectedListener(onTabSelectedListener)
    }
}