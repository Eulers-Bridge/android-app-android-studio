package com.eulersbridge.isegoria.election.candidates.all

import android.annotation.SuppressLint
import android.content.Context
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
import androidx.core.os.bundleOf
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.eulersbridge.isegoria.FRAGMENT_EXTRA_USER
import com.eulersbridge.isegoria.GlideApp
import com.eulersbridge.isegoria.IsegoriaApp
import com.eulersbridge.isegoria.R
import com.eulersbridge.isegoria.data.Repository
import com.eulersbridge.isegoria.network.api.model.Candidate
import com.eulersbridge.isegoria.profile.ProfileOverviewFragment
import com.eulersbridge.isegoria.util.extension.runOnUiThread
import com.eulersbridge.isegoria.util.extension.subscribeSuccess
import dagger.android.support.AndroidSupportInjection
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.candidate_all_fragment.*
import javax.inject.Inject


/**
 * This fragment shows a list of all candidates filtered by a search query.
 */
class CandidateAllFragment : Fragment() {
    private lateinit var rootView: View

    private val firstNames = mutableListOf<String>()
    private val lastNames = mutableListOf<String>()
    private val rows = mutableListOf<TableRow>()

    private var dpWidth: Float = 0.toFloat()

    @Inject
    internal lateinit var app: IsegoriaApp

    @Inject
    internal lateinit var repository: Repository

    private val compositeDisposable = CompositeDisposable()

    override fun onAttach(context: Context?) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.candidate_all_fragment, container, false)

        activity?.resources?.displayMetrics?.let {
            dpWidth = it.widthPixels.toFloat()
        }

        return rootView
    }

    override fun onPause() {
        compositeDisposable.dispose()
        super.onPause()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val dividerView = View(activity)
        dividerView.layoutParams = TableRow.LayoutParams(LayoutParams.MATCH_PARENT, 1)
        dividerView.setBackgroundColor(Color.parseColor("#676475"))
        candidateAllTable.addView(dividerView)

        searchViewCandidatesAll.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(query: String) = handleSearchQueryTextChange(query)

            override fun onQueryTextSubmit(query: String) = handleSearchQueryTextChange(query)
        })

        repository.getLatestElectionCandidates()
                .subscribeSuccess { addCandidates(it) }
                .addTo(compositeDisposable)
    }

    private fun handleSearchQueryTextChange(query: String): Boolean {
        addAllRows()

        if (query.isNotEmpty()) {
            for (i in rows.indices) {
                val view = rows[i]

                val firstName = firstNames[i]
                val lastName = lastNames[i]

                if (!firstName.contains(query.toLowerCase(), true)
                        && !lastName.contains(query.toLowerCase(), true))
                    candidateAllTable.removeView(view)
            }

            rootView.invalidate()

            return true
        }

        return false
    }

    private fun addAllRows() {
        candidateAllTable.removeAllViews()

        for (row in rows)
            candidateAllTable.addView(row)
    }

    private fun addCandidates(candidates: List<Candidate>) {
        runOnUiThread {
            for (candidate in candidates)
                addTableRow(candidate)
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

        repository.getPhotos(candidate.userId)
                .subscribeSuccess {
                    it.photos.firstOrNull()?.let {
                        GlideApp.with(this@CandidateAllFragment)
                                .load(it.getPhotoUrl())
                                .transform(CircleCrop())
                                .transition(DrawableTransitionOptions.withCrossFade())
                                .into(candidateProfileView)
                    }
                }
                .addTo(compositeDisposable)

        repository.getPhotos(candidate.userId)
                .subscribeSuccess {
                    it.photos.firstOrNull()?.let {
                        GlideApp.with(this@CandidateAllFragment)
                                .load(it.getPhotoUrl())
                                .transform(CircleCrop())
                                .transition(DrawableTransitionOptions.withCrossFade())
                                .into(candidateProfileView)
                    }
                }
                .addTo(compositeDisposable)

        candidateProfileView.setPadding(paddingMargin, 0, paddingMargin, 0)

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
        }

        candidateProfileImage.setOnClickListener {
            val profileFragment = ProfileOverviewFragment()
            profileFragment.arguments = bundleOf(FRAGMENT_EXTRA_USER to candidate)

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

            @SuppressLint("SetTextI18n")
            text = "GRN"

            layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
            )
            gravity = Gravity.CENTER
            setTypeface(null, Typeface.BOLD)
        }

        if (candidate.ticketId != null) {
            repository.getTicket(candidate.ticketId)
                    .subscribeSuccess {
                        it.value?.let { ticket ->
                            runOnUiThread {
                                textViewParty.text = ticket.code
                                textViewParty.setBackgroundColor(Color.parseColor(ticket.getColour()))
                            }
                        }
                    }
                    .addTo(compositeDisposable)
        } else {
            textViewParty.text = "IND"
            textViewParty.setBackgroundColor(Color.BLACK)
        }

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

        repository.getPosition(candidate.positionId)
                .subscribeSuccess {
                    runOnUiThread {
                        textViewPosition.text = it.value?.name
                    }
                }
                .addTo(compositeDisposable)

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

        layout.apply {
            addView(candidateProfileView)
            addView(partyLayout)
            addView(linLayout)
            layoutParams = relativeParamsLeft
        }

        relLayoutMaster.addView(layout)
        relLayoutMaster.addView(linLayout2)

        tr.addView(relLayoutMaster)

        candidateAllTable.addView(tr)
        candidateAllTable.addView(dividerView)

        firstNames += candidate.givenName!!
        lastNames += candidate.givenName!!
        rows += tr
    }
}
