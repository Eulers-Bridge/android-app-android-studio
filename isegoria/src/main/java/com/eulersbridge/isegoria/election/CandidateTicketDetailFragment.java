package com.eulersbridge.isegoria.election;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TableRow.LayoutParams;
import android.widget.TextView;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.eulersbridge.isegoria.GlideApp;
import com.eulersbridge.isegoria.Isegoria;
import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.common.BlurTransformation;
import com.eulersbridge.isegoria.common.Constant;
import com.eulersbridge.isegoria.models.Candidate;
import com.eulersbridge.isegoria.models.Photo;
import com.eulersbridge.isegoria.models.Position;
import com.eulersbridge.isegoria.models.Ticket;
import com.eulersbridge.isegoria.network.IgnoredCallback;
import com.eulersbridge.isegoria.network.PhotosResponse;
import com.eulersbridge.isegoria.network.SimpleCallback;
import com.eulersbridge.isegoria.profile.ProfileOverviewFragment;

import java.util.List;

import retrofit2.Response;

public class CandidateTicketDetailFragment extends Fragment {
    private TableLayout candidateTicketDetialTableLayout;
    private Button ticketSupportButton;
	
	private float dpWidth;

	private long ticketId;
    private String code =  "";
    private String colour = "#000000";
    private String partyColour = "";
    private String partyLogo = "";

