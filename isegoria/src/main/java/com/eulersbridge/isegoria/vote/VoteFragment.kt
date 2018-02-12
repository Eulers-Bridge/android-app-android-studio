package com.eulersbridge.isegoria.vote

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.format.DateFormat
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import com.eulersbridge.isegoria.R
import com.eulersbridge.isegoria.network.api.models.VoteLocation
import com.eulersbridge.isegoria.observe
import com.eulersbridge.isegoria.util.ui.TitledFragment
import kotlinx.android.synthetic.main.vote_fragment.*
import java.util.*
import java.util.concurrent.TimeUnit

class VoteFragment : Fragment(), TitledFragment {

    companion object {
        private val MILLIS_PER_DAY = TimeUnit.DAYS.toMillis(1)
    }

    private lateinit var voteLocationArrayAdapter: ArrayAdapter<VoteLocation>

    private var openDialog: Dialog? = null
    private lateinit var viewModel: VoteViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.vote_fragment, container, false)

        viewModel = ViewModelProviders.of(parentFragment!!).get(VoteViewModel::class.java)
        createViewModelObservers()

        voteLocationArrayAdapter = ArrayAdapter(context!!, R.layout.spinner_layout)
        voteLocationArrayAdapter.setDropDownViewResource(R.layout.spinner_layout)

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        voteLocationSpinner.adapter = voteLocationArrayAdapter
        voteLocationSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                adapterView: AdapterView<*>,
                view: View,
                index: Int,
                id: Long
            ) {
                viewModel.onVoteLocationChanged(index)
            }

            override fun onNothingSelected(adapterView: AdapterView<*>) {}
        }

        timeTextView.isEnabled = false

        timeTextView.setOnClickListener {
            if (!it.isEnabled) {
                viewModel.dateTime.value?.let { calendar ->
                    openDialog = TimePickerDialog(
                        context,
                        { _, hourOfDay, minute ->

                            viewModel.dateTime.value?.also {
                                it.set(Calendar.HOUR_OF_DAY, hourOfDay)
                                it.set(Calendar.MINUTE, minute)

                                viewModel.dateTime.value = it
                            }
                        },
                        calendar.get(Calendar.HOUR_OF_DAY),
                        calendar.get(Calendar.MINUTE),
                        DateFormat.is24HourFormat(context)
                    )
                    openDialog?.show()
                }
            }
        }

        dateTextView.isEnabled = false

        dateTextView.setOnClickListener {
            if (!it.isEnabled) {
                viewModel.dateTime.value?.let { calendar ->
                    val datePickerDialog = DatePickerDialog(
                        context!!,
                        { _, year, monthOfYear, dayOfMonth ->

                            viewModel.dateTime.value?.also {
                                it.set(Calendar.YEAR, year)
                                it.set(Calendar.MONTH, monthOfYear)
                                it.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                                viewModel.dateTime.value = it
                            }
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    )

                    val election = viewModel.electionData?.value?.takeIf { it.startVoting < it.endVoting }
                    if (election != null) {
                        datePickerDialog.datePicker.minDate = election.startVoting
                        datePickerDialog.datePicker.maxDate = election.endVoting
                    }

                    openDialog = datePickerDialog
                    openDialog?.show()
                }
            }
        }

        completeButton.setOnClickListener { viewModel.locationAndDateComplete.value = true }
    }

    private fun createViewModelObservers() {
        observe(viewModel.dateTime) {
            it?.let {
                updateDateLabel(dateTextView, it)
                updateTimeLabel(timeTextView, it)
            }
        }

        observe(viewModel.getVoteLocations()) { locations ->
            if (locations != null)
                voteLocationArrayAdapter.addAll(locations)
        }

        observe(viewModel.getElection()) {
            dateTextView.isEnabled = true
            timeTextView.isEnabled = true
        }
    }

    override fun getTitle(context: Context?) = context?.getString(R.string.vote_tab_1)

    private fun updateDateLabel(label: EditText, calendar: Calendar) {

        val calMillis = calendar.timeInMillis
        val dateStr: String

        if (DateUtils.isToday(calMillis)) {
            dateStr = "Today"

        } else if (DateUtils.isToday(calMillis - MILLIS_PER_DAY)) {
            dateStr = "Tomorrow"

        } else {
            val todayCal = Calendar.getInstance()

            if (calendar.get(Calendar.YEAR) == todayCal.get(Calendar.YEAR)) {

                dateStr = if (calendar.get(Calendar.WEEK_OF_YEAR) == todayCal.get(Calendar.WEEK_OF_YEAR) && calendar.get(
                        Calendar.ERA
                    ) == todayCal.get(Calendar.ERA)) {

                    //If this week, use name of day of week (eg. Friday)
                    calendar.getDisplayName(
                        Calendar.DAY_OF_WEEK,
                        Calendar.LONG,
                        Locale.getDefault()
                    )

                } else {
                    val flags = DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_NO_YEAR
                    DateUtils.formatDateTime(context, calMillis, flags)
                }

            } else {
                dateStr = DateUtils.formatDateTime(context, calMillis, DateUtils.FORMAT_SHOW_DATE)
            }
        }

        label.setText(dateStr)
    }

    private fun updateTimeLabel(label: EditText, calendar: Calendar) {
        val formatter = android.text.format.DateFormat.getTimeFormat(context)
        val timeStr = formatter.format(calendar.time)

        label.setText(timeStr)
    }
}