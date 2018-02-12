package com.eulersbridge.isegoria.vote

import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.eulersbridge.isegoria.R
import com.eulersbridge.isegoria.observe
import com.eulersbridge.isegoria.util.ui.TitledFragment
import kotlinx.android.synthetic.main.vote_fragment_done.*

class VoteDoneFragment : Fragment(), TitledFragment {

    private lateinit var viewModel: VoteViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.vote_fragment_done, container, false)

        viewModel = ViewModelProviders.of(parentFragment!!).get(VoteViewModel::class.java)

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        addToCalendarButton.apply {
            isEnabled = false
            setOnClickListener { addToCalendar() }
        }

        observe(viewModel.getLatestVoteReminder()) { success ->
            if (success == false)
                addToCalendarButton.isEnabled = true
        }
    }

    private fun addToCalendar() {
        addToCalendarButton.isEnabled = false

        activity?.let { activity ->
            val addToCalendarIntent = viewModel.addVoteReminderToCalendarIntent
            if (addToCalendarIntent?.resolveActivity(activity.packageManager) != null) {
                activity.startActivity(addToCalendarIntent)
            }
        }

        addToCalendarButton.isEnabled = true
    }

    override fun getTitle(context: Context?) = context?.getString(R.string.vote_tab_3)
}