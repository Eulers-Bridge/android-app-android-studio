package com.eulersbridge.isegoria.feed.events.detail

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RectShape
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.core.os.bundleOf
import com.bumptech.glide.Priority
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.eulersbridge.isegoria.*
import com.eulersbridge.isegoria.feed.events.ACTIVITY_EXTRA_EVENT
import com.eulersbridge.isegoria.network.api.model.Event
import com.eulersbridge.isegoria.network.api.model.User
import com.eulersbridge.isegoria.profile.ProfileOverviewFragment
import com.eulersbridge.isegoria.util.extension.observe
import com.eulersbridge.isegoria.util.extension.toDateString
import com.eulersbridge.isegoria.util.transformation.TintTransformation
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.android.synthetic.main.event_detail_activity.*
import javax.inject.Inject

class EventDetailActivity : DaggerAppCompatActivity() {

    private val dpWidth: Float by lazy {
        val displayMetrics = resources.displayMetrics
        displayMetrics.widthPixels / displayMetrics.density
    }

    @Inject
    lateinit var modelFactory: ViewModelProvider.Factory
    lateinit var viewModel: EventDetailViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.event_detail_activity)

        window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

        viewModel = ViewModelProviders.of(this, modelFactory)[EventDetailViewModel::class.java]

        backButton.setOnClickListener { onBackPressed() }
        addToCalendarButton.setOnClickListener { viewModel.addToCalendar(this) }

        val event = intent.getParcelableExtra<Event>(ACTIVITY_EXTRA_EVENT)
        viewModel.event.value = event

        if (event != null) {
            populateContent(event)

            GlideApp.with(this)
                .load(event.getPhotoUrl())
                .priority(Priority.HIGH)
                .transform(TintTransformation())
                .placeholder(R.color.lightGrey)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(eventImageView)
        }
    }

    private fun populateContent(event: Event) {
        runOnUiThread {
            titleTextView.text = event.name
            timeTextView.text = event.createdDate.toDateString(this)
            locationTextView.text = event.location
            detailsTextView.text = event.description

            event.organizerEmail?.let {
                addCandidate(it)
            }
        }
    }

    private fun addCandidate(email: String) {
        val tr = TableRow(this)
        tr.apply {
            layoutParams = TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
            )

            setPadding(10, 10, 0, 10)
        }

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.HORIZONTAL

        val contactTextView = TextView(this)
        contactTextView.apply {
            setTypeface(null, Typeface.BOLD)
            text = getString(R.string.event_detail_activity_organiser, email)
        }

        layout.addView(contactTextView)
        tr.addView(layout)

        tableLayout.addView(tr)
    }

    fun addCandidate(
        userId: Int, ticketId: Int, positionId: Int, candidateId: Int,
        firstName: String, lastName: String
    ) {
        addTableRow(
            ticketId,
            null,
            userId,
            "GRN",
            "#4FBE3E",
            "$firstName $lastName",
            "",
            positionId
        )
    }

    private fun addTableRow(
        ticketId: Int, user: User?, userId: Int, partyAbr: String,
        colour: String, candidateName: String,
        candidatePosition: String, positionId: Int
    ) {
        val tr = TableRow(this)

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.HORIZONTAL

        val rowParams = TableRow.LayoutParams(
            TableRow.LayoutParams.MATCH_PARENT,
            TableRow.LayoutParams.WRAP_CONTENT
        )
        tr.layoutParams = rowParams
        tr.setPadding(0, 10, 0, 10)

        val candidateProfileView = ImageView(this)
        candidateProfileView.apply {
            layoutParams = TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT
            )

            scaleType = ImageView.ScaleType.CENTER_CROP
            setPadding(10, 0, 10, 0)
            layoutParams = LinearLayout.LayoutParams(80, 80)
        }

        GlideApp.with(this)
            .load(user?.profilePhotoURL)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(candidateProfileView)

        val candidateProfileImage = ImageView(this)
        candidateProfileImage.apply {

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                Gravity.END.toFloat()
            )

            scaleType = ImageView.ScaleType.CENTER_CROP

            setImageResource(R.drawable.profile_light)
            setPadding(10, 0, 10, 0)
            setOnClickListener {
                val profileFragment = ProfileOverviewFragment()
                profileFragment.arguments = bundleOf(FRAGMENT_EXTRA_PROFILE_ID to userId)

                supportFragmentManager.beginTransaction()
                    .setReorderingAllowed(true)
                    .addToBackStack(null)
                    .replace(R.id.container, profileFragment)
                    .commit()
            }
        }

        val textViewParty = TextView(this)
        textViewParty.apply {
            setTextColor(Color.parseColor("#FFFFFF"))
            setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12.0f)
            setTypeface(null, Typeface.BOLD)

            text = partyAbr
            gravity = Gravity.CENTER

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }

        observe(viewModel.getTicket(ticketId.toLong())) {
            if (it != null)
                textViewParty.apply {
                    text = it.code
                    setBackgroundColor(Color.parseColor(it.getColour()))
                }
        }

        val rect = RectShape()
        val rectShapeDrawable = ShapeDrawable(rect)
        val paint = rectShapeDrawable.paint
        paint.color = Color.parseColor(colour)
        paint.style = Paint.Style.FILL_AND_STROKE
        paint.strokeWidth = 5f

        val partyLayout = LinearLayout(this)
        partyLayout.orientation = LinearLayout.VERTICAL
        val params = LinearLayout.LayoutParams(
            80, 40
        )
        params.gravity = Gravity.CENTER_VERTICAL
        partyLayout.layoutParams = params
        //partyLayout.setBackgroundDrawable(rectShapeDrawable);
        partyLayout.addView(textViewParty)

        val textViewCandidate = TextView(this)
        textViewCandidate.apply {
            setTextColor(Color.parseColor("#3A3F43"))
            setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16.0f)
            text = candidateName
            setPadding(10, 0, 10, 0)
            gravity = Gravity.START
        }

        val textViewPosition = TextView(this)
        textViewPosition.apply {
            setTextColor(Color.parseColor("#3A3F43"))
            setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12.0f)
            text = candidatePosition
            setPadding(10, 0, 10, 0)
            gravity = Gravity.START
        }

        observe(viewModel.getPosition(positionId.toLong())) {
            textViewPosition.text = it?.name
        }

        val dividerView = View(this)
        dividerView.layoutParams = TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, 1)
        dividerView.setBackgroundColor(Color.parseColor("#676475"))

        val relLayoutMaster = RelativeLayout(this)
        val relLayoutMasterParam =
            TableRow.LayoutParams(dpWidth.toInt(), TableRow.LayoutParams.WRAP_CONTENT)
        relLayoutMaster.layoutParams = relLayoutMasterParam

        val relativeParamsLeft = RelativeLayout.LayoutParams(
            TableRow.LayoutParams.MATCH_PARENT,
            TableRow.LayoutParams.WRAP_CONTENT
        )
        relativeParamsLeft.addRule(RelativeLayout.ALIGN_PARENT_LEFT)

        val relativeParamsRight = RelativeLayout.LayoutParams(
            TableRow.LayoutParams.MATCH_PARENT,
            TableRow.LayoutParams.WRAP_CONTENT
        )
        relativeParamsRight.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)

        val linLayout = LinearLayout(this)
        linLayout.orientation = LinearLayout.VERTICAL
        linLayout.addView(textViewCandidate)
        linLayout.addView(textViewPosition)

        val linLayout2 = LinearLayout(this)
        linLayout2.apply {
            orientation = LinearLayout.VERTICAL
            addView(candidateProfileImage)
            gravity = Gravity.END
        }
        linLayout2.layoutParams = relativeParamsRight

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
}
