package com.eulersbridge.isegoria.feed

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import com.eulersbridge.isegoria.feed.events.EventsFragment
import com.eulersbridge.isegoria.feed.news.NewsFragment
import com.eulersbridge.isegoria.feed.photos.PhotosFragment
import com.eulersbridge.isegoria.util.ui.TitledFragment

internal class FeedViewPagerAdapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager) {

    private companion object {
        private const val FRAGMENT_COUNT = 3
    }

    private val fragments by lazy {
        arrayOf<Fragment>(NewsFragment(), PhotosFragment(), EventsFragment())
    }

    override fun getItem(position: Int) = fragments[position]

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