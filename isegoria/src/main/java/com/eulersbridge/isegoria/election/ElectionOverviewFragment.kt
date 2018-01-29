package com.eulersbridge.isegoria.election


import android.animation.LayoutTransition
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.eulersbridge.isegoria.R
import com.eulersbridge.isegoria.network.api.models.Election
import com.eulersbridge.isegoria.toDateString
import kotlinx.android.synthetic.main.election_overview_fragment.*

class ElectionOverviewFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.election_overview_fragment, container, false)

        val viewModel = ViewModelProviders.of(this).get(ElectionViewModel::class.java)
        viewModel.getElection().observe(this, Observer { election ->
            if (election != null)
                populateElectionText(election)
        })

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        /* By default, animateLayoutChanges="true" will not work when children change size,
          so enable animation when a child changes its size */
        (contentContainer as ViewGroup)
            .layoutTransition
            .enableTransitionType(LayoutTransition.CHANGING)
    }

    private fun populateElectionText(election: Election) {
        titleTextView.text = election.title
        dateTextView.text = election.start.toDateString(context!!)

        if (!election.introduction.isNullOrBlank()) {
            introductionHeadingTextView.visibility = View.VISIBLE
            introductionTextView.visibility = View.VISIBLE
            introductionTextView.text = election.introduction

        } else {
            introductionHeadingTextView!!.visibility = View.GONE
            introductionTextView.visibility = View.GONE
        }

        if (!election.process.isNullOrBlank()) {
            processHeadingTextView.visibility = View.VISIBLE
            processTextView.visibility = View.VISIBLE
            processTextView.text = election.process

        } else {
            processHeadingTextView.visibility = View.GONE
            processTextView.visibility = View.GONE
        }
    }
}
