package com.eulersbridge.isegoria.election.candidates

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.Paint.Style
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RectShape
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TableRow
import android.widget.TableRow.LayoutParams
import android.widget.TextView
import androidx.core.os.bundleOf
import com.eulersbridge.isegoria.R
import com.eulersbridge.isegoria.data.Repository
import com.eulersbridge.isegoria.network.api.model.CandidateTicket
import com.eulersbridge.isegoria.util.extension.runOnUiThread
import com.eulersbridge.isegoria.util.extension.subscribeSuccess
import dagger.android.support.AndroidSupportInjection
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.election_candidates_tickets_fragment.*
import javax.inject.Inject

/**
 * This fragment shows a grid of all the tickets (parties) in the current election.
 */
class CandidateTicketFragment : Fragment() {

    @Inject
    internal lateinit var repository: Repository

    private val compositeDisposable = CompositeDisposable()

    private var dpWidth: Float = 0.toFloat()

    private var lastTicketId: Long = 0
    private var lastName: String? = null
    private var lastNoOfSupporters: String? = null
    private var lastColour: String? = null
    private var lastInformation: String? = null
    private var lastLogo: String? = null

    private var added = false
    private var addedCounter = 0

    override fun onAttach(context: Context?) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onPause() {
        compositeDisposable.dispose()
        super.onPause()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val rootView =
            inflater.inflate(R.layout.election_candidates_tickets_fragment, container, false)

        val displayMetrics = activity!!.resources.displayMetrics
        dpWidth = displayMetrics.widthPixels / displayMetrics.density

        repository.getLatestElectionTickets()
                .subscribeSuccess { addTickets(it) }
                .addTo(compositeDisposable)

        return rootView
    }

    private fun addTickets(tickets: List<CandidateTicket>) {
        runOnUiThread {
            for (ticket in tickets) {
                addedCounter += 1

                if (added) {
                    this.addTableRow(
                        lastTicketId,
                        ticket.id,
                        lastColour, ticket.getColour(),
                        true,
                        false,
                        lastName,
                        ticket.fullName,
                        lastNoOfSupporters,
                        ticket.supportersCount,
                        lastLogo,
                        ticket.logo
                    )
                }

                lastTicketId = ticket.id
                lastName = ticket.name
                lastInformation = ticket.information
                lastNoOfSupporters = ticket.supportersCount
                lastColour = ticket.getColour()
                lastLogo = ticket.logo

                added = !added

                if (tickets.size == addedCounter && tickets.size % 2 != 0) {
                    this.addTableRowOneSquare(
                        ticket.id,
                        ticket.getColour(),
                        ticket.fullName,
                        ticket.supportersCount,
                        ticket.logo
                    )
                }
            }
        }
    }

