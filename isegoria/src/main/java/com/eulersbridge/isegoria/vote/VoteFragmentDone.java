package com.eulersbridge.isegoria.vote;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.eulersbridge.isegoria.Isegoria;
import com.eulersbridge.isegoria.utilities.TitledFragment;
import com.eulersbridge.isegoria.models.VoteReminder;
import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.network.SimpleCallback;

import java.util.List;

import retrofit2.Response;

public class VoteFragmentDone extends Fragment implements TitledFragment {

    private VoteReminder reminder;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.vote_fragment_done, container, false);

        //TODO: No tabs

        Button addToCalButton = rootView.findViewById(R.id.addToCalButton);
        addToCalButton.setEnabled(false);

        Activity activity = getActivity();
        Isegoria isegoria = (Isegoria)activity.getApplication();

        isegoria.getAPI().getVoteReminders(isegoria.getLoggedInUser().email).enqueue(new SimpleCallback<List<VoteReminder>>() {
            @Override
            protected void handleResponse(Response<List<VoteReminder>> response) {
                List<VoteReminder> reminders = response.body();
                if (reminders != null && reminders.size() > 0) {
                    reminder = reminders.get(0);

                    activity.runOnUiThread(() -> addToCalButton.setEnabled(true));
                }
            }
        });

        addToCalButton.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_EDIT);
            intent.setType("vnd.android.cursor.item/event");
            intent.putExtra("beginTime", reminder.date);
            intent.putExtra("allDay", false);
            intent.putExtra("endTime", reminder.date+60*60*1000);
            intent.putExtra("title", "Voting for Candidate");
            intent.putExtra("description", reminder.location);
            activity.startActivity(intent);
        });

        return rootView;
    }

    @Override
    public String getTitle() {
        return null;
    }
}