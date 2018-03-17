package com.eulersbridge.isegoria.election.candidates.positions

import android.graphics.Color
import android.graphics.Typeface
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
import androidx.os.bundleOf
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.eulersbridge.isegoria.*
import com.eulersbridge.isegoria.election.candidates.FRAGMENT_EXTRA_CANDIDATE_POSITION
import com.eulersbridge.isegoria.network.api.models.Candidate
import com.eulersbridge.isegoria.network.api.models.Position
import com.eulersbridge.isegoria.profile.ProfileOverviewFragment
import kotlinx.android.synthetic.main.candidate_position_fragment.*

class CandidatePositionFragment : Fragment() {
    private var dpWidth: Float = 0.toFloat()

    private var app: IsegoriaApp? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.candidate_position_fragment, container, false)

        val position =
            arguments?.getParcelable<Position>(FRAGMENT_EXTRA_CANDIDATE_POSITION)

        activity?.resources?.displayMetrics?.let {
            dpWidth = it.widthPixels / it.density
        }

        //addTableRow(R.drawable.head1, "GRN", "#4FBE3E", "Lillian Adams", "President");

        app = activity?.application as IsegoriaApp

        position?.id?.let { positionId ->
            app?.api?.getPositionCandidates(positionId)?.onSuccess { candidates ->
                addCandidates(candidates)
            }
        }

        return rootView
    }

    private fun addCandidates(candidates: List<Candidate>?) {
        activity?.runOnUiThread {
            candidates?.forEach { addTableRow(it) }
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
        val imageSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            53.toFloat(), resources.displayMetrics
        ).toInt()

        val rowParams = TableRow.LayoutParams(
            TableRow.LayoutParams.MATCH_PARENT,
            TableRow.LayoutParams.WRAP_CONTENT
        )
        tr.layoutParams = rowParams
        tr.setPadding(0, paddingMargin, 0, paddingMargin)

        val candidateProfileView = ImageView(activity)
        candidateProfileView.layoutParams = TableRow.LayoutParams(imageSize, imageSize)
        candidateProfileView.scaleType = ScaleType.CENTER_CROP
        //candidateProfileView.setImageBitmap(decodeSampledBitmapFromResource(getResources(), profileDrawable, imageSize, imageSize));
        candidateProfileView.setPadding(paddingMargin, 0, paddingMargin, 0)

        app?.api?.getPhoto(candidate.userId)?.onSuccess {
            GlideApp.with(this@CandidatePositionFragment)
                .load(it.thumbnailUrl)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(candidateProfileView)
        }

        val candidateProfileImage = ImageView(activity)
        candidateProfileImage.apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                Gravity.END.toFloat()
            )

            scaleType = ScaleType.CENTER_CROP
            setImageResource(R.drawable.profile_light)
            setPadding(paddingMargin, 0, paddingMargin, 0)
            setOnClickListener {

                val profileOverviewFragment = ProfileOverviewFragment()
                profileOverviewFragment.arguments = bundleOf(FRAGMENT_EXTRA_PROFILE_ID to candidate.userId)

                childFragmentManager
                    .beginTransaction()
                    .addToBackStack(null)
                    .replace(android.R.id.content, profileOverviewFragment)
                    .commit()
            }
        }

        val textViewParty = TextView(activity)
        textViewParty.apply {
            setTextColor(Color.parseColor("#FFFFFF"))
            textSize = 12.0f
            text = ""
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            gravity = Gravity.CENTER
            setTypeface(null, Typeface.BOLD)
        }

        app?.api?.getTicket(candidate.ticketId)?.onSuccess {
            textViewParty.text = it.code
            textViewParty.setBackgroundColor(Color.parseColor(it.getColour()))
        }

        val imageSize2 = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            27.toFloat(), resources.displayMetrics
        ).toInt()

        val partyLayout = LinearLayout(activity)
        partyLayout.orientation = LinearLayout.VERTICAL
        val params = LinearLayout.LayoutParams(
            imageSize, imageSize2
        )
        params.gravity = Gravity.CENTER_VERTICAL
        partyLayout.layoutParams = params
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
            setPadding(paddingMargin, 0, paddingMargin, 0)
            gravity = Gravity.START
        }

        app?.api?.getPosition(candidate.positionId)?.onSuccess {
            textViewPosition.text = it.name
        }

        val dividerView = View(activity)
        dividerView.layoutParams = TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, 1)
        dividerView.setBackgroundColor(Color.parseColor("#676475"))

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
        linLayout.orientation = LinearLayout.VERTICAL

        linLayout.addView(textViewCandidate)
        linLayout.addView(textViewPosition)

        val linLayout2 = LinearLayout(activity)
        linLayout2.orientation = LinearLayout.VERTICAL

        linLayout2.addView(candidateProfileImage)
        linLayout2.gravity = Gravity.END
        linLayout2.layoutParams = relativeParamsRight

        layout.addView(candidateProfileView)
        layout.addView(partyLayout)
        layout.addView(linLayout)
        layout.layoutParams = relativeParamsLeft

        relLayoutMaster.addView(layout)
        relLayoutMaster.addView(linLayout2)

        tr.addView(relLayoutMaster)

        candidatePositionTable.addView(tr)
        candidatePositionTable.addView(dividerView)
    }
}
