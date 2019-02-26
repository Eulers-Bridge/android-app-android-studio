package com.eulersbridge.isegoria.election.candidates

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RectShape
import android.os.Bundle
import android.support.design.widget.TabLayout
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
import androidx.core.view.isGone
import com.bumptech.glide.Priority
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.eulersbridge.isegoria.*
import com.eulersbridge.isegoria.data.Repository
import com.eulersbridge.isegoria.network.api.model.Candidate
import com.eulersbridge.isegoria.profile.ProfileOverviewFragment
import com.eulersbridge.isegoria.util.extension.runOnUiThread
import com.eulersbridge.isegoria.util.extension.subscribeSuccess
import com.eulersbridge.isegoria.util.extension.toBooleanSingle
import com.eulersbridge.isegoria.util.transformation.BlurTransformation
import com.eulersbridge.isegoria.util.ui.TabbedFragment
import com.eulersbridge.isegoria.util.ui.TitledFragment
import dagger.android.support.AndroidSupportInjection
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.candidate_ticket_detail_fragment.*
import kotlinx.android.synthetic.main.candidate_ticket_detail_fragment.view.*
import javax.inject.Inject

/**
 * This fragment shows the profile for a ticket (party).
 */

class CandidateTicketDetailFragment : Fragment(), TabbedFragment, TitledFragment {

    private var dpWidth: Float = 0.toFloat()

    private var ticketId: Long = 0
    private val code = ""
    private val colour = "#000000"
    private var partyColour: String? = ""
    private var partyLogo: String? = ""

    @Inject
    internal lateinit var app: IsegoriaApp

    @Inject
    internal lateinit var repository: Repository

    private val compositeDisposable = CompositeDisposable()

    override fun onAttach(context: Context?) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
            = inflater.inflate(R.layout.candidate_ticket_detail_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        var ticketName: String? = null
        var noOfSupporters: Int? = null

        arguments?.let {
            ticketId = it.getLong("TicketId")
            ticketName = it.getString("TicketName")
            noOfSupporters = it.getInt("NoOfSupporters")
            partyColour = it.getString("Colour")
            partyLogo = it.getString("Logo")
        }

        activity?.resources?.displayMetrics?.let {
            dpWidth = it.widthPixels / it.density
        }

        GlideApp.with(this)
            .load(R.drawable.birmingham)
            .transforms(CenterCrop(), BlurTransformation(context!!))
            .priority(Priority.HIGH)
            .into(object : SimpleTarget<Drawable>() {
                override fun onResourceReady(
                    resource: Drawable,
                    transition: Transition<in Drawable>?
                ) {
                    if (isAdded && !isDetached)
                        backgroundLayout.post { backgroundLayout.background = resource }
                }

            })

        repository.getTicketCandidates(ticketId)
                .subscribeSuccess { addCandidates(it) }
                .addTo(compositeDisposable)

        repository.getPhotos(ticketId)
                .subscribeSuccess {
                    it.photos.firstOrNull()?.let {
                        GlideApp.with(this@CandidateTicketDetailFragment)
                            .load(it.getPhotoUrl())
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .into(partyDetailLogoImageView)
                    }
                }
                .addTo(compositeDisposable)

        repository.getUserSupportedTicket(ticketId)
                .subscribeSuccess {
                    runOnUiThread {
                        ticketSupportButton.text = getString(R.string.candidate_ticket_detail_button_unsupport)
                    }
                }
                .addTo(compositeDisposable)

        ticketSupportButton.setOnClickListener {

            val supportStr = getString(R.string.candidate_ticket_detail_button_support)
            val unsupportStr = getString(R.string.candidate_ticket_detail_button_unsupport)

            if (ticketSupportButton.text == supportStr) {

                repository.supportTicket(ticketId)
                        .toBooleanSingle()
                        .subscribe()
                        .addTo(compositeDisposable)

                val value = partyDetailSupporters.text.toString()
                partyDetailSupporters.text = (Integer.parseInt(value) + 1).toString()
                ticketSupportButton.text = unsupportStr

            } else if (ticketSupportButton.text == unsupportStr) {

                repository.unsupportTicket(ticketId)
                        .toBooleanSingle()
                        .subscribe()
                        .addTo(compositeDisposable)

                val value = partyDetailSupporters.text.toString()
                partyDetailSupporters.text = (Integer.parseInt(value) - 1).toString()
                ticketSupportButton.text = supportStr
            }
        }

        partyDetailSupporters.text = noOfSupporters?.toString()
        backgroundLayout.partyNameTextView.text = ticketName
    }

