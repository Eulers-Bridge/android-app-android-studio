package com.eulersbridge.isegoria.feed;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Build;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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

import com.android.volley.VolleyError;
import com.eulersbridge.isegoria.ContactProfileFragment;
import com.eulersbridge.isegoria.Isegoria;
import com.eulersbridge.isegoria.Network;
import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.models.Event;
import com.eulersbridge.isegoria.utilities.TimeConverter;
import com.eulersbridge.isegoria.utilities.Utils;

public class EventsDetailFragment extends Fragment {
	private View rootView;
	private float dpWidth;
    private Isegoria isegoria;

    private TableLayout eventContactTableLayout;
    private Network network;

    private Event event;
    private Bitmap eventImage;
	
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.events_detail_fragment, container, false);

        eventContactTableLayout = rootView.findViewById(R.id.eventDetailsTableLayout);

        Button addToCalendar = rootView.findViewById(R.id.addToCalendar);
        addToCalendar.setOnClickListener(view -> addToCalendar());

        DisplayMetrics displayMetrics = getActivity().getResources().getDisplayMetrics();
		dpWidth = displayMetrics.widthPixels / displayMetrics.density;

        event = getArguments().getParcelable("event");

        isegoria = (Isegoria) getActivity().getApplication();
        network = isegoria.getNetwork();

        if (event != null && !TextUtils.isEmpty(event.getImageUrl())) {
            network.getPicture(event.getImageUrl(), new Network.PictureDownloadListener() {
                @Override
                public void onDownloadFinished(String url, @Nullable Bitmap bitmap) {
                    eventImage = bitmap;
                    populateContent(event);
                }

                @Override
                public void onDownloadFailed(String url, VolleyError error) {
                    eventImage = null;
                    populateContent(event);
                }
            });
        }
		
		return rootView;
	}
	
	private void populateContent(final Event event) {
        getActivity().runOnUiThread(() -> {
            int imageHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                    (float) 200, getResources().getDisplayMetrics());

            RelativeLayout backgroundLayout = rootView.findViewById(R.id.topBackgroundNews);
            backgroundLayout.getLayoutParams().height = imageHeight;
            //Bitmap original = BitmapFactory.decodeResource(getActivity().getResources(), backgroundDrawableResource);
            //Bitmap b = Bitmap.createScaledBitmap(original, (int)dpWidth, (int)dpHeight/2, false);

            if (eventImage != null) {
                Drawable d = new BitmapDrawable(getActivity().getResources(), eventImage);
                d.setColorFilter(Color.argb(125, 35, 35, 35), Mode.DARKEN);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    backgroundLayout.setBackground(d);
                } else {
                    backgroundLayout.setBackgroundDrawable(d);
                }
            }

            TextView eventTitleField = rootView.findViewById(R.id.eventTitle);
            eventTitleField.setText(event.getName());

            TextView eventTime = rootView.findViewById(R.id.eventTime);
            eventTime.setText(TimeConverter.convertTimestampToString(event.getDate()));

            TextView eventLocationLine1 = rootView.findViewById(R.id.eventLocationLine1);
            eventLocationLine1.setText(event.getLocation());

            TextView eventsTextField = rootView.findViewById(R.id.eventDetails);
            eventsTextField.setText(event.getDescription());
        });
	}
	
	private void addToCalendar() {
        Intent intent = new Intent(Intent.ACTION_INSERT)
                .setData(CalendarContract.Events.CONTENT_URI)
                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, event.getDate())

                // Make event 1 hour long (add an hour in in milliseconds to event start)
                .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, event.getDate() + 60 * 60 * 1000)
                .putExtra(CalendarContract.Events.ALL_DAY, false)

                .putExtra(CalendarContract.Events.TITLE, event.getName())
                .putExtra(CalendarContract.Events.DESCRIPTION, event.getDescription())
                .putExtra(CalendarContract.Events.EVENT_LOCATION, event.getLocation())
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
        addTableRow(ticketId, userId, "GRN", "#4FBE3E", String.format("%s %s", firstName, lastName), "", positionId);
    }

    private void addTableRow(int ticketId, final int userId, String partyAbr,
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
        network.getFirstPhoto(userId, candidateProfileView);
        candidateProfileView.setPadding(10, 0, 10, 0);

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

        network.getTicketLabel(textViewParty, ticketId);

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

        network.getPositionText(textViewPosition, positionId);

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