package com.eulersbridge.isegoria.vote;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.eulersbridge.isegoria.Isegoria;
import com.eulersbridge.isegoria.common.TitledFragment;
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
            Intent intent = new Intent(Intent.ACTION_INSERT)
                    .setData(CalendarContract.Events.CONTENT_URI)
                    .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, reminder.date)

                    // Make event 1 hour long (add an hour in in milliseconds to start)
                    .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, reminder.date + 60 * 60 * 1000)
                    .putExtra(CalendarContract.Events.ALL_DAY, false)

                    .putExtra(CalendarContract.Events.TITLE, "Voting for Candidate")
                    .putExtra(CalendarContract.Events.DESCRIPTION, reminder.location)
                    .putExtra(CalendarContract.Events.EVENT_LOCATION, reminder.location)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            if (intent.resolveActivity(activity.getPackageManager()) != null) {
                activity.startActivity(intent);
            }
        });

        return rootView;
    }

    @Override
    public String getTitle(Context context) {
        return null;
    }
}