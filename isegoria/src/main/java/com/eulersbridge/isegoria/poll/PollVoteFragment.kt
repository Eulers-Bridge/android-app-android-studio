package com.eulersbridge.isegoria.poll

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.eulersbridge.isegoria.ACTIVITY_EXTRA_POLL
import com.eulersbridge.isegoria.GlideApp
import com.eulersbridge.isegoria.R
import com.eulersbridge.isegoria.network.api.models.Poll
import kotlinx.android.synthetic.main.poll_fragment.*

class PollVoteFragment : Fragment(), PollOptionAdapter.PollOptionVoteListener {

    private lateinit var viewModel: PollViewModel
    private var pollOptionsAdapter: PollOptionAdapter = PollOptionAdapter(this)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val rootView = inflater.inflate(R.layout.poll_fragment, container, false)

        viewModel = ViewModelProviders.of(this).get(PollViewModel::class.java)

        if (arguments != null) {
            val poll =
                arguments!!.getParcelable<Poll>(ACTIVITY_EXTRA_POLL)
            viewModel.poll.value = poll
        }

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        listView.adapter = pollOptionsAdapter

        createViewModelObservers()
    }

    private fun createViewModelObservers() {
        viewModel.poll.observe(this, Observer {
            if (it != null) {
                questionTextView.text = it.question
                populatePollOptions()
            }
        })

        viewModel.pollCreator.observe(this, Observer { creator ->
            if (creator != null) {
                GlideApp.with(this)
                    .load(creator.profilePhotoURL)
                    .placeholder(R.color.lightGrey)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(creatorImageView)

                creatorNameTextView.apply {
                    text = getString(R.string.poll_asked_by, creator.fullName)
                    visibility = View.VISIBLE
                }
            }
        })

        viewModel.pollResults.observe(this, Observer { results ->
            results?.let {
                val answersCount = results.size

                answersCountTextView.text = answersCount.toString()
                answersCountTextView.contentDescription =
                        getString(R.string.poll_vote_answers_content_description, answersCount)

                populatePollOptions()
            }
        })
    }

    private fun populatePollOptions() {
        val poll = viewModel.poll.value

        poll?.options?.let {
            pollOptionsAdapter.isLoading = false
            pollOptionsAdapter.replaceItems(it)
            pollOptionsAdapter.pollVotingEnabled = !poll.closed
        }
    }

    override fun onPollOptionClick(position: Int) {
        viewModel.voteForPollOption(position)
    }
}
