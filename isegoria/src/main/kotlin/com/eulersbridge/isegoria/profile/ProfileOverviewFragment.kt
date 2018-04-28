package com.eulersbridge.isegoria.profile

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.eulersbridge.isegoria.FRAGMENT_EXTRA_CONTACT
import com.eulersbridge.isegoria.GlideApp
import com.eulersbridge.isegoria.R
import com.eulersbridge.isegoria.data.Repository
import com.eulersbridge.isegoria.friends.FriendsFragment
import com.eulersbridge.isegoria.network.api.model.GenericUser
import com.eulersbridge.isegoria.personality.PersonalityActivity
import com.eulersbridge.isegoria.util.extension.ifTrue
import com.eulersbridge.isegoria.util.extension.observe
import com.eulersbridge.isegoria.util.transformation.BlurTransformation
import com.eulersbridge.isegoria.util.transformation.TintTransformation
import com.eulersbridge.isegoria.util.ui.TitledFragment
import kotlinx.android.synthetic.main.profile_overview_fragment.*

class ProfileOverviewFragment : Fragment(), TitledFragment {

    companion object {
        fun create(repository: Repository, viewModel: ProfileViewModel?): ProfileOverviewFragment {
            val fragment = ProfileOverviewFragment()
            fragment.provideDependencies(repository, viewModel)
            return fragment
        }
    }

    private var wasOpenedByFriendsScreen = false
    private lateinit var repository: Repository
    private lateinit var taskAdapter: TaskAdapter
    private var viewModel: ProfileViewModel? = null

    private class ViewModelProviderFactory(private val repository: Repository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ProfileViewModel(repository) as T
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.profile_overview_fragment, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if (viewModel == null) {
            viewModel = ViewModelProviders.of(this, ViewModelProviderFactory(repository))[ProfileViewModel::class.java]
        }

        requireNotNull(viewModel)

        personalityTestButton.setOnClickListener {
            startActivity(Intent(activity, PersonalityActivity::class.java))
        }

        viewProgressTextView.setOnClickListener { viewModel!!.viewTasksProgress() }

        setupTaskListView()
        createViewModelObservers()
        fetchAndSetViewModelUser()
    }

    private fun fetchAndSetViewModelUser() {
        val userArgument = arguments?.getParcelable<Parcelable>(FRAGMENT_EXTRA_CONTACT) as? GenericUser
        wasOpenedByFriendsScreen = userArgument != null

        val user = userArgument ?: repository.getUser()

        /*if (user == null) {
            val userId = arguments?.getLong(FRAGMENT_EXTRA_PROFILE_ID)

            // ...
        }*/

        viewModel!!.setUser(user)
    }

    private fun provideDependencies(repository: Repository, viewModel: ProfileViewModel?) {
        this.repository = repository
        this.viewModel = viewModel
    }

    private fun setupTaskListView() {
        taskAdapter = TaskAdapter(Glide.with(this), repository)

        friendsCountTextView.setOnClickListener({  viewModel!!.viewFriends() })
        friendsLabel.setOnClickListener({  viewModel!!.viewFriends() })

        tasksListView.apply {
            adapter = taskAdapter
            isNestedScrollingEnabled = false

            // TODO: Change condition when P releases
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O_MR1 + 1) {
                isDrawingCacheEnabled = true
                drawingCacheQuality = View.DRAWING_CACHE_QUALITY_LOW
            }
        }
    }

    private fun createViewModelObservers() {
        requireNotNull(viewModel)

        observe(viewModel!!.userPhoto) {
            if (it != null)
                GlideApp.with(this)
                        .load(it.getPhotoUrl())
                        .transforms(BlurTransformation(context!!), TintTransformation(0.1))
                        .priority(Priority.HIGH)
                        .placeholder(R.color.profileImageBackground)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(backgroundImageView)
        }

        observe(viewModel!!.institutionName) {
            institutionTextView.text = it
        }

        observe(viewModel!!.badgeCount) {
            if (it != null)
                updateBadgeCount(it.remaining, it.completed)
        }

        observe(viewModel!!.tasks) {
            taskAdapter.submitList(it!!)
        }

        observe(viewModel!!.personalityTestHintVisible) {
            personalityTestButton.isGone = (it == false)
        }

        observe(viewModel!!.viewProgressHintVisible) {
            viewProgressTextView.isGone = (it == false)
        }

        observe(viewModel!!.user) {
            it?.let { user ->
                GlideApp.with(this)
                        .load(user.profilePhotoURL)
                        .priority(Priority.HIGH)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(smallPhotoImageView)

                nameTextView.text = user.fullName

                updateCompletedTasksCount(user.completedTasksCount)
                updateExperience(user.level, user.experience)
            }
        }

        observe(viewModel!!.contactsCount) {
            if (it != null) updateContactsCount(it)
        }

        observe(viewModel!!.totalTasksCount) {
            if (it != null) updateTotalTasksCount(it)
        }

        ifTrue(viewModel!!.friendsScreenVisible) {
            childFragmentManager
                    .beginTransaction()
                    .add(FriendsFragment(), null)
                    .addToBackStack(null)
                    .commit()
        }
    }

    private fun updateBadgeCount(remainingCount: Int, completedCount: Int) {
        badgesProgressCircle.post {
            badgesProgressCircle.topText = completedCount.toString()
            badgesProgressCircle.bottomText = "/" + remainingCount.toString()
            badgesProgressCircle.maximumValue = remainingCount
            badgesProgressCircle.setValue(completedCount, true)
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

    override fun getTitle(context: Context?): String? {
        return if (wasOpenedByFriendsScreen) {
            ""

        } else {
            context?.getString(R.string.profile_overview_section_title)
        }
    }
}