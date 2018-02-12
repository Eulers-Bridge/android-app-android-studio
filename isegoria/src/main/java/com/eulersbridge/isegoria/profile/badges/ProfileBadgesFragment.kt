package com.eulersbridge.isegoria.profile.badges

import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.MemoryCategory
import com.eulersbridge.isegoria.R
import com.eulersbridge.isegoria.observe
import com.eulersbridge.isegoria.profile.ProfileViewModel
import com.eulersbridge.isegoria.util.ui.TitledFragment
import kotlinx.android.synthetic.main.profile_badges_fragment.*

class ProfileBadgesFragment : Fragment(), TitledFragment {

    private lateinit var viewModel: ProfileViewModel
    private val badgeAdapter = BadgeAdapter(this)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.profile_badges_fragment, container, false)

        viewModel = ViewModelProviders.of(parentFragment!!).get(ProfileViewModel::class.java)

        arguments?.getInt("level")?.let {
            viewModel.setTargetBadgeLevel(it)
        }

        Glide.get(context!!).setMemoryCategory(MemoryCategory.HIGH)

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        badgesGridView.apply {
            adapter = badgeAdapter
            isDrawingCacheEnabled = true
            drawingCacheQuality = View.DRAWING_CACHE_QUALITY_AUTO
            setItemViewCacheSize(21)
        }

        observe(viewModel.user) {
            if (it != null) getBadges()
        }
    }

    private fun getBadges() {
        observe(viewModel.getRemainingBadges(true)) { remainingBadges ->
            if (remainingBadges != null)
                badgeAdapter.replaceRemainingItems(remainingBadges)
        }

        observe(viewModel.getCompletedBadges()) { completedBadges ->
            if (completedBadges != null)
                badgeAdapter.replaceCompletedItems(completedBadges)
        }
    }

    override fun onDetach() {
        super.onDetach()

        if (context != null)
            Glide.get(context!!).setMemoryCategory(MemoryCategory.NORMAL)
    }

    override fun getTitle(context: Context?)
            = context?.getString(R.string.profile_badges_section_title)
}
