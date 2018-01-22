package com.eulersbridge.isegoria.vote;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.util.ui.TitledFragment;

public class VotePledgeFragment extends Fragment implements TitledFragment {

    private VoteViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.vote_fragment_pledge, container, false);

        //noinspection ConstantConditions
        viewModel = ViewModelProviders.of(getParentFragment()).get(VoteViewModel.class);

        Button voteNextButton = rootView.findViewById(R.id.vote_pledge_next_button);
        voteNextButton.setOnClickListener(view -> {
            LiveData data = viewModel.setPledgeComplete();
        });

        return rootView;
    }

    @Override
    public String getTitle(Context context) {
        return context.getString(R.string.vote_tab_2);
    }
}