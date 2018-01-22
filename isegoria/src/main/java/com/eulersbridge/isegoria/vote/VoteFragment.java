package com.eulersbridge.isegoria.vote;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.arch.lifecycle.ViewModelProviders;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.network.api.models.Election;
import com.eulersbridge.isegoria.network.api.models.VoteLocation;
import com.eulersbridge.isegoria.util.ui.TitledFragment;

import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class VoteFragment extends Fragment implements TitledFragment {

    private ArrayAdapter<VoteLocation> voteLocationArrayAdapter;

    private EditText timeField;
    private EditText dateField;

    private Dialog openDialog;

    private VoteViewModel viewModel;

    @Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.vote_fragment, container, false);

        //noinspection ConstantConditions
        viewModel = ViewModelProviders.of(getParentFragment()).get(VoteViewModel.class);
        setupModelObservers();

        Spinner spinnerLocation = rootView.findViewById(R.id.vote_location);

        voteLocationArrayAdapter = new ArrayAdapter<>(getContext(), R.layout.spinner_layout);
        voteLocationArrayAdapter.setDropDownViewResource(R.layout.spinner_layout);

        spinnerLocation.setAdapter(voteLocationArrayAdapter);
        spinnerLocation.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int index, long id) {
                viewModel.onVoteLocationChanged(index);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        timeField = rootView.findViewById(R.id.vote_time);
        timeField.setEnabled(false);

        timeField.setOnClickListener(view -> {
            if (!view.isEnabled()) return;

            Calendar calendar = viewModel.dateTime.getValue();
            if (calendar == null) return;

            openDialog = new TimePickerDialog(getContext(),
                    (view1, hourOfDay, minute) -> {

                        Calendar updatedCalendar = viewModel.dateTime.getValue();

                        updatedCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        updatedCalendar.set(Calendar.MINUTE, minute);

                        viewModel.dateTime.setValue(updatedCalendar);
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

            Calendar calendar = viewModel.dateTime.getValue();
            if (calendar == null) return;

            final DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                    (datePicker, year, monthOfYear, dayOfMonth) -> {

                        Calendar updatedCalendar = viewModel.dateTime.getValue();

                        updatedCalendar.set(Calendar.YEAR, year);
                        updatedCalendar.set(Calendar.MONTH, monthOfYear);
                        updatedCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                        viewModel.dateTime.setValue(updatedCalendar);
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));

            Election election = viewModel.getElection().getValue();
            if (election != null && election.startVotingTimestamp < election.endVotingTimestamp) {
                datePickerDialog.getDatePicker().setMinDate(election.startVotingTimestamp);
                datePickerDialog.getDatePicker().setMaxDate(election.endVotingTimestamp);
            }

            openDialog = datePickerDialog;
            openDialog.show();
        });

        Button voteOkButton = rootView.findViewById(R.id.vote_ok_button);
        voteOkButton.setOnClickListener(view -> viewModel.locationAndDateComplete.setValue(true));

		return rootView;
	}

    private void setupModelObservers() {
        viewModel.dateTime.observe(this, calendar -> {
            updateDateLabel(dateField, calendar);
            updateTimeLabel(timeField, calendar);
        });

        viewModel.getVoteLocations().observe(this, locations -> {
            if (locations != null)
                voteLocationArrayAdapter.addAll(locations);
        });

        viewModel.getElection().observe(this, election -> {
            dateField.setEnabled(true);
            timeField.setEnabled(true);
        });
    }

    @Nullable
    @Override
    public String getTitle(Context context) {
        return context.getString(R.string.vote_tab_1);
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
}