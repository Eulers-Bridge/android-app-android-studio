package com.eulersbridge.isegoria.vote;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.eulersbridge.isegoria.Isegoria;
import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.common.TitledFragment;
import com.eulersbridge.isegoria.models.User;
import com.eulersbridge.isegoria.models.VoteReminder;
import com.eulersbridge.isegoria.network.SimpleCallback;

import java.util.List;

import retrofit2.Response;

public class VoteDoneFragment extends Fragment implements TitledFragment {

    private Button addToCalendarButton;
    private VoteReminder reminder;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.vote_fragment_done, container, false);

        addToCalendarButton = rootView.findViewById(R.id.vote_done_button_add_to_calendar);
        addToCalendarButton.setEnabled(false);
        addToCalendarButton.setOnClickListener(view -> addToCalendar());

        fetchReminders();

        return rootView;
    }

    private void fetchReminders() {
        if (getActivity() == null) return;

        Isegoria isegoria = (Isegoria)getActivity().getApplication();

        if (isegoria != null) {
            User user = isegoria.getLoggedInUser();

            if (user != null && !TextUtils.isEmpty(user.email)) {
                isegoria.getAPI().getVoteReminders(user.email).enqueue(new SimpleCallback<List<VoteReminder>>() {
                    @Override
                    protected void handleResponse(Response<List<VoteReminder>> response) {
                        List<VoteReminder> reminders = response.body();
                        if (reminders != null && reminders.size() > 0) {
                            reminder = reminders.get(0);

                            getActivity().runOnUiThread(() -> addToCalendarButton.setEnabled(true));
                        }
                    }
                });
            }
        }
    }

    private void addToCalendar() {
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

        if (getActivity() != null && intent.resolveActivity(getActivity().getPackageManager()) != null) {
            getActivity().startActivity(intent);
        }
    }

    @Override
    public String getTitle(Context context) {
        return context.getString(R.string.vote_tab_3);
    }
}