    override fun onPause() {
        compositeDisposable.dispose()
        super.onPause()
    }

    private fun addCandidates(candidates: List<Candidate>) {
        if (candidates.isNotEmpty()) {
            runOnUiThread {
                for (candidate in candidates)
                    addTableRow(candidate)
            }
        }
    }

    private fun addTableRow(candidate: Candidate) {
        val tr = TableRow(activity)

        val paddingMargin3 = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            6.666666667.toFloat(), resources.displayMetrics
        ).toInt()

        val layout = LinearLayout(activity)
        layout.orientation = LinearLayout.HORIZONTAL

        val rowParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        tr.layoutParams = rowParams
        tr.setPadding(0, paddingMargin3, 0, paddingMargin3)
        val imageHeight = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            53.33333333.toFloat(), resources.displayMetrics
        ).toInt()

        val candidateProfileView = ImageView(activity)
        candidateProfileView.apply {
            layoutParams = LayoutParams(imageHeight, imageHeight)
            scaleType = ScaleType.CENTER_CROP
            //setImageBitmap(decodeSampledBitmapFromResource(resources, candidate.userId, imageHeight, imageHeight));
            setPadding(paddingMargin3, 0, paddingMargin3, 0)
        }

        repository.getPhotos(candidate.userId)
                .subscribeSuccess {
                    it.photos.firstOrNull()?.let {
                        GlideApp.with(this@CandidateTicketDetailFragment)
                            .load(it.getPhotoUrl())
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .into(candidateProfileView)
                    }
                }
                .addTo(compositeDisposable)

        val candidateProfileImage = ImageView(activity)
        candidateProfileImage.apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT,
                Gravity.END.toFloat()
            )

            scaleType = ScaleType.CENTER_CROP
            setImageResource(R.drawable.profile_light)
            setPadding(paddingMargin3, 0, paddingMargin3, 0)

            setOnClickListener {

                val profileFragment = ProfileOverviewFragment()
                profileFragment.arguments = bundleOf(FRAGMENT_EXTRA_PROFILE_ID to candidate.userId)


                (activity as MainActivity).presentContent(profileFragment)
            }
        }

        val textViewParty = TextView(activity)
        textViewParty.apply {
            setTextColor(Color.parseColor("#FFFFFF"))
            setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12.0f)
            text = partyLogo
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            gravity = Gravity.CENTER
            setTypeface(null, Typeface.BOLD)
        }

        val rect = RectShape()
        val rectShapeDrawable = ShapeDrawable(rect)
        val paint = rectShapeDrawable.paint
        paint.apply {
            color = Color.parseColor(partyColour)
            style = Paint.Style.FILL_AND_STROKE
            strokeWidth = 5f
        }

        val partyLayout = LinearLayout(activity)
        partyLayout.apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                imageHeight, 40
            )
            background = rectShapeDrawable
            addView(textViewParty)
        }

        val textViewCandidate = TextView(activity)
        textViewCandidate.apply {
            setTextColor(Color.parseColor("#3A3F43"))
            setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16.0f)
            text = candidate.name
            setPadding(paddingMargin3, 0, paddingMargin3, 0)
            gravity = Gravity.START
        }

        val textViewPosition = TextView(activity)
        textViewPosition.apply {
            setTextColor(Color.parseColor("#3A3F43"))
            setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12.0f)
            setPadding(paddingMargin3, 0, paddingMargin3, 0)
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
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, 1)
            setBackgroundColor(Color.parseColor("#676475"))
        }

        val relLayoutMaster = RelativeLayout(activity)
        val relLayoutMasterParam = LayoutParams(dpWidth.toInt(), LayoutParams.WRAP_CONTENT)
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

        tableLayout.addView(tr)
        tableLayout.addView(dividerView)
    }

    override fun getTitle(context: Context?): String? = "Ticket Profile"

    override fun setupTabLayout(tabLayout: TabLayout) {
        tabLayout.apply {
            isGone = true
        }
    }
}