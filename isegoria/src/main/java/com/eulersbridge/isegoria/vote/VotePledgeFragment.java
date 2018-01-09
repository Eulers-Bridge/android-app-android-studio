package com.eulersbridge.isegoria.vote;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.eulersbridge.isegoria.Isegoria;
import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.common.TitledFragment;
import com.eulersbridge.isegoria.models.Election;
import com.eulersbridge.isegoria.models.User;
import com.eulersbridge.isegoria.models.VoteLocation;
import com.eulersbridge.isegoria.models.VoteReminder;
import com.eulersbridge.isegoria.network.IgnoredCallback;
import com.eulersbridge.isegoria.network.SimpleCallback;

import org.parceler.Parcels;

import java.util.List;

import retrofit2.Response;

public class VotePledgeFragment extends Fragment implements TitledFragment {

    interface VotePledgeListener {
        /**
         * Called once the user has signed and clicked the button to continue.
         */
        void onComplete();
    }

    private VotePledgeListener listener;

    private VoteLocation voteLocation;
    private long dateTimeMillis;

    private long electionId;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.vote_fragment_pledge, container, false);

        Bundle arguments = getArguments();
        if (arguments != null) {
            voteLocation = Parcels.unwrap(arguments.getParcelable("voteLocation"));
            dateTimeMillis = Parcels.unwrap(arguments.getParcelable("dateTimeMillis"));
        }

        Button voteNextButton = rootView.findViewById(R.id.vote_pledge_next_button);
        voteNextButton.setOnClickListener(view -> nextAction());

        fetchElectionId();

        return rootView;
    }

    private @Nullable Isegoria getIsegoria() {
        FragmentActivity activity = getActivity();
        if (activity == null) {
            return null;

        } else {
            return (Isegoria)activity.getApplication();
        }
    }

    private void fetchElectionId() {
        Isegoria isegoria = getIsegoria();

        if (isegoria != null) {
            User user = isegoria.getLoggedInUser();

            if (user != null && user.institutionId != null) {
                isegoria.getAPI().getElections(user.institutionId).enqueue(new SimpleCallback<List<Election>>() {
                    @Override
                    protected void handleResponse(Response<List<Election>> response) {
                        List<Election> elections = response.body();
                        if (elections != null && elections.size() > 0) {
                            Election election = elections.get(0);
                            electionId = election.id;
                        }
                    }
                });
            }
        }
    }

    private void nextAction() {
        Isegoria isegoria = getIsegoria();

        if (isegoria == null || voteLocation == null) return;

        User user = isegoria.getLoggedInUser();

        if (user == null) return;

        VoteReminder reminder = new VoteReminder(user.email, electionId, voteLocation.name, dateTimeMillis);
        isegoria.getAPI().addVoteReminder(user.email, reminder).enqueue(new IgnoredCallback<>());

        if (listener != null)
            listener.onComplete();
    }

    @Override
    public String getTitle(Context context) {
        return context.getString(R.string.vote_tab_2);
    }

    public void setListener(VotePledgeListener listener) {
        this.listener = listener;
    }
}