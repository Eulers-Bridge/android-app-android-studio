package com.eulersbridge.isegoria.election.candidates.ticket

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.util.Log
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
import com.eulersbridge.isegoria.election.candidates.profile.CandidateProfileFragment
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
    private var partyCode: String? = ""
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
            partyCode = it.getString("Code")
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


        GlideApp.with(this@CandidateTicketDetailFragment)
                .load(partyLogo)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(partyDetailLogoImageView)

        repository.getUserSupportedTicket(ticketId)
                .subscribeSuccess {userSupportsTicket ->
                    runOnUiThread {
                        ticketSupportButton.text =
                            if (userSupportsTicket)
                                getString(R.string.candidate_ticket_detail_button_supporting)
                            else
                                getString(R.string.candidate_ticket_detail_button_not_supporting)

                    }
                }
                .addTo(compositeDisposable)

        ticketSupportButton.setOnClickListener {

            val supportStr = getString(R.string.candidate_ticket_detail_button_supporting)
            val unsupportStr =  getString(R.string.candidate_ticket_detail_button_not_supporting)

            if (ticketSupportButton.text == unsupportStr) {

                repository.supportTicket(ticketId)
                        .toBooleanSingle()
                        .subscribe()
                        .addTo(compositeDisposable)

                ticketSupportButton.setBackgroundResource(R.color.election_ticket_supporting)

                val value = partyDetailSupporters.text.toString()
                partyDetailSupporters.text = (Integer.parseInt(value) + 1).toString()
                ticketSupportButton.text = supportStr

            } else if (ticketSupportButton.text == supportStr ) {

                repository.unsupportTicket(ticketId)
                        .toBooleanSingle()
                        .subscribe()
                        .addTo(compositeDisposable)

                ticketSupportButton.setBackgroundResource(R.color.election_ticket_not_supporting)


                val value = partyDetailSupporters.text.toString()
                partyDetailSupporters.text = (Integer.parseInt(value) - 1).toString()
                ticketSupportButton.text = unsupportStr
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

        // Sets the candidate image
        val candidateProfileImage = ImageView(activity)
        candidateProfileImage.apply {
            layoutParams = LinearLayout.LayoutParams(
                    150,
                    150
            )

            scaleType = ScaleType.FIT_CENTER
            setImageResource(R.drawable.account_circle_24dp)

            setOnClickListener {

                val profileFragment = ProfileOverviewFragment()
                profileFragment.arguments = bundleOf(FRAGMENT_EXTRA_PROFILE_ID to candidate.userId)


                (activity as MainActivity).presentContent(profileFragment)
            }
        }

        val candidatePartyImage = TextView(activity)
        candidatePartyImage.apply {
            layoutParams = LinearLayout.LayoutParams(
                    200 ,
                    150
            )
            text = partyCode

            setOnClickListener {

                val profileFragment = ProfileOverviewFragment()
                profileFragment.arguments = bundleOf(FRAGMENT_EXTRA_PROFILE_ID to candidate.userId)


                (activity as MainActivity).presentContent(profileFragment)
            }
        }

        val candidateProfileLink = ImageView(activity)
        candidateProfileLink.apply {
            layoutParams = LinearLayout.LayoutParams(
                    200,
                    150
            )

            setBackgroundResource(R.drawable.profile_active)

            setOnClickListener {
                val profileFragment = CandidateProfileFragment()
                profileFragment.arguments = bundleOf(FRAGMENT_EXTRA_CANDIDATE_ID to candidate.id)

                (activity as MainActivity).presentContent(profileFragment)
            }
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
            addView(candidateProfileLink)
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.END
            layoutParams = relativeParamsRight
        }

        layout.addView(candidateProfileImage)
        layout.addView(candidatePartyImage)
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