    private Isegoria isegoria;

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.candidate_ticket_detail_fragment, container, false);

		int backgroundDrawableResource = R.drawable.me;

        Bundle bundle = getArguments();
        ticketId = bundle.getLong("TicketId");
        final String ticketName = bundle.getString("TicketName");
        final int noOfSupporters = bundle.getInt("NoOfSupporters");
        partyColour = bundle.getString("Colour");
        partyLogo = bundle.getString("Logo");

		candidateTicketDetialTableLayout = rootView.findViewById(R.id.candidateTicketDetailTable);
        TextView partyDetailSupporters = rootView.findViewById(R.id.partyDetailSupporters);

		DisplayMetrics displayMetrics = getActivity().getResources().getDisplayMetrics();
		dpWidth = displayMetrics.widthPixels / displayMetrics.density;
		
		LinearLayout backgroundLinearLayout = rootView.findViewById(R.id.topBackgroundDetail);

        GlideApp.with(this)
                .load(R.drawable.birmingham)
                .transforms(new CenterCrop(), new BlurTransformation(getContext()))
                .priority(Priority.HIGH)
                .into(new SimpleTarget<Drawable>() {
                    @Override
                    public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
                        if (isAdded() && !isDetached()) {
                            backgroundLinearLayout.post(() -> backgroundLinearLayout.setBackground(resource));
                        }
                    }
                });

        isegoria = (Isegoria)getActivity().getApplication();
        isegoria.getAPI().getTicketCandidates(ticketId).enqueue(new SimpleCallback<List<Candidate>>() {
            @Override
            public void handleResponse(Response<List<Candidate>> response) {
                List<Candidate> candidates = response.body();
                if (candidates != null) {
                    addCandidates(candidates);
                }
            }
        });

        ImageView partyLogoImageView = rootView.findViewById(R.id.partyDetailLogo);

        isegoria.getAPI().getPhotos(ticketId).enqueue(new SimpleCallback<PhotosResponse>() {
            @Override
            protected void handleResponse(Response<PhotosResponse> response) {
                PhotosResponse photosResponse = response.body();
                if (photosResponse != null && photosResponse.totalPhotos > 0) {
                    Photo photo = photosResponse.photos.get(0);

                    GlideApp.with(CandidateTicketDetailFragment.this)
                            .load(photo.thumbnailUrl)
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .into(partyLogoImageView);
                }
            }
        });

        ticketSupportButton = rootView.findViewById(R.id.supportButton);

        isegoria.getAPI().getUserSupportedTickets(isegoria.getLoggedInUser().email).enqueue(new SimpleCallback<List<Ticket>>() {
            @Override
            public void handleResponse(Response<List<Ticket>> response) {
                List<Ticket> tickets = response.body();
                if (tickets != null && tickets.size() > 0) {
                    for (Ticket ticket : tickets) {
                        if (ticket.id == ticketId) {
                            getActivity().runOnUiThread(() -> ticketSupportButton.setText("Unsupport"));
                        }
                    }
                }
            }
        });

        ticketSupportButton.setOnClickListener(view -> {

            String userEmail = isegoria.getLoggedInUser().email;

            if(ticketSupportButton.getText().equals("Support")) {

                isegoria.getAPI().supportTicket(ticketId, userEmail).enqueue(new IgnoredCallback<>());

                String value = String.valueOf(partyDetailSupporters.getText());
                partyDetailSupporters.setText(String.valueOf(Integer.parseInt(value) + 1));
                ticketSupportButton.setText("Unsupport");
            }
            else if(ticketSupportButton.getText().equals("Unsupport")) {

                isegoria.getAPI().unsupportTicket(ticketId, userEmail).enqueue(new IgnoredCallback<>());

                String value = String.valueOf(partyDetailSupporters.getText());
                partyDetailSupporters.setText(String.valueOf(Integer.parseInt(value) - 1));
                ticketSupportButton.setText("Support");
            }
        });

        TextView partyDetailName = rootView.findViewById(R.id.partyNameDetail);
        partyDetailSupporters.setText(String.valueOf(noOfSupporters));
        partyDetailName.setText(ticketName);

		return rootView;
	}

    private void addCandidates(@NonNull List<Candidate> candidates) {
	    if (getActivity() != null && candidates.size() > 0) {

	        getActivity().runOnUiThread(() -> {

                for (Candidate candidate : candidates) {
                    addTableRow(candidate);
                }
            });
        }
    }
	
	private void addTableRow(Candidate candidate) {
		TableRow tr;

        int paddingMargin3 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                (float) 6.666666667, getResources().getDisplayMetrics());
		
		LinearLayout layout = new LinearLayout(getActivity());
		layout.setOrientation(LinearLayout.HORIZONTAL);
		
		tr = new TableRow(getActivity());
		LayoutParams rowParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		tr.setLayoutParams(rowParams);
		tr.setPadding(0, paddingMargin3, 0, paddingMargin3);
        int imageHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                (float) 53.33333333, getResources().getDisplayMetrics());
        
		ImageView candidateProfileView = new ImageView(getActivity());
		candidateProfileView.setLayoutParams(new LayoutParams(imageHeight, imageHeight));
		candidateProfileView.setScaleType(ScaleType.CENTER_CROP);
		//candidateProfileView.setImageBitmap(decodeSampledBitmapFromResource(getResources(), candidate.userId, imageHeight, imageHeight));
		candidateProfileView.setPadding(paddingMargin3, 0, paddingMargin3, 0);

		isegoria.getAPI().getPhotos(candidate.userId).enqueue(new SimpleCallback<PhotosResponse>() {
            @Override
            protected void handleResponse(Response<PhotosResponse> response) {
                PhotosResponse body = response.body();
                if (body != null && body.totalPhotos > 0) {
                    Photo photo = body.photos.get(0);

                    GlideApp.with(CandidateTicketDetailFragment.this)
                            .load(photo.thumbnailUrl)
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .into(candidateProfileView);
                }
            }
        });
		
		ImageView candidateProfileImage = new ImageView(getActivity());
		candidateProfileImage.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 
				Gravity.END));
		candidateProfileImage.setScaleType(ScaleType.CENTER_CROP);
		candidateProfileImage.setImageResource(R.drawable.profilelight);
		candidateProfileImage.setPadding(paddingMargin3, 0, paddingMargin3, 0);

        candidateProfileImage.setOnClickListener(view -> {

            Bundle args = new Bundle();
            args.putLong(Constant.FRAGMENT_EXTRA_PROFILE_ID, candidate.userId);

            ProfileOverviewFragment profileOverviewFragment = new ProfileOverviewFragment();
            profileOverviewFragment.setArguments(args);

            getChildFragmentManager()
                    .beginTransaction()
                    .addToBackStack(null)
                    .replace(android.R.id.content, profileOverviewFragment)
                    .commit();
        });
		
        TextView textViewParty = new TextView(getActivity());
        textViewParty.setTextColor(Color.parseColor("#FFFFFF"));
        textViewParty.setTextSize(TypedValue.COMPLEX_UNIT_DIP,12.0f);
        textViewParty.setText(partyLogo);
        textViewParty.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        textViewParty.setGravity(Gravity.CENTER);
        textViewParty.setTypeface(null, Typeface.BOLD);
		
        RectShape rect = new RectShape();
        ShapeDrawable rectShapeDrawable = new ShapeDrawable(rect);
        Paint paint = rectShapeDrawable.getPaint();
        paint.setColor(Color.parseColor(partyColour));
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setStrokeWidth(5);	     
        
		LinearLayout partyLayout = new LinearLayout(getActivity());
		partyLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
        		imageHeight, 40);
        params.gravity = Gravity.CENTER_VERTICAL;
        partyLayout.setLayoutParams(params);
        partyLayout.setBackground(rectShapeDrawable);
        partyLayout.addView(textViewParty);
		
        TextView textViewCandidate = new TextView(getActivity());
        textViewCandidate.setTextColor(Color.parseColor("#3A3F43"));
        textViewCandidate.setTextSize(TypedValue.COMPLEX_UNIT_DIP,16.0f);
        textViewCandidate.setText(candidate.getName());
        textViewCandidate.setPadding(paddingMargin3, 0, paddingMargin3, 0);
        textViewCandidate.setGravity(Gravity.START);
        
        TextView textViewPosition = new TextView(getActivity());
        textViewPosition.setTextColor(Color.parseColor("#3A3F43"));
        textViewPosition.setTextSize(TypedValue.COMPLEX_UNIT_DIP,12.0f);
        textViewPosition.setPadding(paddingMargin3, 0, paddingMargin3, 0);
        textViewPosition.setGravity(Gravity.START);

        isegoria.getAPI().getPosition(candidate.positionId).enqueue(new SimpleCallback<Position>() {
            @Override
            public void handleResponse(Response<Position> response) {
                Position position = response.body();
                if (position != null) {
                    textViewPosition.setText(position.name);
                }
            }
        });
        
        View dividerView = new View(getActivity());
        dividerView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, 1));
        dividerView.setBackgroundColor(Color.parseColor("#676475"));

        RelativeLayout relLayoutMaster = new RelativeLayout(getActivity());
        LayoutParams relLayoutMasterParam = new LayoutParams((int)dpWidth, LayoutParams.WRAP_CONTENT);
        relLayoutMaster.setLayoutParams(relLayoutMasterParam);
        
        RelativeLayout.LayoutParams relativeParamsLeft = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        relativeParamsLeft.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        
        RelativeLayout.LayoutParams relativeParamsRight = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
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
        
        candidateTicketDetialTableLayout.addView(tr);
        candidateTicketDetialTableLayout.addView(dividerView);
	}
}
