package com.eulersbridge.isegoria.profile


import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import android.support.annotation.UiThread
import android.support.v4.app.Fragment
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.eulersbridge.isegoria.R
import com.eulersbridge.isegoria.network.api.models.Task
import com.eulersbridge.isegoria.util.ui.TitledFragment
import kotlinx.android.synthetic.main.profile_task_progress_fragment.*

class ProfileTaskProgressFragment : Fragment(), TitledFragment {

    private val completedAdapter = TaskAdapter(this)
    private val remainingAdapter = TaskAdapter(this)

    private lateinit var viewModel: ProfileViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.profile_task_progress_fragment, container, false)

        viewModel = ViewModelProviders.of(parentFragment!!).get(ProfileViewModel::class.java)

        viewModel.totalXp.observe(this, Observer {
            if (it != null)
                setLevel(it)
        })

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        progressBar.progressDrawable.setColorFilter(
            Color.parseColor("#4FBF31"),
            PorterDuff.Mode.SRC_IN
        )

        completedTasksListView.adapter = completedAdapter
        remainingTasksListView.adapter = remainingAdapter

        fetchTasks()
    }

    private fun fetchTasks() {
        viewModel.getRemainingTasks()?.observe(this, Observer { remainingTasks ->
            if (remainingTasks != null)
                setRemainingTasks(remainingTasks)
        })

        viewModel.getCompletedTasks()?.observe(this, Observer { completedTasks ->
            if (completedTasks != null)
                completedAdapter.setItems(completedTasks)
        })
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

    @UiThread
    private fun setLevel(totalXp: Long) {
        if (activity != null) {
            activity!!.runOnUiThread {
                val level = totalXp.toInt() / 1000 + 1

                levelTextView.text = getString(R.string.profile_tasks_progress_level, level)

                var nextLevelPoints = totalXp.toInt() + 500
                nextLevelPoints /= 1000
                nextLevelPoints *= 1000

                if (nextLevelPoints == 0) nextLevelPoints = 1000

                progressBar.max = nextLevelPoints

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    progressBar.setProgress(totalXp.toInt(), true)
                } else {
                    progressBar.progress = totalXp.toInt()
                }

                descriptionTextView.text = getString(
                    R.string.profile_tasks_progress_description,
                    totalXp,
                    nextLevelPoints
                )
            }
        }
    }

    private fun setRemainingTasks(remainingTasks: List<Task>) {
        if (activity != null) {
            remainingAdapter.setItems(remainingTasks)

            // Calculate rough new list view size to 'autosize' it
            activity!!.runOnUiThread {
                val heightDp = 44 * remainingTasks.size

                val heightPx = Math.round(
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