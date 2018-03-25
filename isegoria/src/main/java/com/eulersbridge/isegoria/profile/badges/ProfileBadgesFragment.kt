package com.eulersbridge.isegoria.profile.badges

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.eulersbridge.isegoria.GlideApp
import com.eulersbridge.isegoria.R
import com.eulersbridge.isegoria.network.NetworkService
import com.eulersbridge.isegoria.observe
import com.eulersbridge.isegoria.profile.ProfileViewModel
import com.eulersbridge.isegoria.util.ui.TitledFragment
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.profile_badges_fragment.*
import javax.inject.Inject

class ProfileBadgesFragment : Fragment(), TitledFragment {

    @Inject
    lateinit var modelFactory: ViewModelProvider.Factory
    private lateinit var viewModel: ProfileViewModel

    private lateinit var badgeAdapter: BadgeAdapter

    @Inject
    lateinit var networkService: NetworkService

    override fun onAttach(context: Context?) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)

        viewModel = if (parentFragment != null) {
            ViewModelProviders.of(parentFragment!!)[ProfileViewModel::class.java]
        } else {
            ViewModelProviders.of(this, modelFactory)[ProfileViewModel::class.java]
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.profile_badges_fragment, container, false)

        viewModel.setTargetBadgeLevel(arguments?.getInt("level"))

        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        badgeAdapter = BadgeAdapter(GlideApp.with(this), networkService.api)

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

    override fun getTitle(context: Context?)
            = context?.getString(R.string.profile_badges_section_title)
}
