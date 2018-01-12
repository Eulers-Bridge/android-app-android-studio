package com.eulersbridge.isegoria.feed.events;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.eulersbridge.isegoria.GlideApp;
import com.eulersbridge.isegoria.IsegoriaApp;
import com.eulersbridge.isegoria.network.api.models.Event;
import com.eulersbridge.isegoria.network.api.models.Position;
import com.eulersbridge.isegoria.network.api.models.Ticket;
import com.eulersbridge.isegoria.network.api.models.User;
import com.eulersbridge.isegoria.profile.ProfileOverviewFragment;
import com.eulersbridge.isegoria.util.Constants;
import com.eulersbridge.isegoria.util.Strings;
import com.eulersbridge.isegoria.util.network.SimpleCallback;
import com.eulersbridge.isegoria.util.transformation.TintTransformation;
import com.eulersbridge.isegoria.R;

import org.parceler.Parcels;

import retrofit2.Response;

public class EventDetailActivity extends AppCompatActivity {

    private float dpWidth;
    private IsegoriaApp isegoriaApp;

    private TableLayout eventContactTableLayout;

    private Event event;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.event_detail_activity);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        findViewById(R.id.back_button).setOnClickListener(view -> onBackPressed());

        eventContactTableLayout = findViewById(R.id.event_details_table_layout);

        Button addToCalendar = findViewById(R.id.event_button_add_to_calendar);
        addToCalendar.setOnClickListener(view -> addToCalendar());

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        dpWidth = displayMetrics.widthPixels / displayMetrics.density;

        event = Parcels.unwrap(getIntent().getParcelableExtra(Constants.ACTIVITY_EXTRA_EVENT));

        isegoriaApp = (IsegoriaApp)getApplication();

        if (event != null) {
            populateContent(event);

            ImageView eventImageView = findViewById(R.id.event_image);

            GlideApp.with(this)
                    .load(event.getPhotoUrl())
                    .priority(Priority.HIGH)
                    .transform(new TintTransformation())
                    .placeholder(R.color.lightGrey)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(eventImageView);
        }
    }

    private void populateContent(final Event event) {
        runOnUiThread(() -> {
            TextView eventTitleField = findViewById(R.id.event_title);
            eventTitleField.setText(event.name);

            TextView eventTime = findViewById(R.id.event_time);
            eventTime.setText(Strings.fromTimestamp(this, event.date));

            TextView eventLocationLine1 = findViewById(R.id.event_location);
            eventLocationLine1.setText(event.location);

            TextView eventsTextField = findViewById(R.id.event_details);
            eventsTextField.setText(event.description);

            addCandidate(event.organizerEmail);
        });
    }

    private void addToCalendar() {
        Intent intent = new Intent(Intent.ACTION_INSERT)
                .setData(CalendarContract.Events.CONTENT_URI)
                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, event.date)

                // Make event 1 hour long (add an hour in in milliseconds to event start)
                .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, event.date + 60 * 60 * 1000)
                .putExtra(CalendarContract.Events.ALL_DAY, false)

                .putExtra(CalendarContract.Events.TITLE, event.name)
                .putExtra(CalendarContract.Events.DESCRIPTION, event.description)
                .putExtra(CalendarContract.Events.EVENT_LOCATION, event.location)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        if (intent.resolveActivity(getPackageManager()) != null) {
            isegoriaApp.getMainActivity().startActivity(intent);
        }
    }

    private void addCandidate(String email) {
        TableRow tr;

        tr = new TableRow(this);
        TableRow.LayoutParams rowParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
        tr.setLayoutParams(rowParams);
        tr.setPadding(10, 10, 0, 10);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.HORIZONTAL);

        TextView contactTextView = new TextView(this);
        contactTextView.setTypeface(null, Typeface.BOLD);
        contactTextView.setText(getString(R.string.event_detail_activity_organiser, email));
        layout.addView(contactTextView);
        tr.addView(layout);

        eventContactTableLayout.addView(tr);
    }

    public void addCandidate(int userId, int ticketId, int positionId, int candidateId,
                             String firstName, String lastName) {
        addTableRow(ticketId, null, userId, "GRN", "#4FBE3E", String.format("%s %s", firstName, lastName), "", positionId);
    }

    private void addTableRow(int ticketId, User user, final int userId, String partyAbr,
                             String colour, String candidateName,
                             String candidatePosition, int positionId) {
        TableRow tr;

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.HORIZONTAL);

        tr = new TableRow(this);
        TableRow.LayoutParams rowParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
        tr.setLayoutParams(rowParams);
        tr.setPadding(0, 10, 0, 10);

        ImageView candidateProfileView = new ImageView(this);
        candidateProfileView.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(80, 80);
        candidateProfileView.setLayoutParams(layoutParams);
        candidateProfileView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        candidateProfileView.setPadding(10, 0, 10, 0);

        GlideApp.with(this)
                .load(user.profilePhotoURL)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(candidateProfileView);

        ImageView candidateProfileImage = new ImageView(this);
        candidateProfileImage.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, Gravity.END));
        candidateProfileImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
        candidateProfileImage.setImageResource(R.drawable.profilelight);
        candidateProfileImage.setPadding(10, 0, 10, 0);
        candidateProfileImage.setOnClickListener(view -> {

            ProfileOverviewFragment profileOverviewFragment = new ProfileOverviewFragment();
            Bundle args = new Bundle();
            args.putInt(Constants.FRAGMENT_EXTRA_PROFILE_ID, userId);

            getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .addToBackStack(null)
                    .replace(R.id.container, profileOverviewFragment)
                    .commit();
        });

        TextView textViewParty = new TextView(this);
        textViewParty.setTextColor(Color.parseColor("#FFFFFF"));
        textViewParty.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12.0f);
        textViewParty.setText(partyAbr);
        textViewParty.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        textViewParty.setGravity(Gravity.CENTER);
        textViewParty.setTypeface(null, Typeface.BOLD);

        isegoriaApp.getAPI().getTicket(ticketId).enqueue(new SimpleCallback<Ticket>() {
            @Override
            public void handleResponse(Response<Ticket> response) {
                Ticket ticket = response.body();
                if (ticket != null) {
                    textViewParty.setText(ticket.code);
                    textViewParty.setBackgroundColor(Color.parseColor(ticket.getColour()));
                }
            }
        });

        RectShape rect = new RectShape();
        ShapeDrawable rectShapeDrawable = new ShapeDrawable(rect);
        Paint paint = rectShapeDrawable.getPaint();
        paint.setColor(Color.parseColor(colour));
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setStrokeWidth(5);

        LinearLayout partyLayout = new LinearLayout(this);
        partyLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                80, 40);
        params.gravity = Gravity.CENTER_VERTICAL;
        partyLayout.setLayoutParams(params);
        //partyLayout.setBackgroundDrawable(rectShapeDrawable);
        partyLayout.addView(textViewParty);

        TextView textViewCandidate = new TextView(this);
        textViewCandidate.setTextColor(Color.parseColor("#3A3F43"));
        textViewCandidate.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16.0f);
        textViewCandidate.setText(candidateName);
        textViewCandidate.setPadding(10, 0, 10, 0);
        textViewCandidate.setGravity(Gravity.START);

        TextView textViewPosition = new TextView(this);
        textViewPosition.setTextColor(Color.parseColor("#3A3F43"));
        textViewPosition.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12.0f);
        textViewPosition.setText(candidatePosition);
        textViewPosition.setPadding(10, 0, 10, 0);
        textViewPosition.setGravity(Gravity.START);

        isegoriaApp.getAPI().getPosition(positionId).enqueue(new SimpleCallback<Position>() {
            @Override
            public void handleResponse(Response<Position> response) {
                Position position = response.body();
                if (position != null) textViewPosition.setText(position.name);
            }
        });

        View dividerView = new View(this);
        dividerView.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, 1));
        dividerView.setBackgroundColor(Color.parseColor("#676475"));

        RelativeLayout relLayoutMaster = new RelativeLayout(this);
        TableRow.LayoutParams relLayoutMasterParam = new TableRow.LayoutParams((int)dpWidth, TableRow.LayoutParams.WRAP_CONTENT);
        relLayoutMaster.setLayoutParams(relLayoutMasterParam);

        RelativeLayout.LayoutParams relativeParamsLeft = new RelativeLayout.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
        relativeParamsLeft.addRule(RelativeLayout.ALIGN_PARENT_LEFT);

        RelativeLayout.LayoutParams relativeParamsRight = new RelativeLayout.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
        relativeParamsRight.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

        LinearLayout linLayout = new LinearLayout(this);
        linLayout.setOrientation(LinearLayout.VERTICAL);
        linLayout.addView(textViewCandidate);
        linLayout.addView(textViewPosition);

        LinearLayout linLayout2 = new LinearLayout(this);
        linLayout2.setOrientation(LinearLayout.VERTICAL);
        linLayout2.addView(candidateProfileImage);
        linLayout2.setGravity(Gravity.END);
        linLayout2.setLayoutParams(relativeParamsRight);

        layout.addView(candidateProfileView);
        layout.addView(partyLayout);
        layout.addView(linLayout);
        layout.setLayoutParams(relativeParamsLeft);

        relLayoutMaster.addView(layout);
        relLayoutMaster.addView(linLayout2);

        tr.addView(relLayoutMaster);

        eventContactTableLayout.addView(tr);
        eventContactTableLayout.addView(dividerView);
    }
}
