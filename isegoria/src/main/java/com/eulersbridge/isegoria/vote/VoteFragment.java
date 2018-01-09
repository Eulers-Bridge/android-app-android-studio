package com.eulersbridge.isegoria.vote;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.eulersbridge.isegoria.Isegoria;
import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.common.TitledFragment;
import com.eulersbridge.isegoria.models.Election;
import com.eulersbridge.isegoria.models.User;
import com.eulersbridge.isegoria.models.VoteLocation;
import com.eulersbridge.isegoria.network.SimpleCallback;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import retrofit2.Response;

public class VoteFragment extends Fragment implements TitledFragment {

    interface VoteFragmentListener {
        /**
         * Called once the user has selected a location and date/time,
         * and clicked the button to proceed.
         */
        void onComplete(VoteLocation voteLocation, Calendar dateTime);
    }

    private VoteFragmentListener listener;

    private ArrayAdapter<VoteLocation> voteLocationArrayAdapter;

    private Spinner spinnerLocation;

    private final Calendar calendar = Calendar.getInstance();
    private EditText timeField;
    private EditText dateField;

    private Dialog openDialog;

    private Election election;

    @Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.vote_fragment, container, false);
		
        spinnerLocation = rootView.findViewById(R.id.vote_location);

        voteLocationArrayAdapter = new ArrayAdapter<>(getContext(), R.layout.spinner_layout);
        voteLocationArrayAdapter.setDropDownViewResource(R.layout.spinner_layout);

        spinnerLocation.setAdapter(voteLocationArrayAdapter);

        timeField = rootView.findViewById(R.id.vote_time);
        timeField.setEnabled(false);

        timeField.setOnClickListener(view -> {
            if (!view.isEnabled()) return;

            openDialog = new TimePickerDialog(getContext(),
                    (view1, hourOfDay, minute) -> {
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        calendar.set(Calendar.MINUTE, minute);
                        updateTimeLabel(timeField, calendar);
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    DateFormat.is24HourFormat(getContext()));
            openDialog.show();
        });

        dateField = rootView.findViewById(R.id.vote_date);
        dateField.setEnabled(false);

        dateField.setOnClickListener(view -> {
            if (!view.isEnabled()) return;

            final DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                    (datePicker, year, monthOfYear, dayOfMonth) -> {
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, monthOfYear);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        updateDateLabel(dateField, calendar);
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));

            if (election != null && election.startVotingTimestamp < election.endVotingTimestamp) {
                datePickerDialog.getDatePicker().setMinDate(election.startVotingTimestamp);
                datePickerDialog.getDatePicker().setMaxDate(election.endVotingTimestamp);
            }

            openDialog = datePickerDialog;
            openDialog.show();
        });

        Button voteOkButton = rootView.findViewById(R.id.vote_ok_button);
        voteOkButton.setOnClickListener(view -> {
            if (listener != null)
                listener.onComplete((VoteLocation)spinnerLocation.getSelectedItem(), calendar);
        });

        Isegoria isegoria = (Isegoria) getActivity().getApplication();

        if (isegoria != null) {
            User user = isegoria.getLoggedInUser();

            if (user != null && user.institutionId != null) {
                isegoria.getAPI().getVoteLocations(user.institutionId).enqueue(new SimpleCallback<List<VoteLocation>>() {
                    @Override
                    protected void handleResponse(Response<List<VoteLocation>> response) {
                        List<VoteLocation> locations = response.body();
                        if (locations != null) {
                            for (VoteLocation voteLocation : locations) {
                                voteLocationArrayAdapter.add(voteLocation);
                            }
                        }
                    }
                });

                isegoria.getAPI().getElections(user.institutionId).enqueue(new SimpleCallback<List<Election>>() {
                    @Override
                    protected void handleResponse(Response<List<Election>> response) {
                        List<Election> elections = response.body();
                        if (elections != null && elections.size() > 0)
                            setElection(elections.get(0));
                    }
                });
            }
        }

		return rootView;
	}

    @Nullable
    @Override
    public String getTitle(Context context) {
        return context.getString(R.string.vote_tab_1);
    }

	private void setElection(@NonNull Election election) {
        this.election = election;

        timeField.setEnabled(true);
        dateField.setEnabled(true);

        calendar.setTimeInMillis(election.startVotingTimestamp);
    }

    private static final long MILLIS_PER_DAY = TimeUnit.DAYS.toMillis(1);

	private void updateDateLabel(EditText label, Calendar calendar) {

	    final long calMillis = calendar.getTimeInMillis();
        String dateStr;

        if (DateUtils.isToday(calMillis)) {
            dateStr = "Today";

        } else if (DateUtils.isToday(calMillis - MILLIS_PER_DAY)) {
            dateStr = "Tomorrow";

        } else {

            final Calendar todayCal = Calendar.getInstance();

            if (calendar.get(Calendar.YEAR) == todayCal.get(Calendar.YEAR)) {

                if (calendar.get(Calendar.WEEK_OF_YEAR) == todayCal.get(Calendar.WEEK_OF_YEAR)
                        && calendar.get(Calendar.ERA) == todayCal.get(Calendar.ERA)) {

                    //If this week, use name of day of week (eg. Friday)
                    dateStr = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault());

                } else {
                    final int flags = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_NO_YEAR;
                    dateStr = DateUtils.formatDateTime(getContext(), calMillis, flags);
                }

            } else {
                dateStr = DateUtils.formatDateTime(getContext(), calMillis, DateUtils.FORMAT_SHOW_DATE);
            }
        }

        label.setText(dateStr);
    }

    private void updateTimeLabel(EditText label, Calendar calendar) {
        final java.text.DateFormat formatter = android.text.format.DateFormat.getTimeFormat(getContext());
        final String timeStr = formatter.format(calendar.getTime());

        label.setText(timeStr);
    }

    public void setListener(VoteFragmentListener listener) {
        this.listener = listener;
    }
}