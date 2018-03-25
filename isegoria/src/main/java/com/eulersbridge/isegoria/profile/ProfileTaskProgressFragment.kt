package com.eulersbridge.isegoria.profile


import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import android.support.annotation.Px
import android.support.annotation.UiThread
import android.support.v4.app.Fragment
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import com.eulersbridge.isegoria.GlideApp
import com.eulersbridge.isegoria.IsegoriaApp
import com.eulersbridge.isegoria.R
import com.eulersbridge.isegoria.network.NetworkService
import com.eulersbridge.isegoria.network.api.models.Task
import com.eulersbridge.isegoria.observe
import com.eulersbridge.isegoria.util.ui.TitledFragment
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.profile_task_progress_fragment.*
import javax.inject.Inject

class ProfileTaskProgressFragment : Fragment(), TitledFragment {

    @Inject
    lateinit var app: IsegoriaApp

    @Inject
    lateinit var networkService: NetworkService

    @Inject
    lateinit var modelFactory: ViewModelProvider.Factory
    private lateinit var viewModel: ProfileViewModel

    private lateinit var completedAdapter: TaskAdapter
    private lateinit var remainingAdapter: TaskAdapter

    override fun onAttach(context: Context?) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
        viewModel = ViewModelProviders.of(this, modelFactory)[ProfileViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.profile_task_progress_fragment, container, false)

        observe(viewModel.totalXp) {
            if (it != null)
                setLevel(it)
        }

        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        progressBar.progressDrawable.setColorFilter(
            Color.parseColor("#4FBF31"),
            PorterDuff.Mode.SRC_IN
        )

        val glide = GlideApp.with(this)
        val api = networkService.api

        completedAdapter = TaskAdapter(glide, api)
        remainingAdapter = TaskAdapter(glide, api)

        completedTasksListView.adapter = completedAdapter
        remainingTasksListView.adapter = remainingAdapter

        fetchTasks()
    }

    private fun fetchTasks() {
        observe(viewModel.getRemainingTasks()) { remainingTasks ->
            if (remainingTasks != null)
                setRemainingTasks(remainingTasks)
        }

        observe(viewModel.getCompletedTasks()) { completedTasks ->
            if (completedTasks != null)
                completedAdapter.setItems(completedTasks)
        }
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

    private fun ProgressBar.setCompatProgress(progress: Int, animate: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            setProgress(progress, animate)
        } else {
            setProgress(progress)
        }
    }

    @UiThread
    private fun setLevel(totalXp: Long) {
        activity?.runOnUiThread {
            val level = totalXp.toInt() / 1000 + 1

            levelTextView.text = getString(R.string.profile_tasks_progress_level, level)

            var nextLevelPoints = totalXp.toInt() + 500
            if (nextLevelPoints == 0) nextLevelPoints = 1000

            progressBar.max = nextLevelPoints
            progressBar.setCompatProgress(totalXp.toInt(), true)

            descriptionTextView.text = getString(
                R.string.profile_tasks_progress_description,
                totalXp,
                nextLevelPoints
            )
        }
    }

    private fun setRemainingTasks(remainingTasks: List<Task>) {
        if (activity != null) {
            remainingAdapter.setItems(remainingTasks)

            // Calculate rough new list view size to 'autosize' it
            activity?.runOnUiThread {
                val heightDp = 44 * remainingTasks.size

                @Px val heightPx = Math.round(
                    TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, heightDp.toFloat(), resources.displayMetrics
                    )
                )

                val layoutParams =
                    LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, heightPx)

                remainingTasksListView.layoutParams = layoutParams
            }
        }
    }

    override fun getTitle(context: Context?)
            = context?.getString(R.string.profile_progress_section_title)
}