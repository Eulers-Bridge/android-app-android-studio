package com.eulersbridge.isegoria.feed;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.eulersbridge.isegoria.ContactProfileFragment;
import com.eulersbridge.isegoria.GlideApp;
import com.eulersbridge.isegoria.Isegoria;
import com.eulersbridge.isegoria.models.Position;
import com.eulersbridge.isegoria.models.Ticket;
import com.eulersbridge.isegoria.models.UserProfile;
import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.models.Event;
import com.eulersbridge.isegoria.utilities.TimeConverter;
import com.eulersbridge.isegoria.utilities.TintTransformation;
import com.eulersbridge.isegoria.utilities.Utils;

import org.parceler.Parcels;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EventsDetailFragment extends Fragment {
	private View rootView;
	private float dpWidth;
    private Isegoria isegoria;

    private TableLayout eventContactTableLayout;

    private Event event;
	
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.events_detail_fragment, container, false);

        eventContactTableLayout = rootView.findViewById(R.id.event_details_table_layout);

        Button addToCalendar = rootView.findViewById(R.id.event_button_add_to_calendar);
        addToCalendar.setOnClickListener(view -> addToCalendar());

        DisplayMetrics displayMetrics = getActivity().getResources().getDisplayMetrics();
		dpWidth = displayMetrics.widthPixels / displayMetrics.density;

        event = Parcels.unwrap(getArguments().getParcelable("event"));

        isegoria = (Isegoria) getActivity().getApplication();

        if (event != null) {
            populateContent(event);

            ImageView eventImageView = rootView.findViewById(R.id.event_image);

            if (!TextUtils.isEmpty(event.photos.get(0).thumbnailUrl)) {
                GlideApp.with(this)
                        .load(event.photos.get(0).thumbnailUrl)
                        .transform(new TintTransformation())
                        .into(eventImageView);
            }
        }
		
		return rootView;
	}
	
	private void populateContent(final Event event) {
        getActivity().runOnUiThread(() -> {
            TextView eventTitleField = rootView.findViewById(R.id.event_title);
            eventTitleField.setText(event.name);

            TextView eventTime = rootView.findViewById(R.id.event_time);
            eventTime.setText(TimeConverter.convertTimestampToString(event.date));

            TextView eventLocationLine1 = rootView.findViewById(R.id.event_location_1);
            eventLocationLine1.setText(event.location);

            TextView eventsTextField = rootView.findViewById(R.id.event_details);
            eventsTextField.setText(event.description);
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

        if (intent.resolveActivity(getContext().getPackageManager()) != null) {
            isegoria.getMainActivity().startActivity(intent);
        }
	}

    public void addCandidate(String email) {
        TableRow tr;

        tr = new TableRow(getActivity());
        TableRow.LayoutParams rowParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
        tr.setLayoutParams(rowParams);
        tr.setPadding(10, 10, 0, 10);

        LinearLayout layout = new LinearLayout(getActivity());
        layout.setOrientation(LinearLayout.HORIZONTAL);

        TextView contactTextView = new TextView(getActivity());
        contactTextView.setTypeface(null, Typeface.BOLD);
        contactTextView.setText("Organizer: " + email);
        layout.addView(contactTextView);
        tr.addView(layout);

        eventContactTableLayout.addView(tr);
    }

    public void addCandidate(int userId, int ticketId, int positionId, int candidateId,
                             String firstName, String lastName) {
        addTableRow(ticketId, null, userId, "GRN", "#4FBE3E", String.format("%s %s", firstName, lastName), "", positionId);
    }

    private void addTableRow(int ticketId, UserProfile user, final int userId, String partyAbr,
                             String colour, String candidateName,
                             String candidatePosition, int positionId) {
        TableRow tr;

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.HORIZONTAL);

        tr = new TableRow(getContext());
        TableRow.LayoutParams rowParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
        tr.setLayoutParams(rowParams);
        tr.setPadding(0, 10, 0, 10);

        ImageView candidateProfileView = new ImageView(getContext());
        candidateProfileView.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(80, 80);
        candidateProfileView.setLayoutParams(layoutParams);
        candidateProfileView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        candidateProfileView.setPadding(10, 0, 10, 0);

        if (!TextUtils.isEmpty(user.profilePhotoURL)){
            GlideApp.with(this).load(user.profilePhotoURL).into(candidateProfileView);
        }

        ImageView candidateProfileImage = new ImageView(getContext());
        candidateProfileImage.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, Gravity.END));
        candidateProfileImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
        candidateProfileImage.setImageBitmap(Utils.decodeSampledBitmapFromResource(getResources(), R.drawable.profilelight, 80, 80));
        candidateProfileImage.setPadding(10, 0, 10, 0);
        candidateProfileImage.setOnClickListener(view -> {

            ContactProfileFragment profileFragment = new ContactProfileFragment();
            Bundle args = new Bundle();
            args.putInt("ProfileId", userId);

            getFragmentManager().beginTransaction()
                    .addToBackStack(null)
                    .replace(R.id.container, profileFragment)
                    .commit();
        });

        TextView textViewParty = new TextView(getContext());
        textViewParty.setTextColor(Color.parseColor("#FFFFFF"));
        textViewParty.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12.0f);
        textViewParty.setText(partyAbr);
        textViewParty.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        textViewParty.setGravity(Gravity.CENTER);
        textViewParty.setTypeface(null, Typeface.BOLD);

        isegoria.getAPI().getTicket(ticketId).enqueue(new Callback<Ticket>() {
            @Override
            public void onResponse(Call<Ticket> call, Response<Ticket> response) {
                Ticket ticket = response.body();
                if (ticket != null) {
                    textViewParty.setText(ticket.code);
                    textViewParty.setBackgroundColor(Color.parseColor(ticket.getColour()));
                }
            }

            @Override
            public void onFailure(Call<Ticket> call, Throwable t) {
                t.printStackTrace();
            }
        });

        RectShape rect = new RectShape();
        ShapeDrawable rectShapeDrawable = new ShapeDrawable(rect);
        Paint paint = rectShapeDrawable.getPaint();
        paint.setColor(Color.parseColor(colour));
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setStrokeWidth(5);

        LinearLayout partyLayout = new LinearLayout(getContext());
        partyLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                80, 40);
        params.gravity = Gravity.CENTER_VERTICAL;
        partyLayout.setLayoutParams(params);
        //partyLayout.setBackgroundDrawable(rectShapeDrawable);
        partyLayout.addView(textViewParty);

        TextView textViewCandidate = new TextView(getContext());
        textViewCandidate.setTextColor(Color.parseColor("#3A3F43"));
        textViewCandidate.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16.0f);
        textViewCandidate.setText(candidateName);
        textViewCandidate.setPadding(10, 0, 10, 0);
        textViewCandidate.setGravity(Gravity.START);

        TextView textViewPosition = new TextView(getContext());
        textViewPosition.setTextColor(Color.parseColor("#3A3F43"));
        textViewPosition.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12.0f);
        textViewPosition.setText(candidatePosition);
        textViewPosition.setPadding(10, 0, 10, 0);
        textViewPosition.setGravity(Gravity.START);

        isegoria.getAPI().getPosition(positionId).enqueue(new Callback<Position>() {
            @Override
            public void onResponse(Call<Position> call, Response<Position> response) {
                Position position = response.body();
                if (position != null) {
                    textViewPosition.setText(position.name);
                }
            }

            @Override
            public void onFailure(Call<Position> call, Throwable t) {
                t.printStackTrace();
            }
        });

        View dividerView = new View(getContext());
        dividerView.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, 1));
        dividerView.setBackgroundColor(Color.parseColor("#676475"));

        RelativeLayout relLayoutMaster = new RelativeLayout(getContext());
        TableRow.LayoutParams relLayoutMasterParam = new TableRow.LayoutParams((int)dpWidth, TableRow.LayoutParams.WRAP_CONTENT);
        relLayoutMaster.setLayoutParams(relLayoutMasterParam);

        RelativeLayout.LayoutParams relativeParamsLeft = new RelativeLayout.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
        relativeParamsLeft.addRule(RelativeLayout.ALIGN_PARENT_LEFT);

        RelativeLayout.LayoutParams relativeParamsRight = new RelativeLayout.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
        relativeParamsRight.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

        LinearLayout linLayout = new LinearLayout(getContext());
        linLayout.setOrientation(LinearLayout.VERTICAL);
        linLayout.addView(textViewCandidate);
        linLayout.addView(textViewPosition);

        LinearLayout linLayout2 = new LinearLayout(getContext());
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