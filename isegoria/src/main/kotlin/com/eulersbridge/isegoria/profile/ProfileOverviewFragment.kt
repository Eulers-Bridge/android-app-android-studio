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
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.eulersbridge.isegoria.*
import com.eulersbridge.isegoria.data.Repository
import com.eulersbridge.isegoria.network.api.model.GenericUser
import com.eulersbridge.isegoria.personality.PersonalityActivity
import com.eulersbridge.isegoria.util.extension.observe
import com.eulersbridge.isegoria.util.transformation.BlurTransformation
import com.eulersbridge.isegoria.util.transformation.TintTransformation
import com.eulersbridge.isegoria.util.ui.TitledFragment
import kotlinx.android.synthetic.main.profile_overview_fragment.*

class ProfileOverviewFragment : Fragment(), TitledFragment {

    private class ViewModelProviderFactory(
            private val repository: Repository,
            private val appRouter: AppRouter?
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ProfileViewModel(repository, appRouter) as T
        }
    }

    companion object {
        fun create(repository: Repository, viewModel: ProfileViewModel? = null, appRouter: AppRouter? = null): ProfileOverviewFragment {
            val fragment = ProfileOverviewFragment()
            fragment.provideDependencies(repository, viewModel, appRouter)
            return fragment
        }
    }

    private lateinit var repository: Repository
    private var appRouter: AppRouter? = null
    private lateinit var viewModel: ProfileViewModel
    private var wasOpenedByFriendsScreen = false
    private lateinit var taskAdapter: TaskAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.profile_overview_fragment, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if (!::viewModel.isInitialized)
            viewModel = ViewModelProviders.of(this, ViewModelProviderFactory(repository, appRouter))[ProfileViewModel::class.java]

        requireNotNull(viewModel)

        personalityTestButton.setOnClickListener {
            startActivity(Intent(activity, PersonalityActivity::class.java))
        }

        viewProgressTextView.setOnClickListener { viewModel.viewTasksProgress() }

        setupTaskListView()
        createViewModelObservers()
        fetchAndSetViewModelUser()
    }

    private val fragmentExtraContactArgument get() = arguments?.getParcelable<Parcelable>(FRAGMENT_EXTRA_CONTACT) as? GenericUser
    private val fragmentExtraUserArgument get() = arguments?.getParcelable<Parcelable>(FRAGMENT_EXTRA_USER) as? GenericUser

    private fun fetchAndSetViewModelUser() {
        //checks if the parcelable is the current user or a contact
        val userArgument = fragmentExtraContactArgument ?: fragmentExtraUserArgument

        wasOpenedByFriendsScreen = userArgument != null

        viewModel.setUser(userArgument ?: repository.getUserFromLoginState(), !wasOpenedByFriendsScreen)
    }

    private fun provideDependencies(repository: Repository, viewModel: ProfileViewModel?, appRouter: AppRouter?) {
        this.repository = repository
        if (viewModel != null) this.viewModel = viewModel
        this.appRouter = appRouter
    }

    private fun setupTaskListView() {
        taskAdapter = TaskAdapter(Glide.with(this), repository)

        friendsCountTextView.setOnClickListener({  viewModel.viewFriends() })
        friendsLabel.setOnClickListener({  viewModel.viewFriends() })

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

        observe(viewModel.userPhoto) {
            if (it != null)
                GlideApp.with(this)
                        .load(it.getPhotoUrl())
                        .transforms(BlurTransformation(context!!), TintTransformation(0.1))
                        .priority(Priority.HIGH)
                        .placeholder(R.color.profileImageBackground)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(backgroundImageView)
        }

        observe(viewModel.institutionName) {
            institutionTextView.text = it
        }

        observe(viewModel.badgeCount) {
            if (it != null)
                updateBadgeCount(it.remaining, it.completed)
        }

        observe(viewModel.tasks) {
            taskAdapter.submitList(it!!)
        }

        observe(viewModel.personalityTestHintVisible) {
            personalityTestButton.isGone = (it == false)
        }

        observe(viewModel.viewProgressHintVisible) {
            viewProgressTextView.isGone = (it == false)
        }

        observe(viewModel.user) {
            it?.let { user ->
                GlideApp.with(this)
                        .load(user.profilePhotoURL)
                        .transform(CircleCrop())
                        .priority(Priority.HIGH)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(smallPhotoImageView)

                nameTextView.text = user.fullName

                updateCompletedTasksCount(user.completedTasksCount)
                updateExperience(user.level, user.experience)
            }
        }

        observe(viewModel.contactsCount) {
            if (it != null) updateContactsCount(it)
        }

        observe(viewModel.totalTasksCount) {
            if (it != null) updateTotalTasksCount(it)
        }
    }

    private fun updateBadgeCount(remainingCount: Int, completedCount: Int) {
        badgesProgressCircle.post {
            badgesProgressCircle?.topText = completedCount.toString()
            badgesProgressCircle?.bottomText = "/" + remainingCount.toString()
            badgesProgressCircle?.maximumValue = remainingCount
            badgesProgressCircle?.setValue(completedCount, true)
        }
    }

    private fun updateCompletedTasksCount(count: Long) {
        tasksProgressCircle.post {
            tasksProgressCircle?.topText = count.toString()
            tasksProgressCircle?.setValue(count.toInt(), true)
        }
    }

    private fun updateExperience(level: Long, experience: Long) {
        experienceProgressCircle.post {
            experienceProgressCircle?.topText = level.toString()

            val progress = experience % 1000
            val max: Long = 1000

            experienceProgressCircle?.bottomText = "NEED $progress"
            experienceProgressCircle?.maximumValue = max.toInt()
            experienceProgressCircle?.setValue(progress.toInt(), true)
        }
    }

    private fun updateContactsCount(contactsCount: Long) {
        friendsCountTextView?.text = contactsCount.toString()
    }

    private fun updateTotalTasksCount(totalTasksCount: Long) {
        tasksProgressCircle?.post { tasksProgressCircle?.maximumValue  = totalTasksCount.toInt() }
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