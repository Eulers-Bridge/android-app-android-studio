package com.eulersbridge.isegoria.feed

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import com.eulersbridge.isegoria.feed.events.EventsFragment
import com.eulersbridge.isegoria.feed.news.NewsFragment
import com.eulersbridge.isegoria.feed.photos.PhotosFragment
import com.eulersbridge.isegoria.util.ui.TitledFragment

internal class FeedViewPagerAdapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager) {

    companion object {
        private const val FRAGMENT_COUNT = 3
    }

    private val fragments: ArrayList<Fragment> by lazy {
        ArrayList<Fragment>(FRAGMENT_COUNT)
    }

    override fun getItem(position: Int): Fragment {
        return try {
            fragments[position]

        } catch (e: Exception) {
            val fragment: Fragment = when (position) {
                1 -> PhotosFragment()
                2 -> EventsFragment()
                else -> NewsFragment()
            }

            fragments.add(position, fragment)

            fragment
        }
    }

    override fun getCount() = FRAGMENT_COUNT

    override fun getPageTitle(position: Int): CharSequence? {
        getItem(position).let {
            return if (it is TitledFragment)
                it.getTitle(it.context)
            else
                null
        }
    }
}