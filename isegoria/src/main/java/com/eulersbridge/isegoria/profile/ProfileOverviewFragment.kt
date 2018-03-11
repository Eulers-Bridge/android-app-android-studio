package com.eulersbridge.isegoria.profile

import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.view.isGone
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.eulersbridge.isegoria.*
import com.eulersbridge.isegoria.network.api.models.Contact
import com.eulersbridge.isegoria.network.api.models.GenericUser
import com.eulersbridge.isegoria.network.api.models.User
import com.eulersbridge.isegoria.personality.PersonalityQuestionsActivity
import com.eulersbridge.isegoria.util.transformation.BlurTransformation
import com.eulersbridge.isegoria.util.transformation.TintTransformation
import com.eulersbridge.isegoria.util.ui.TitledFragment
import kotlinx.android.synthetic.main.profile_overview_fragment.*

class ProfileOverviewFragment : Fragment(), TitledFragment {

    private lateinit var taskAdapter: TaskAdapter

    private val viewModel: ProfileViewModel by lazy {
        val lifecycleOwner = parentFragment ?: this
        ViewModelProviders.of(lifecycleOwner).get(ProfileViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.profile_overview_fragment, container, false)

        val app = activity?.application as IsegoriaApp?
        observe(app?.loggedInUser) {
            if (it != null && viewModel.user.value == null)
                viewModel.setUser(it)
        }

        val user = arguments?.getParcelable<Parcelable>(FRAGMENT_EXTRA_CONTACT) as? GenericUser

        if (user == null) {
            val userId = arguments?.getLong(FRAGMENT_EXTRA_PROFILE_ID)
        }

        if (user != null)
            viewModel.setUser(user)

        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val app: IsegoriaApp = requireActivity().application as IsegoriaApp
        val api = app.api

        taskAdapter = TaskAdapter(Glide.with(this), api)

        friendsCountTextView.setOnClickListener({  viewModel.viewFriends() })
        friendsLabel.setOnClickListener({  viewModel.viewFriends() })

        tasksListView.apply {
            adapter = taskAdapter
            isNestedScrollingEnabled = false
            isDrawingCacheEnabled = true
            drawingCacheQuality = View.DRAWING_CACHE_QUALITY_LOW
        }

        createViewModelObservers()
    }

    private fun createViewModelObservers() {
        observe(viewModel.user) { user ->
            if (user == null) {
                personalityTestButton.isGone = true
                return@observe
            }

            observe(viewModel.getUserPhoto()) {
                if (it != null)
                    GlideApp.with(this)
                        .load(it.thumbnailUrl)
                        .transforms(BlurTransformation(context!!), TintTransformation(0.1))
                        .priority(Priority.HIGH)
                        .placeholder(R.color.profileImageBackground)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(backgroundImageView)
            }

            observe(viewModel.getInstitutionName()) {
                institutionTextView.text = it
            }

            observe(viewModel.getRemainingBadges()) { remainingBadges ->
                if (remainingBadges != null)
                    updateBadgesCount(user.completedBadgesCount, remainingBadges.size.toLong())
            }

            observe(viewModel.getTasks()) { tasks ->
                if (tasks != null)
                    taskAdapter.setItems(tasks)
            }

            viewModel.fetchUserStats()

            GlideApp.with(this)
                .load(user.profilePhotoURL)
                .priority(Priority.HIGH)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(smallPhotoImageView)

            nameTextView.text = user.fullName

            val isLoggedInUserWithPersonality = user is User && user.hasPersonality
            val isExternalUser = user is Contact
            if (isExternalUser || isLoggedInUserWithPersonality) {
                personalityTestButton.isGone = true

            } else {
                personalityTestButton.setOnClickListener {
                    startActivity(Intent(activity, PersonalityQuestionsActivity::class.java))
                }
            }

            if (isExternalUser) {
                viewProgressTextView.isGone = true
            } else {
                viewProgressTextView.setOnClickListener { viewModel.viewTasksProgress() }
            }

            updateCompletedTasksCount(user.completedTasksCount)
            updateExperience(user.level, user.experience)
        }

        observe(viewModel.contactsCount) {
            if (it != null) updateContactsCount(it)
        }

        observe(viewModel.totalTasksCount) {
            if (it != null) updateTotalTasksCount(it)
        }
    }

    private fun updateBadgesCount(completedCount: Long, remainingCount: Long) {
        badgesProgressCircle.post {
            badgesProgressCircle.topText = completedCount.toString()
            badgesProgressCircle.bottomText = "/" + remainingCount.toString()
            badgesProgressCircle.maximumValue = remainingCount.toInt()
            badgesProgressCircle.setValue(completedCount.toInt(), true)
        }
    }

    private fun updateCompletedTasksCount(count: Long) {
        tasksProgressCircle.post {
            tasksProgressCircle.topText  = count.toString()
            tasksProgressCircle.setValue(count.toInt(), true)
        }
    }

    private fun updateExperience(level: Long, experience: Long) {
        experienceProgressCircle.post {
            experienceProgressCircle.topText = level.toString()

            val progress = experience % 1000
            val max: Long = 1000

            experienceProgressCircle.bottomText = "NEED $progress"
            experienceProgressCircle.maximumValue = max.toInt()
            experienceProgressCircle.setValue(progress.toInt(), true)
        }
    }

    private fun updateContactsCount(contactsCount: Long) {
        friendsCountTextView.text = contactsCount.toString()
    }

    private fun updateTotalTasksCount(totalTasksCount: Long) {
        tasksProgressCircle?.post { tasksProgressCircle.maximumValue  = totalTasksCount.toInt() }
    }

    override fun onDetach() {
        super.onDetach()

        // Work around a child fragment manager bug: https://stackoverflow.com/a/15656428/447697
        try {
            val childFragmentManager =
                Fragment::class.java.getDeclaredField("mChildFragmentManager")
            childFragmentManager.isAccessible = true
            childFragmentManager.set(this, null)

        } catch (e: NoSuchFieldException) {
            throw RuntimeException(e)
        } catch (e: IllegalAccessException) {
            throw RuntimeException(e)
        }
    }

    override fun getTitle(context: Context?)
            = context?.getString(R.string.profile_overview_section_title)
}