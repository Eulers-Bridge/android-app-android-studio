package com.eulersbridge.isegoria.vote.pages

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
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
import com.eulersbridge.isegoria.network.api.model.VoteLocation
import com.eulersbridge.isegoria.util.extension.observe
import com.eulersbridge.isegoria.util.ui.TitledFragment
import com.eulersbridge.isegoria.vote.VoteViewModel
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

        voteLocationArrayAdapter = ArrayAdapter(context!!, R.layout.spinner_layout)
        voteLocationArrayAdapter.setDropDownViewResource(R.layout.spinner_layout)

        return rootView
    }

    fun setViewModel(viewModel: VoteViewModel) {
        this.viewModel = viewModel

        createViewModelObservers()
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
            if (it.isEnabled) {
                viewModel.getDateTime()?.let { calendar ->
                    openDialog = TimePickerDialog(
                        context,
                        { _, hourOfDay, minute ->

                            viewModel.getDateTime()?.also {
                                it.set(Calendar.HOUR_OF_DAY, hourOfDay)
                                it.set(Calendar.MINUTE, minute)

                                viewModel.onDateTimeChanged(it)
                                updateTimeLabel(timeTextView, it)
                            }
                        },
                        calendar[Calendar.HOUR_OF_DAY],
                        calendar[Calendar.MINUTE],
                        DateFormat.is24HourFormat(context)
                    )
                    openDialog?.show()
                }
            }
        }

        dateTextView.isEnabled = false

        dateTextView.setOnClickListener {
            if (it.isEnabled) {
                viewModel.getDateTime()?.let { calendar ->
                    val datePickerDialog = DatePickerDialog(
                        context!!,
                        { _, year, monthOfYear, dayOfMonth ->

                            viewModel.getDateTime()?.also {
                                it.set(Calendar.YEAR, year)
                                it.set(Calendar.MONTH, monthOfYear)
                                it.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                                viewModel.onDateTimeChanged(it)
                                updateDateLabel(dateTextView, it)
                            }
                        },
                        calendar[Calendar.YEAR],
                        calendar[Calendar.MONTH],
                        calendar[Calendar.DAY_OF_MONTH]
                    )

                    val election = viewModel.election?.value?.takeIf { it.startVoting < it.endVoting }
                    if (election != null) {
                        datePickerDialog.datePicker.minDate = election.startVoting
                        datePickerDialog.datePicker.maxDate = election.endVoting
                    }

                    openDialog = datePickerDialog
                    openDialog?.show()
                }
            }
        }

        completeButton.setOnClickListener { viewModel.onInitialPageComplete() }
    }

    private fun createViewModelObservers() {
        observe(viewModel.voteLocations) { locations ->
            if (locations != null)
                voteLocationArrayAdapter.addAll(locations)
        }

        observe(viewModel.election) {
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

            if (calendar[Calendar.YEAR] == todayCal[Calendar.YEAR]) {

                val isThisWeek = calendar[Calendar.WEEK_OF_YEAR] == todayCal[Calendar.WEEK_OF_YEAR]
                val isThisEra =  calendar[Calendar.ERA] == todayCal[Calendar.ERA]

                dateStr = if (isThisWeek || isThisEra) {

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