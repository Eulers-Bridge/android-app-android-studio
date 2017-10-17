package com.eulersbridge.isegoria;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.Calendar;

public class EventsDetailFragment extends Fragment {
	private View rootView;
    private View eventDivider1;
    private View eventDivider2;
	private float dpWidth;
	private float dpHeight;
	private DisplayMetrics displayMetrics;
	private Isegoria isegoria;
	private long timestamp;
	private String eventTitle;
	private String eventDesc;
    private Button addToCalendar;

    private TableLayout eventContactTableLayout;
    private Network network;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {   
		rootView = inflater.inflate(R.layout.events_detail_fragment, container, false);
        eventContactTableLayout = rootView.findViewById(R.id.eventDetailsTableLayout);
		this.isegoria = (Isegoria) getActivity().getApplication();
		Bundle bundle = this.getArguments();
		
		addToCalendar = rootView.findViewById(R.id.addToCalendar);
        addToCalendar.setOnClickListener(new OnClickListener() {
				@Override
			    public void onClick(View v) {
					addToCalendar(v);
			    } 
			}); 

		displayMetrics = getActivity().getResources().getDisplayMetrics();
		dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        dpHeight = displayMetrics.heightPixels / displayMetrics.density;

        eventDivider1 = rootView.findViewById(R.id.eventDivider1);
        eventDivider2 = rootView.findViewById(R.id.eventDivider2);
        
        isegoria.getNetwork().getEventDetails(this, bundle.getInt("EventId"));

        MainActivity mainActivity = (MainActivity) getActivity();
        network = mainActivity.getIsegoriaApplication().getNetwork();
		
		return rootView;
	}
	
