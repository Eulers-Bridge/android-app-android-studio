package com.eulersbridge.isegoria.poll;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.eulersbridge.isegoria.GlideApp;
import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.network.api.models.Poll;
import com.eulersbridge.isegoria.util.Constants;

import org.parceler.Parcels;

public class PollVoteFragment extends Fragment implements PollOptionAdapter.PollOptionVoteListener {

    private PollViewModel viewModel;

    private ImageView creatorImageView;
    private TextView questionTextView;
    private TextView creatorNameTextView;
    private TextView answersCountTextView;

    private PollOptionAdapter adapter;
	
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.poll_fragment, container, false);

        viewModel = ViewModelProviders.of(this).get(PollViewModel.class);
        setupViewModelObservers();

        creatorImageView = rootView.findViewById(R.id.poll_vote_creator_image_view);
        questionTextView = rootView.findViewById(R.id.poll_vote_question_text_view);
        creatorNameTextView = rootView.findViewById(R.id.poll_vote_creator_name_text_view);
        answersCountTextView = rootView.findViewById(R.id.poll_vote_answers_count_text_view);

        adapter = new PollOptionAdapter(this);

        RecyclerView listView = rootView.findViewById(R.id.poll_fragment_list_view);
        listView.setAdapter(adapter);

        Poll poll = Parcels.unwrap(getArguments().getParcelable(Constants.ACTIVITY_EXTRA_POLL));
        viewModel.setPoll(poll);

		return rootView;
	}

	private void setupViewModelObservers() {
	    viewModel.poll.observe(this, poll -> {
	        if (poll != null) {
                questionTextView.setText(poll.question);

                populatePollOptions();
            }
        });

	    viewModel.pollCreator.observe(this, creator -> {
	        if (creator != null) {
                GlideApp.with(this)
                        .load(creator.profilePhotoURL)
                        .placeholder(R.color.lightGrey)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(creatorImageView);

                creatorNameTextView.setText(getString(R.string.poll_asked_by, creator.getFullName()));
                creatorNameTextView.setVisibility(View.VISIBLE);
            }
        });

	    viewModel.pollResults.observe(this, results -> {
	        if (results == null) return;

            int answersCount = results.size();

            answersCountTextView.setText(String.valueOf(answersCount));
            answersCountTextView.setContentDescription(getString(R.string.poll_vote_answers_content_description, answersCount));

            populatePollOptions();
        });
    }

    private void populatePollOptions() {
	    Poll poll = viewModel.poll.getValue();

        if (poll != null && poll.options != null) {
            adapter.setLoading(false);
            adapter.replaceItems(poll.options);
            adapter.setPollVotingEnabled(!poll.closed);
        }
    }

    @Override
    public void onPollOptionClick(int position) {
	    viewModel.voteForPollOption(position);
    }
}