    @SuppressLint("CommitTransaction")
    private fun addTableRow(
        lastTicketId: Long, ticketId: Long, colour1: String?,
        colour2: String, doubleCell: Boolean, lastCell: Boolean,
        title1: String?, title2: String, supporters1: String?,
        supporters2: String?, logo1: String?, logo2: String?
    ) {
        val tr: TableRow

        val paddingMargin = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            3.2.toFloat(), resources.displayMetrics
        ).toInt()
        val paddingMargin3 = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            6.666666667.toFloat(), resources.displayMetrics
        ).toInt()
        val imageHeight = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            170.toFloat(), resources.displayMetrics
        ).toInt()

        if (doubleCell) {
            tr = TableRow(activity)
            val rowParams = TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.MATCH_PARENT
            )
            tr.layoutParams = rowParams

            var relativeLayout = RelativeLayout(activity)
            relativeLayout.layoutParams = TableRow.LayoutParams((dpWidth / 2).toInt(), imageHeight)
            if (lastCell)
                (relativeLayout.layoutParams as ViewGroup.MarginLayoutParams).setMargins(
                    paddingMargin,
                    paddingMargin,
                    paddingMargin,
                    paddingMargin
                )
            else
                (relativeLayout.layoutParams as ViewGroup.MarginLayoutParams).setMargins(
                    paddingMargin,
                    paddingMargin,
                    paddingMargin,
                    0
                )

            var textViewTitle = TextView(activity)
            textViewTitle.apply {
                setTextColor(Color.parseColor("#3A3F43"))
                setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16.0f)
                text = title1
                setPadding(paddingMargin3, 0, paddingMargin3, 0)
                gravity = Gravity.CENTER
            }

            val textViewTitleSupport1 = TextView(activity)
            textViewTitleSupport1.apply {
                setTextColor(Color.parseColor(colour1))
                setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16.0f)
                text = supporters1
                setPadding(paddingMargin3, 0, paddingMargin3, 0)
                gravity = Gravity.CENTER
            }

            var params1 =
                RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
            params1.addRule(RelativeLayout.CENTER_HORIZONTAL, textViewTitle.id)
            params1.addRule(RelativeLayout.CENTER_VERTICAL, textViewTitle.id)

            val rect = RectShape()
            val rectShapeDrawable = ShapeDrawable(rect)
            val paint = rectShapeDrawable.paint

            paint.apply {
                color = Color.parseColor(colour1)
                style = Style.STROKE
                strokeWidth = paddingMargin.toFloat()
            }

            var view = View(activity)
            view.layoutParams = TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.MATCH_PARENT
            )
            view.background = rectShapeDrawable
            view.setOnClickListener {
                val args = bundleOf(
                    "TicketId" to lastTicketId,
                    "TicketName" to title1,
                    "Colour" to colour1,
                    "NoOfSupporters" to Integer.parseInt(supporters1),
                    "Logo" to logo1
                )

                val detailFragment = CandidateTicketDetailFragment()
                detailFragment.arguments = args

                activity?.supportFragmentManager?.beginTransaction()?.apply {
                    addToBackStack(null)
                    add(R.id.candidateFrame, detailFragment)
                    commit()
                }
            }

            var linLayout = LinearLayout(activity)
            linLayout.apply {
                orientation = LinearLayout.VERTICAL
                addView(textViewTitle)
                addView(textViewTitleSupport1)
            }

            relativeLayout.addView(view)
            relativeLayout.addView(linLayout, params1)
            tr.addView(relativeLayout)

            relativeLayout = RelativeLayout(activity)
            relativeLayout.layoutParams = TableRow.LayoutParams((dpWidth / 2).toInt(), imageHeight)
            if (lastCell)
                (relativeLayout.layoutParams as ViewGroup.MarginLayoutParams).setMargins(
                    0,
                    paddingMargin,
                    paddingMargin,
                    paddingMargin
                )
            else
                (relativeLayout.layoutParams as ViewGroup.MarginLayoutParams).setMargins(
                    0,
                    paddingMargin,
                    paddingMargin,
                    0
                )

            textViewTitle = TextView(activity)
            textViewTitle.apply {
                setTextColor(Color.parseColor("#3A3F43"))
                setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16.0f)
                text = title2
                setPadding(paddingMargin3, 0, paddingMargin3, 0)
                gravity = Gravity.CENTER
            }

            val textViewTitleSupport2 = TextView(activity)
            textViewTitleSupport2.apply {
                setTextColor(Color.parseColor(colour2))
                setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16.0f)
                text = supporters1
                setPadding(paddingMargin3, 0, paddingMargin3, 0)
                gravity = Gravity.CENTER
            }

            params1 = RelativeLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
            )
            params1.addRule(RelativeLayout.CENTER_HORIZONTAL, textViewTitle.id)
            params1.addRule(RelativeLayout.CENTER_VERTICAL, textViewTitle.id)

            val rect2 = RectShape()
            val rect2ShapeDrawable = ShapeDrawable(rect2)
            val paint2 = rect2ShapeDrawable.paint
            paint2.apply {
                color = Color.parseColor(colour2)
                paint2.style = Style.STROKE
                paint2.strokeWidth = paddingMargin.toFloat()
            }

            linLayout = LinearLayout(activity)
            linLayout.apply {
                orientation = LinearLayout.VERTICAL
                addView(textViewTitle)
                addView(textViewTitleSupport2)
            }

            view = View(activity)
            view.layoutParams = TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.MATCH_PARENT
            )
            view.background = rect2ShapeDrawable
            view.setOnClickListener {
                val args = bundleOf(
                    "TicketId" to ticketId,
                    "TicketName" to title2,
                    "Colour" to colour2,
                    "NoOfSupporters" to Integer.parseInt(supporters2),
                    "Logo" to logo2
                )

                val detailFragment = CandidateTicketDetailFragment()
                detailFragment.arguments = args

                activity?.supportFragmentManager?.beginTransaction()?.apply {
                    addToBackStack(null)
                    add(R.id.candidateFrame, detailFragment)
                    commit()
                }
            }

            relativeLayout.addView(view)
            relativeLayout.addView(linLayout, params1)
            tr.addView(relativeLayout)

            positionsTableLayout.addView(tr)
        } else {

            tr = TableRow(activity)
            val rowParams = TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.MATCH_PARENT
            )
            tr.layoutParams = rowParams

            val relativeLayout = RelativeLayout(activity)
            relativeLayout.layoutParams =
                    TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, imageHeight)
            (relativeLayout.layoutParams as TableRow.LayoutParams).span = 2
            if (lastCell)
                (relativeLayout.layoutParams as ViewGroup.MarginLayoutParams).setMargins(
                    paddingMargin,
                    paddingMargin,
                    paddingMargin,
                    paddingMargin
                )
            else
                (relativeLayout.layoutParams as ViewGroup.MarginLayoutParams).setMargins(
                    paddingMargin,
                    paddingMargin,
                    paddingMargin,
                    0
                )

            val view = View(activity)
            view.layoutParams =
                    TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, imageHeight)

            val textViewTitle = TextView(activity)
            textViewTitle.apply {
                setTextColor(Color.parseColor("#F8F8F8"))
                setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20.0f)
                text = title1
                gravity = Gravity.CENTER
            }

            val params1 =
                RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
            params1.addRule(RelativeLayout.CENTER_HORIZONTAL, textViewTitle.id)
            params1.addRule(RelativeLayout.CENTER_VERTICAL, textViewTitle.id)

            val params2 =
                RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
            params2.addRule(RelativeLayout.CENTER_HORIZONTAL, textViewTitle.id)
            params2.addRule(RelativeLayout.CENTER_VERTICAL, textViewTitle.id)

            relativeLayout.addView(view)
            relativeLayout.addView(textViewTitle, params1)

            tr.addView(relativeLayout)
            positionsTableLayout.addView(tr)
        }
    }

    @SuppressLint("CommitTransaction")
    private fun addTableRowOneSquare(
        ticketId: Long, colour1: String, title1: String,
        supporters1: String?, logo1: String?
    ) {
        val tr = TableRow(activity)

        val paddingMargin = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            3.2.toFloat(), resources.displayMetrics
        ).toInt()
        val paddingMargin3 = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            6.666666667.toFloat(), resources.displayMetrics
        ).toInt()
        val imageHeight = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            170.toFloat(), resources.displayMetrics
        ).toInt()

        val rowParams = TableRow.LayoutParams(
            TableRow.LayoutParams.MATCH_PARENT,
            TableRow.LayoutParams.MATCH_PARENT
        )
        tr.layoutParams = rowParams

        val relativeLayout = RelativeLayout(activity)
        relativeLayout.layoutParams = TableRow.LayoutParams((dpWidth / 2).toInt(), imageHeight)
        (relativeLayout.layoutParams as ViewGroup.MarginLayoutParams).setMargins(
            paddingMargin,
            paddingMargin,
            paddingMargin,
            0
        )

        val textViewTitle = TextView(activity)
        textViewTitle.apply {
            setTextColor(Color.parseColor("#3A3F43"))
            setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16.0f)
            text = title1
            setPadding(paddingMargin3, 0, paddingMargin3, 0)
            gravity = Gravity.CENTER
        }

        val textViewTitleSupport1 = TextView(activity)
        textViewTitleSupport1.apply {
            setTextColor(Color.parseColor(colour1))
            setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16.0f)
            text = supporters1
            setPadding(paddingMargin3, 0, paddingMargin3, 0)
            gravity = Gravity.CENTER
        }

        val params1 =
            RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        params1.addRule(RelativeLayout.CENTER_HORIZONTAL, textViewTitle.id)
        params1.addRule(RelativeLayout.CENTER_VERTICAL, textViewTitle.id)

        val rect = RectShape()
        val rectShapeDrawable = ShapeDrawable(rect)
        val paint = rectShapeDrawable.paint
        paint.apply {
            color = Color.parseColor(colour1)
            style = Style.STROKE
            strokeWidth = paddingMargin.toFloat()
        }

        val view = View(activity)
        view.layoutParams = TableRow.LayoutParams(
            TableRow.LayoutParams.MATCH_PARENT,
            TableRow.LayoutParams.MATCH_PARENT
        )
        view.background = rectShapeDrawable

        view.setOnClickListener {
            val args = bundleOf(
                "TicketId" to lastTicketId,
                "TicketName" to title1,
                "Colour" to colour1,
                "NoOfSupporters" to Integer.parseInt(supporters1),
                "Logo" to logo1
            )

            val detailFragment = CandidateTicketDetailFragment()
            detailFragment.arguments = args

            activity?.supportFragmentManager?.beginTransaction()?.apply {
                addToBackStack(null)
                add(R.id.candidateFrame, detailFragment)
                commit()
            }
        }

        val linLayout = LinearLayout(activity)
        linLayout.orientation = LinearLayout.VERTICAL
        linLayout.addView(textViewTitle)
        linLayout.addView(textViewTitleSupport1)

        relativeLayout.addView(view)
        relativeLayout.addView(linLayout, params1)
        tr.addView(relativeLayout)

        positionsTableLayout.addView(tr)
    }
}
