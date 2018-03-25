package com.eulersbridge.isegoria.poll

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.eulersbridge.isegoria.*
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.poll_fragment.*
import javax.inject.Inject

class PollVoteFragment : Fragment(), PollOptionAdapter.PollOptionVoteListener {

    @Inject
    lateinit var modelFactory: ViewModelProvider.Factory
    private lateinit var viewModel: PollVoteViewModel

    private var pollOptionsAdapter: PollOptionAdapter = PollOptionAdapter(this)

    override fun onAttach(context: Context?) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
        viewModel = ViewModelProviders.of(this, modelFactory)[PollVoteViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val rootView = inflater.inflate(R.layout.poll_fragment, container, false)

        viewModel.poll.value = arguments?.getParcelable(ACTIVITY_EXTRA_POLL)

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        listView.adapter = pollOptionsAdapter

        createViewModelObservers()
    }

    private fun createViewModelObservers() {
        observe(viewModel.poll) {
            if (it != null) {
                questionTextView.text = it.question
                populatePollOptions()
            }
        }

        observe(viewModel.pollCreator) {
            if (it != null) {
                GlideApp.with(this)
                    .load(it.profilePhotoURL)
                    .placeholder(R.color.lightGrey)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(creatorImageView)

                creatorNameTextView.apply {
                    text = getString(R.string.poll_asked_by, it.fullName)
                    visibility = View.VISIBLE
                }
            }
        }

        observe(viewModel.pollResults) { results ->
            results?.let {
                val answerCount = results.size

                answersCountTextView.text = answerCount.toString()

                val answersQuantity =
                    resources.getQuantityString(R.plurals.poll_vote_answers_content_description, answerCount)
                answersCountTextView.contentDescription = answersQuantity
                answersCountTextView.setCompatTooltipText(answersQuantity)

                populatePollOptions()
            }
        }
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
