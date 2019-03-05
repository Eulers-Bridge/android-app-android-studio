package com.eulersbridge.isegoria.vote.pages

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.eulersbridge.isegoria.R
import com.eulersbridge.isegoria.util.ui.TitledFragment
import com.eulersbridge.isegoria.vote.VoteViewModel
import kotlinx.android.synthetic.main.vote_fragment_done.*


class VoteDoneFragment : Fragment(), TitledFragment {

    private lateinit var viewModel: VoteViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
            = inflater.inflate(R.layout.vote_fragment_done, container, false)

    fun setViewModel(viewModel: VoteViewModel) {
        this.viewModel = viewModel
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        addToCalendarButton.setOnClickListener { addToCalendar() }
    }

    private fun addToCalendar() {
        activity?.let { activity ->
            viewModel.getAddReminderToCalendarIntent()
                ?.apply {
                    if (this.resolveActivity(activity.packageManager) != null) {
                        activity.startActivity(this)
                    } else {
                        //TODO: toast message
                        print("Oh no")
                    }
                }
        }
    }

    override fun getTitle(context: Context?) = context?.getString(R.string.vote_tab_3)
}