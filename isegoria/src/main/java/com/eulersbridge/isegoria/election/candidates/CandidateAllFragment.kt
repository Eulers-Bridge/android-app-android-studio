package com.eulersbridge.isegoria.election.candidates

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RectShape
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.ImageView.ScaleType
import android.widget.TableRow.LayoutParams
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.eulersbridge.isegoria.FRAGMENT_EXTRA_USER
import com.eulersbridge.isegoria.GlideApp
import com.eulersbridge.isegoria.IsegoriaApp
import com.eulersbridge.isegoria.R
import com.eulersbridge.isegoria.network.api.models.Candidate
import com.eulersbridge.isegoria.network.api.models.Election
import com.eulersbridge.isegoria.network.api.models.Position
import com.eulersbridge.isegoria.network.api.models.Ticket
import com.eulersbridge.isegoria.network.api.responses.PhotosResponse
import com.eulersbridge.isegoria.profile.ProfileOverviewFragment
import com.eulersbridge.isegoria.util.network.SimpleCallback
import kotlinx.android.synthetic.main.candidate_all_fragment.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class CandidateAllFragment : Fragment() {
    private lateinit var rootView: View

    private val firstNames = ArrayList<String>()
    private val lastNames = ArrayList<String>()
    private val rows = ArrayList<TableRow>()

    private var dpWidth: Float = 0.toFloat()

    private var candidateAllFragment: CandidateAllFragment? = null
    private var app: IsegoriaApp? = null

    private val electionsCallback = object : Callback<List<Election>> {
        override fun onResponse(call: Call<List<Election>>, response: Response<List<Election>>) {
            val elections = response.body()
            if (elections != null && elections.isNotEmpty()) {
                val (id) = elections[0]

                app!!.api.getElectionCandidates(id).enqueue(candidatesCallback)
            }
        }

        override fun onFailure(call: Call<List<Election>>, t: Throwable) = t.printStackTrace()
    }

    private val candidatesCallback = object : Callback<List<Candidate>> {
        override fun onResponse(call: Call<List<Candidate>>, response: Response<List<Candidate>>) {
            val candidates = response.body()
            if (candidates != null)
                addCandidates(candidates)
        }

        override fun onFailure(call: Call<List<Candidate>>, t: Throwable) = t.printStackTrace()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        candidateAllFragment = this
        val displayMetrics = activity!!.resources.displayMetrics

        rootView = inflater.inflate(R.layout.candidate_all_fragment, container, false)

        dpWidth = displayMetrics.widthPixels.toFloat()

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val dividerView = View(activity)
        dividerView.layoutParams = TableRow.LayoutParams(LayoutParams.MATCH_PARENT, 1)
        dividerView.setBackgroundColor(Color.parseColor("#676475"))
        candidateAllTable!!.addView(dividerView)

        searchViewCandidatesAll.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(query: String): Boolean {
                return handleSearchQueryTextChange(query)
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                return handleSearchQueryTextChange(query)
            }
        })

        app = activity!!.application as IsegoriaApp
        if (app != null) {
            app!!.loggedInUser.value?.institutionId?.let { institutionId ->
                app!!.api.getElections(institutionId).enqueue(electionsCallback)
            }
        }
    }

    private fun handleSearchQueryTextChange(query: String): Boolean {
        addAllRows()

        if (query.isNotEmpty()) {
            for (i in rows.indices) {
                val view = rows[i]

                try {
                    val firstName = firstNames[i]
                    val lastName = lastNames[i]

                    if (!firstName.toLowerCase().contains(query.toLowerCase())
                        && !lastName.toLowerCase().contains(query.toLowerCase()))
                        candidateAllTable.removeView(view)

                } catch (ignored: Exception) {
                }
            }

            rootView.invalidate()

            return true
        }
        return false
    }

    private fun addAllRows() {
        try {
            candidateAllTable.removeAllViews()

            for (row in rows)
                candidateAllTable.addView(row)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun addCandidates(candidates: List<Candidate>) {
        if (activity != null && candidates.isNotEmpty()) {
            activity!!.runOnUiThread {
                for (candidate in candidates)
                    addTableRow(candidate)
            }
        }
    }

    private fun addTableRow(candidate: Candidate) {
        val tr = TableRow(activity)

        val layout = LinearLayout(activity)
        layout.orientation = LinearLayout.HORIZONTAL

        val paddingMargin = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            6.5.toFloat(), resources.displayMetrics
        ).toInt()

        val rowParams = TableRow.LayoutParams(
            TableRow.LayoutParams.MATCH_PARENT,
            TableRow.LayoutParams.WRAP_CONTENT
        )
        tr.layoutParams = rowParams
        tr.setPadding(0, paddingMargin, 0, paddingMargin)

        val imageSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            53.toFloat(), resources.displayMetrics
        ).toInt()

        val candidateProfileView = ImageView(activity)
        candidateProfileView.apply {
            layoutParams = TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT
            )

            layoutParams = LinearLayout.LayoutParams(imageSize, imageSize)
            scaleType = ScaleType.CENTER_CROP
        }

        app!!.api.getPhotos(candidate.userId).enqueue(object : SimpleCallback<PhotosResponse>() {
            override fun handleResponse(response: Response<PhotosResponse>) {
                val body = response.body()

                if (body != null && body.totalPhotos > 0) {
                    GlideApp.with(this@CandidateAllFragment)
                        .load(body.photos?.get(0)?.thumbnailUrl)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(candidateProfileView)
                }
            }
        })

        candidateProfileView.setPadding(paddingMargin, 0, paddingMargin, 0)

        val candidateProfileImage = ImageView(activity)
        candidateProfileImage.apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                Gravity.END.toFloat()
            )

            scaleType = ScaleType.CENTER_CROP
            setImageResource(R.drawable.profilelight)
            setPadding(paddingMargin, 0, paddingMargin, 0)
        }

        candidateProfileImage.setOnClickListener {
            val args = Bundle()
            args.putParcelable(FRAGMENT_EXTRA_USER, candidate)

            val profileFragment = ProfileOverviewFragment()
            profileFragment.arguments = args

            childFragmentManager
                .beginTransaction()
                .addToBackStack(null)
                .replace(R.id.candidateFrame, profileFragment)
                .commit()
        }

        val textViewParty = TextView(activity)
        textViewParty.apply {
            setTextColor(Color.parseColor("#FFFFFF"))
            setTextSize(TypedValue.COMPLEX_UNIT_DIP, 10.0f)
            text = "GRN"
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            gravity = Gravity.CENTER
            setTypeface(null, Typeface.BOLD)
        }

        app!!.api.getTicket(candidate.ticketId).enqueue(object : SimpleCallback<Ticket>() {
            override fun handleResponse(response: Response<Ticket>) {
                val ticket = response.body()

                if (ticket != null) {
                    textViewParty.text = ticket.code
                    textViewParty.setBackgroundColor(Color.parseColor(ticket.getColour()))
                }
            }
        })

        val rect = RectShape()
        val rectShapeDrawable = ShapeDrawable(rect)
        val paint = rectShapeDrawable.paint
        paint.apply {
            color = Color.parseColor("#4FBE3E")
            style = Paint.Style.FILL_AND_STROKE
            strokeWidth = 5f
        }

        val imageSize2 = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            27.toFloat(), resources.displayMetrics
        ).toInt()

        val partyLayout = LinearLayout(activity)
        partyLayout.apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                imageSize, imageSize2
            )
        }

        //partyLayout.setBackgroundDrawable(rectShapeDrawable);
        partyLayout.addView(textViewParty)

        val textViewCandidate = TextView(activity)
        textViewCandidate.apply {
            setTextColor(Color.parseColor("#3A3F43"))
            setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14.0f)
            text = candidate.name
            setPadding(paddingMargin, 0, paddingMargin, 0)
            gravity = Gravity.START
        }

        val textViewPosition = TextView(activity)
        textViewPosition.apply {
            setTextColor(Color.parseColor("#3A3F43"))
            setTextSize(TypedValue.COMPLEX_UNIT_DIP, 11.0f)
            text = ""
            setPadding(paddingMargin, 0, paddingMargin, 0)
            gravity = Gravity.START
        }

        app!!.api.getPosition(candidate.positionId).enqueue(object : SimpleCallback<Position>() {
            override fun handleResponse(response: Response<Position>) {
                val position = response.body()
                if (position != null) {
                    textViewPosition.text = position.name
                }
            }
        })

        val dividerView = View(activity)
        dividerView.apply {
            layoutParams = TableRow.LayoutParams(LayoutParams.MATCH_PARENT, 1)
            setBackgroundColor(Color.parseColor("#676475"))
        }

        val relLayoutMaster = RelativeLayout(activity)
        val relLayoutMasterParam =
            TableRow.LayoutParams(dpWidth.toInt(), TableRow.LayoutParams.WRAP_CONTENT)
        relLayoutMaster.layoutParams = relLayoutMasterParam

        val relativeParamsLeft =
            RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        relativeParamsLeft.addRule(RelativeLayout.ALIGN_PARENT_LEFT)

        val relativeParamsRight =
            RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        relativeParamsRight.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)

        val linLayout = LinearLayout(activity)
        linLayout.apply {
            orientation = LinearLayout.VERTICAL
            addView(textViewCandidate)
            addView(textViewPosition)
        }

        val linLayout2 = LinearLayout(activity)
        linLayout2.apply {
            orientation = LinearLayout.VERTICAL
            addView(candidateProfileImage)
            gravity = Gravity.END
            layoutParams = relativeParamsRight
        }

        layout.addView(candidateProfileView)
        layout.addView(partyLayout)
        layout.addView(linLayout)
        layout.layoutParams = relativeParamsLeft

        relLayoutMaster.addView(layout)
        relLayoutMaster.addView(linLayout2)

        tr.addView(relLayoutMaster)

        candidateAllTable.addView(tr)
        candidateAllTable.addView(dividerView)

        firstNames + candidate.givenName!!
        lastNames + candidate.familyName!!

        rows + tr
    }
}
