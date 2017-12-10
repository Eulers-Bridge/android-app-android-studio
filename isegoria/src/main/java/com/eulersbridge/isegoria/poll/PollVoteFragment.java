package com.eulersbridge.isegoria.poll;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.eulersbridge.isegoria.Constant;
import com.eulersbridge.isegoria.GlideApp;
import com.eulersbridge.isegoria.Isegoria;
import com.eulersbridge.isegoria.models.Contact;
import com.eulersbridge.isegoria.models.PollResult;
import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.models.Poll;
import com.eulersbridge.isegoria.models.PollOption;
import com.eulersbridge.isegoria.network.API;
import com.eulersbridge.isegoria.network.PollResultsResponse;
import com.eulersbridge.isegoria.network.SimpleCallback;

import org.parceler.Parcels;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PollVoteFragment extends Fragment implements PollOptionAdapter.PollOptionVoteListener {

    private API api;

    private ImageView creatorImageView;
    private TextView questionTextView;
    private TextView creatorNameTextView;
    private TextView answersCountTextView;

    private PollOptionAdapter adapter;

    private Poll poll;
    private List<PollOption> pollOptions;

    private Contact creator;
	
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.poll_fragment, container, false);

        creatorImageView = rootView.findViewById(R.id.poll_vote_creator_image_view);
        questionTextView = rootView.findViewById(R.id.poll_vote_question_text_view);
        creatorNameTextView = rootView.findViewById(R.id.poll_vote_creator_name_text_view);
        answersCountTextView = rootView.findViewById(R.id.poll_vote_answers_count_text_view);

        Isegoria isegoria = (Isegoria)getActivity().getApplication();
        api = isegoria.getAPI();

        adapter = new PollOptionAdapter(this);

        RecyclerView listView = rootView.findViewById(R.id.poll_fragment_list_view);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        listView.setLayoutManager(layoutManager);
        listView.setAdapter(adapter);

        poll = Parcels.unwrap(getArguments().getParcelable(Constant.ACTIVITY_EXTRA_POLL));
        pollOptions = poll.options;

        populatePollInfo();
        populatePollOptions();

		return rootView;
	}

	private void populatePollInfo() {
	    if (getActivity() != null) {
	        getActivity().runOnUiThread(() -> {
                questionTextView.setText(poll.question);

                if (poll.creator == null && !TextUtils.isEmpty(poll.creatorEmail)) {
                    api.getContact(poll.creatorEmail).enqueue(new SimpleCallback<Contact>() {
                        @Override
                        protected void handleResponse(Response<Contact> response) {
                            Contact user = response.body();
                            if (user != null) {
                                creator = user;
                                populatePollCreatorInfo();
                            }
                        }
                    });

                } else if (creator != null) {
                    populatePollCreatorInfo();
                }
            });
        }
    }

    private void populatePollCreatorInfo() {
	    if (creator != null) {
	        GlideApp.with(this)
                    .load(creator.profilePhotoURL)
                    .placeholder(R.color.grey)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(creatorImageView);

	        creatorNameTextView.setText(getString(R.string.poll_asked_by, creator.getFullName()));
	        creatorNameTextView.setVisibility(View.VISIBLE);
        }
    }

    private void populatePollOptions() {
        if (poll != null && pollOptions != null) {
            adapter.replaceItems(pollOptions);
            adapter.notifyDataSetChanged();

            adapter.setPollVotingEnabled(!poll.closed);
        }
    }

    @Override
    public void onPollOptionClick(int position) {
        PollOption option = pollOptions.get(position);

        api.answerPoll(poll.id, option.id).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                // After voting, fetch poll results and update with this data
                api.getPollResults(poll.id).enqueue(new SimpleCallback<PollResultsResponse>() {
                    @Override
                    protected void handleResponse(Response<PollResultsResponse> response) {
                        PollResultsResponse body = response.body();
                        if (body != null && body.results != null) {

                            List<PollResult> results = body.results;

                            int answersCount = results.size();

                            answersCountTextView.setText(String.valueOf(answersCount));
                            answersCountTextView.setContentDescription(getString(R.string.poll_vote_answers_content_description, answersCount));

                            for (int i = 0; i < results.size(); i++) {
                                PollOption pollOption = pollOptions.get(i);
                                pollOption.setResult(results.get(i));
                            }

                            populatePollOptions();
                        }
                    }
                });
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }
}
