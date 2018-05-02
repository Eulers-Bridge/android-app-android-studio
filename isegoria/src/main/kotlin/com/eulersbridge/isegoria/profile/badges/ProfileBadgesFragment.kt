package com.eulersbridge.isegoria.profile.badges

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.eulersbridge.isegoria.GlideApp
import com.eulersbridge.isegoria.R
import com.eulersbridge.isegoria.data.Repository
import com.eulersbridge.isegoria.profile.ProfileViewModel
import com.eulersbridge.isegoria.util.extension.observe
import com.eulersbridge.isegoria.util.ui.TitledFragment
import kotlinx.android.synthetic.main.profile_badges_fragment.*

class ProfileBadgesFragment : Fragment(), TitledFragment {

    private lateinit var repository: Repository
    private lateinit var viewModel: ProfileViewModel
    private lateinit var badgeAdapter: BadgeAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.profile_badges_fragment, container, false)

        arguments?.getInt("level")?.let {
            viewModel.setTargetBadgeLevel(it)
        }

        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        setupBadgesGridView()
        createViewModelObservers()
    }

    fun provideDependencies(repository: Repository, viewModel: ProfileViewModel) {
        this.repository = repository
        this.viewModel = viewModel
    }

    private fun setupBadgesGridView() {
        badgeAdapter = BadgeAdapter(GlideApp.with(this), repository)

        badgesGridView.apply {
            adapter = badgeAdapter
            setItemViewCacheSize(21)

            // TODO: Change condition when P releases
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O_MR1 + 1) {
                isDrawingCacheEnabled = true
                drawingCacheQuality = View.DRAWING_CACHE_QUALITY_LOW
            }
        }
    }

    private fun createViewModelObservers() {
        observe(viewModel.remainingBadges) {
            if (it != null)
                badgeAdapter.replaceRemainingItems(it)
        }

        observe(viewModel.getCompletedBadges()) { completedBadges ->
            if (completedBadges != null)
                badgeAdapter.replaceCompletedItems(completedBadges)
        }
    }

    override fun getTitle(context: Context?)
            = context?.getString(R.string.profile_badges_section_title)
}