	public void populateContent(final String title, final String content, final String location, final String likes, final Bitmap picture, final long timestamp) {
		try {
			this.timestamp = timestamp;
			getActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() {
                    int imageHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                            (float) 200, getResources().getDisplayMetrics());

					LinearLayout backgroundLinearLayout = rootView.findViewById(R.id.topBackgroundNews);
					backgroundLinearLayout.getLayoutParams().height = imageHeight;
					//Bitmap original = BitmapFactory.decodeResource(getActivity().getResources(), backgroundDrawableResource);
					//Bitmap b = Bitmap.createScaledBitmap(original, (int)dpWidth, (int)dpHeight/2, false);
					Drawable d = new BitmapDrawable(getActivity().getResources(), picture);
					d.setColorFilter(Color.argb(125, 35, 35, 35), Mode.DARKEN);
					backgroundLinearLayout.setBackgroundDrawable(d);

					TextView eventTitleField = rootView.findViewById(R.id.eventTitle);
					eventTitleField.setText(title);
					
					TextView eventTime = rootView.findViewById(R.id.eventTime);
					eventTime.setText(TimeConverter.convertTimestampToString(timestamp));
					
					TextView eventLocationLine1 = rootView.findViewById(R.id.eventLocationLine1);
					eventLocationLine1.setText(location);

                    TextView eventLocationLine2 = rootView.findViewById(R.id.eventLocationLine2);
					
					TextView eventsTextField = rootView.findViewById(R.id.eventDetails);
					eventsTextField.setText(content);
					
					eventTitle = title;
					eventDesc = content;

                    addToCalendar.setVisibility(ViewGroup.VISIBLE);
                    eventDivider1.setVisibility(ViewGroup.VISIBLE);
                    eventDivider2.setVisibility(ViewGroup.VISIBLE);
                    eventLocationLine2.setVisibility(ViewGroup.VISIBLE);
				}
			});
		} catch(Exception ignored) {
			
		}
	}
	
	private void addToCalendar(View v) {
	    Calendar cal = Calendar.getInstance();     
        Intent intent = new Intent(Intent.ACTION_EDIT);
        intent.setType("vnd.android.cursor.item/event");
        intent.putExtra("beginTime", timestamp);
        intent.putExtra("allDay", false);
        intent.putExtra("endTime", timestamp+60*60*1000);
        intent.putExtra("title", eventTitle);
        intent.putExtra("description", eventDesc);
        isegoria.getMainActivity().startActivity(intent);
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
        addTableRow(ticketId, userId, "GRN", "#4FBE3E", firstName + " " + lastName, "", positionId);
    }

    private void addTableRow(int ticketId, final int userId, String partyAbr,
                             String colour, String candidateName,
                             String candidatePosition, int positionId) {
        TableRow tr;

        LinearLayout layout = new LinearLayout(getActivity());
        layout.setOrientation(LinearLayout.HORIZONTAL);

        tr = new TableRow(getActivity());
        TableRow.LayoutParams rowParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
        tr.setLayoutParams(rowParams);
        tr.setPadding(0, 10, 0, 10);

        ImageView candidateProfileView = new ImageView(getActivity());
        candidateProfileView.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(80, 80);
        candidateProfileView.setLayoutParams(layoutParams);
        candidateProfileView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        network.getFirstPhoto(0, userId, candidateProfileView);
        candidateProfileView.setPadding(10, 0, 10, 0);

        ImageView candidateProfileImage = new ImageView(getActivity());
        candidateProfileImage.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, Gravity.END));
        candidateProfileImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
        candidateProfileImage.setImageBitmap(decodeSampledBitmapFromResource(getResources(), R.drawable.profilelight, 80, 80));
        candidateProfileImage.setPadding(10, 0, 10, 0);
        candidateProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fragmentManager2 = getFragmentManager();
                FragmentTransaction fragmentTransaction2 = fragmentManager2.beginTransaction();
                ContactProfileFragment fragment2 = new ContactProfileFragment();
                Bundle args = new Bundle();
                args.putInt("ProfileId", userId);
                fragment2.setArguments(args);
                fragmentTransaction2.addToBackStack(null);
                fragmentTransaction2.replace(android.R.id.content, fragment2);
                fragmentTransaction2.commit();
            }
        });

        TextView textViewParty = new TextView(getActivity());
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

        LinearLayout partyLayout = new LinearLayout(getActivity());
        partyLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                80, 40);
        params.gravity = Gravity.CENTER_VERTICAL;
        partyLayout.setLayoutParams(params);
        //partyLayout.setBackgroundDrawable(rectShapeDrawable);
        partyLayout.addView(textViewParty);

        TextView textViewCandidate = new TextView(getActivity());
        textViewCandidate.setTextColor(Color.parseColor("#3A3F43"));
        textViewCandidate.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16.0f);
        textViewCandidate.setText(candidateName);
        textViewCandidate.setPadding(10, 0, 10, 0);
        textViewCandidate.setGravity(Gravity.START);

        TextView textViewPosition = new TextView(getActivity());
        textViewPosition.setTextColor(Color.parseColor("#3A3F43"));
        textViewPosition.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12.0f);
        textViewPosition.setText(candidatePosition);
        textViewPosition.setPadding(10, 0, 10, 0);
        textViewPosition.setGravity(Gravity.START);

        network.getPositionText(textViewPosition, positionId);

        View dividierView = new View(getActivity());
        dividierView.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, 1));
        dividierView.setBackgroundColor(Color.parseColor("#676475"));

        RelativeLayout relLayoutMaster = new RelativeLayout(getActivity());
        TableRow.LayoutParams relLayoutMasterParam = new TableRow.LayoutParams((int)dpWidth, TableRow.LayoutParams.WRAP_CONTENT);
        relLayoutMaster.setLayoutParams(relLayoutMasterParam);

        RelativeLayout.LayoutParams relativeParamsLeft = new RelativeLayout.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
        relativeParamsLeft.addRule(RelativeLayout.ALIGN_PARENT_LEFT);

        RelativeLayout.LayoutParams relativeParamsRight = new RelativeLayout.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
        relativeParamsRight.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

        LinearLayout linLayout = new LinearLayout(getActivity());
        linLayout.setOrientation(LinearLayout.VERTICAL);
        linLayout.addView(textViewCandidate);
        linLayout.addView(textViewPosition);

        LinearLayout linLayout2 = new LinearLayout(getActivity());
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
        eventContactTableLayout.addView(dividierView);
    }

	private static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
	    // Raw height and width of image
	    final int height = options.outHeight;
	    final int width = options.outWidth;
	    int inSampleSize = 1;
	
	    if (height > reqHeight || width > reqWidth) {
	
	        final int halfHeight = height / 2;
	        final int halfWidth = width / 2;
	
	        // Calculate the largest inSampleSize value that is a power of 2 and keeps both
	        // height and width larger than the requested height and width.
	        while ((halfHeight / inSampleSize) > reqHeight
	                && (halfWidth / inSampleSize) > reqWidth) {
	            inSampleSize *= 2;
	        }
	    }
	
	    return inSampleSize;
	}
	
	private static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
                                                          int reqWidth, int reqHeight) {

	    // First decode with inJustDecodeBounds=true to check dimensions
	    final BitmapFactory.Options options = new BitmapFactory.Options();
	    options.inJustDecodeBounds = true;
	    BitmapFactory.decodeResource(res, resId, options);

	    // Calculate inSampleSize
	    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

	    // Decode bitmap with inSampleSize set
	    options.inJustDecodeBounds = false;
	    return BitmapFactory.decodeResource(res, resId, options);
	}
}