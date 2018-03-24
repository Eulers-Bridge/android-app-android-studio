package com.eulersbridge.isegoria.election


import android.animation.LayoutTransition
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.view.isVisible
import com.eulersbridge.isegoria.R
import com.eulersbridge.isegoria.network.api.models.Election
import com.eulersbridge.isegoria.observe
import com.eulersbridge.isegoria.toDateString
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.election_overview_fragment.*
import javax.inject.Inject

class ElectionOverviewFragment : Fragment() {

    @Inject
    lateinit var modelFactory: ViewModelProvider.Factory
    private lateinit var viewModel: ElectionViewModel

    override fun onAttach(context: Context?) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
        viewModel = ViewModelProviders.of(this, modelFactory)[ElectionViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.election_overview_fragment, container, false)

        observe(viewModel.getElection()) {
            if (it != null)
                populateElectionText(it)
        }

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
            introductionHeadingTextView.isVisible = true
            introductionTextView.isVisible = true
            introductionTextView.text = election.introduction

        } else {
            introductionHeadingTextView.isVisible = false
            introductionTextView.isVisible = false
        }

        if (!election.process.isNullOrBlank()) {
            processHeadingTextView.isVisible = true
            processTextView.isVisible = true
            processTextView.text = election.process

        } else {
            processHeadingTextView.isVisible = false
            processTextView.isVisible = false
        }
    }
}
