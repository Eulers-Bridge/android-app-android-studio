package com.eulersbridge.isegoria.election;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TableRow.LayoutParams;
import android.widget.TextView;

import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.eulersbridge.isegoria.GlideApp;
import com.eulersbridge.isegoria.Isegoria;
import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.common.Constant;
import com.eulersbridge.isegoria.models.Candidate;
import com.eulersbridge.isegoria.models.Photo;
import com.eulersbridge.isegoria.models.Position;
import com.eulersbridge.isegoria.models.Ticket;
import com.eulersbridge.isegoria.network.SimpleCallback;
import com.eulersbridge.isegoria.profile.ProfileOverviewFragment;

import org.parceler.Parcels;

import java.util.List;

import retrofit2.Response;

public class CandidatePositionFragment extends Fragment {
    private TableLayout positionsTableLayout;
	
	private float dpWidth;

	private Isegoria isegoria;

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.candidate_position_fragment, container, false);
		positionsTableLayout = rootView.findViewById(R.id.candidatePositionTable);

        Position position = Parcels.unwrap(getArguments().getParcelable(Constant.FRAGMENT_EXTRA_CANDIDATE_POSITION));

        long positionId = position.id;
		
		DisplayMetrics displayMetrics = getActivity().getResources().getDisplayMetrics();
		dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        
        //addTableRow(R.drawable.head1, "GRN", "#4FBE3E", "Lillian Adams", "President");

        isegoria = (Isegoria)getActivity().getApplication();

        isegoria.getAPI().getPositionCandidates(positionId).enqueue(new SimpleCallback<List<Candidate>>() {
            @Override
            public void handleResponse(Response<List<Candidate>> response) {
                List<Candidate> candidates = response.body();
                if (candidates != null && candidates.size() > 0) {
                    addCandidates(candidates);
                }
            }
        });
		
		return rootView;
	}

    private void addCandidates(List<Candidate> candidates) {
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
		
		LinearLayout layout = new LinearLayout(getActivity());
		layout.setOrientation(LinearLayout.HORIZONTAL);

        int paddingMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                (float) 6.5, getResources().getDisplayMetrics());
        int imageSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                (float) 53, getResources().getDisplayMetrics());
		
		tr = new TableRow(getActivity());
		TableRow.LayoutParams rowParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
		tr.setLayoutParams(rowParams);
		tr.setPadding(0, paddingMargin, 0, paddingMargin);
		
		ImageView candidateProfileView = new ImageView(getActivity());
		candidateProfileView.setLayoutParams(new TableRow.LayoutParams(imageSize, imageSize));
		candidateProfileView.setScaleType(ScaleType.CENTER_CROP);
		//candidateProfileView.setImageBitmap(decodeSampledBitmapFromResource(getResources(), profileDrawable, imageSize, imageSize));
		candidateProfileView.setPadding(paddingMargin, 0, paddingMargin, 0);

		isegoria.getAPI().getPhoto(candidate.userId).enqueue(new SimpleCallback<Photo>() {
            @Override
            protected void handleResponse(Response<Photo> response) {
                Photo photo = response.body();
                if (photo != null) {
                    GlideApp.with(CandidatePositionFragment.this)
                            .load(photo.thumbnailUrl)
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .into(candidateProfileView);
                }
            }
        });
		
		ImageView candidateProfileImage = new ImageView(getActivity());
		candidateProfileImage.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, Gravity.END));
		candidateProfileImage.setScaleType(ScaleType.CENTER_CROP);
		candidateProfileImage.setImageResource(R.drawable.profilelight);
		candidateProfileImage.setPadding(paddingMargin, 0, paddingMargin, 0);
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
        textViewParty.setTextSize(12.0f);
        textViewParty.setText("");
        textViewParty.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        textViewParty.setGravity(Gravity.CENTER);
        textViewParty.setTypeface(null, Typeface.BOLD);

        isegoria.getAPI().getTicket(candidate.ticketId).enqueue(new SimpleCallback<Ticket>() {
            @Override
            protected void handleResponse(Response<Ticket> response) {
                Ticket ticket = response.body();
                if (ticket != null) {
                    textViewParty.setText(ticket.code);
                    textViewParty.setBackgroundColor(Color.parseColor(ticket.getColour()));
                }
            }
        });

        int imageSize2 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                (float) 27, getResources().getDisplayMetrics());
        
		LinearLayout partyLayout = new LinearLayout(getActivity());
		partyLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
        		imageSize, imageSize2);
        params.gravity = Gravity.CENTER_VERTICAL;
        partyLayout.setLayoutParams(params);
		partyLayout.addView(textViewParty);
		
        TextView textViewCandidate = new TextView(getActivity());
        textViewCandidate.setTextColor(Color.parseColor("#3A3F43"));
        textViewCandidate.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14.0f);
        textViewCandidate.setText(candidate.getName());
        textViewCandidate.setPadding(paddingMargin, 0, paddingMargin, 0);
        textViewCandidate.setGravity(Gravity.START);
        
        TextView textViewPosition = new TextView(getActivity());
        textViewPosition.setTextColor(Color.parseColor("#3A3F43"));
        textViewPosition.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 11.0f);
        textViewPosition.setPadding(paddingMargin, 0, paddingMargin, 0);
        textViewPosition.setGravity(Gravity.START);

        isegoria.getAPI().getPosition(candidate.positionId).enqueue(new SimpleCallback<Position>() {
            @Override
            public void handleResponse(Response<Position> response) {
                Position position = response.body();
                if (position != null) textViewPosition.setText(position.name);
            }
        });
        
        View dividierView = new View(getActivity());
        dividierView.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, 1));
        dividierView.setBackgroundColor(Color.parseColor("#676475"));

        RelativeLayout relLayoutMaster = new RelativeLayout(getActivity());
        TableRow.LayoutParams relLayoutMasterParam = new TableRow.LayoutParams((int)dpWidth, TableRow.LayoutParams.WRAP_CONTENT); 
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
        
        positionsTableLayout.addView(tr);
        positionsTableLayout.addView(dividierView);
	}
}
