package com.eulersbridge.isegoria.election;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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
import android.widget.SearchView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TableRow.LayoutParams;
import android.widget.TextView;

import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.eulersbridge.isegoria.ContactProfileFragment;
import com.eulersbridge.isegoria.GlideApp;
import com.eulersbridge.isegoria.Isegoria;
import com.eulersbridge.isegoria.models.Election;
import com.eulersbridge.isegoria.models.Position;
import com.eulersbridge.isegoria.models.Ticket;
import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.models.Candidate;
import com.eulersbridge.isegoria.network.PhotosResponse;
import com.eulersbridge.isegoria.network.SimpleCallback;
import com.eulersbridge.isegoria.utilities.Utils;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CandidateAllFragment extends Fragment {
	private View rootView;
	private TableLayout candidateAllTableLayout;
    private final ArrayList<String> firstNames = new ArrayList<>();
    private final ArrayList<String> lastnNmes = new ArrayList<>();
    private final ArrayList<TableRow> rows = new ArrayList<>();
	
	private float dpWidth;

    private CandidateAllFragment candidateAllFragment;
    private Isegoria isegoria;

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        candidateAllFragment = this;
		DisplayMetrics displayMetrics = getActivity().getResources().getDisplayMetrics();
		
		rootView = inflater.inflate(R.layout.candidate_all_fragment, container, false);
		candidateAllTableLayout = rootView.findViewById(R.id.candidateAllTable);

		dpWidth = displayMetrics.widthPixels;

        View dividerView = new View(getActivity());
        dividerView.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, 1));
        dividerView.setBackgroundColor(Color.parseColor("#676475"));
        candidateAllTableLayout.addView(dividerView);

        isegoria = (Isegoria)getActivity().getApplication();

        long institutionId = isegoria.getLoggedInUser().institutionId;

        isegoria.getAPI().getElections(institutionId).enqueue(electionsCallback);

        SearchView searchViewCandidatesAll = rootView.findViewById(R.id.searchViewCandidatesAll);
        searchViewCandidatesAll.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String query) {
                return handleSearchQueryTextChange(query);
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                return handleSearchQueryTextChange(query);
            }
        });
        
		return rootView;
	}

	private boolean handleSearchQueryTextChange(String query) {
        addAllRows();

        if (query.length() != 0) {
            for (int i = 0; i < rows.size(); i++) {
                View view = rows.get(i);
                if (view instanceof TableRow) {
                    try {
                        TableRow row = (TableRow) view;
                        String firstName = firstNames.get(i);
                        String lastName = lastnNmes.get(i);

                        if(!firstName.toLowerCase().contains(query.toLowerCase()) && !lastName.toLowerCase().contains(query.toLowerCase())) {
                            candidateAllTableLayout.removeView(row);
                        }

                    } catch(Exception ignored) {}
                }
            }
            candidateAllFragment.rootView.invalidate();

            return true;
        }
        return false;
    }

    private final Callback<List<Election>> electionsCallback = new Callback<List<Election>>() {
        @Override
        public void onResponse(Call<List<Election>> call, Response<List<Election>> response) {
            List<Election> elections = response.body();
            if (elections != null && elections.size() > 0) {
                Election election = elections.get(0);

                isegoria.getAPI().getElectionCandidates(election.id).enqueue(candidatesCallback);
            }
        }

        @Override
        public void onFailure(Call<List<Election>> call, Throwable t) {
            t.printStackTrace();
        }
    };

    private final Callback<List<Candidate>> candidatesCallback = new Callback<List<Candidate>>() {
        @Override
        public void onResponse(Call<List<Candidate>> call, Response<List<Candidate>> response) {
            List<Candidate> candidates = response.body();
            if (candidates != null) {
                addCandidates(candidates);
            }
        }

        @Override
        public void onFailure(Call<List<Candidate>> call, Throwable t) {
            t.printStackTrace();
        }
    };

    private void addAllRows() {
        try {
            candidateAllTableLayout.removeAllViews();
            for (int i = 0; i < rows.size(); i++) {
                candidateAllTableLayout.addView(rows.get(i));
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void addCandidates(List<Candidate> candidates) {
	    if (getActivity() != null && candidates.size() > 0) {
	        getActivity().runOnUiThread(() -> {

	            for (Candidate candidate : candidates) {
                    addTableRow(
                            candidate.ticketId,
                            candidate.userId,
                            "GRN",
                            "#4FBE3E",
                            String.format("%s %s", candidate.givenName, candidate.familyName),
                            "",
                            candidate.positionId,
                            candidate.givenName,
                            candidate.familyName);
                }
            });
        }
    }
	
	private void addTableRow(long ticketId, long userId, String partyAbr,
                             String colour, String candidateName,
                             String candidatePosition, long positionId,
                             String firstName, String lastName) {
		TableRow tr;
		
		LinearLayout layout = new LinearLayout(getActivity());
		layout.setOrientation(LinearLayout.HORIZONTAL);

        int paddingMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                (float) 6.5, getResources().getDisplayMetrics());
		
		tr = new TableRow(getActivity());
		TableRow.LayoutParams rowParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
		tr.setLayoutParams(rowParams);
		tr.setPadding(0, paddingMargin, 0, paddingMargin);

        int imageSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                (float) 53, getResources().getDisplayMetrics());
		
		ImageView candidateProfileView = new ImageView(getActivity());
		candidateProfileView.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(imageSize, imageSize);
        candidateProfileView.setLayoutParams(layoutParams);
		candidateProfileView.setScaleType(ScaleType.CENTER_CROP);

        isegoria.getAPI().getPhotos(userId).enqueue(new SimpleCallback<PhotosResponse>() {
            @Override
            protected void handleResponse(Response<PhotosResponse> response) {
                PhotosResponse body = response.body();

                if (body != null && body.totalPhotos > 0) {
                    GlideApp.with(CandidateAllFragment.this)
                            .load(body.photos.get(0).thumbnailUrl)
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .into(candidateProfileView);
                }
            }
        });

		candidateProfileView.setPadding(paddingMargin, 0, paddingMargin, 0);
		
		ImageView candidateProfileImage = new ImageView(getActivity());
		candidateProfileImage.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, Gravity.END));
		candidateProfileImage.setScaleType(ScaleType.CENTER_CROP);
		candidateProfileImage.setImageBitmap(decodeSampledBitmapFromResource(getResources(), R.drawable.profilelight, imageSize, imageSize));
		candidateProfileImage.setPadding(paddingMargin, 0, paddingMargin, 0);
        candidateProfileImage.setOnClickListener(view -> {
            FragmentManager fragmentManager2 = getActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction2 = fragmentManager2.beginTransaction();
            ContactProfileFragment fragment2 = new ContactProfileFragment();
            Bundle args = new Bundle();
            args.putLong("ProfileId", userId);
            fragment2.setArguments(args);
            fragmentTransaction2.addToBackStack(null);
            fragmentTransaction2.replace(R.id.election_candidate_frame, fragment2);
            fragmentTransaction2.commit();
        });
		
        TextView textViewParty = new TextView(getActivity());
        textViewParty.setTextColor(Color.parseColor("#FFFFFF"));
        textViewParty.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 10.0f);
        textViewParty.setText(partyAbr);
        textViewParty.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        textViewParty.setGravity(Gravity.CENTER);
        textViewParty.setTypeface(null, Typeface.BOLD);

        isegoria.getAPI().getTicket(ticketId).enqueue(new SimpleCallback<Ticket>() {
            @Override
            protected void handleResponse(Response<Ticket> response) {
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

        int imageSize2 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                (float) 27, getResources().getDisplayMetrics());
        
		LinearLayout partyLayout = new LinearLayout(getActivity());
		partyLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                imageSize, imageSize2);
        params.gravity = Gravity.CENTER_VERTICAL;
        partyLayout.setLayoutParams(params);
		//partyLayout.setBackgroundDrawable(rectShapeDrawable);
		partyLayout.addView(textViewParty);
		
        TextView textViewCandidate = new TextView(getActivity());
        textViewCandidate.setTextColor(Color.parseColor("#3A3F43"));
        textViewCandidate.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14.0f);
        textViewCandidate.setText(candidateName);
        textViewCandidate.setPadding(paddingMargin, 0, paddingMargin, 0);
        textViewCandidate.setGravity(Gravity.START);
        
        TextView textViewPosition = new TextView(getActivity());
        textViewPosition.setTextColor(Color.parseColor("#3A3F43"));
        textViewPosition.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 11.0f);
        textViewPosition.setText(candidatePosition);
        textViewPosition.setPadding(paddingMargin, 0, paddingMargin, 0);
        textViewPosition.setGravity(Gravity.START);

        isegoria.getAPI().getPosition(positionId).enqueue(new SimpleCallback<Position>() {
            @Override
            protected void handleResponse(Response<Position> response) {
                Position position = response.body();
                if (position != null) {
                    textViewPosition.setText(position.name);
                }
            }
        });
        
        View dividierView = new View(getActivity());
        dividierView.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, 1));
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
        
		candidateAllTableLayout.addView(tr);
		candidateAllTableLayout.addView(dividierView);
        firstNames.add(firstName);
        lastnNmes.add(lastName);
        rows.add(tr);
	}
	
	private static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
                                                          int reqWidth, int reqHeight) {

	    // First decode with inJustDecodeBounds=true to check dimensions
	    final BitmapFactory.Options options = new BitmapFactory.Options();
	    options.inJustDecodeBounds = true;
	    BitmapFactory.decodeResource(res, resId, options);

	    // Calculate inSampleSize
	    options.inSampleSize = Utils.calculateInSampleSize(options, reqWidth, reqHeight);

	    // Decode bitmap with inSampleSize set
	    options.inJustDecodeBounds = false;
	    return BitmapFactory.decodeResource(res, resId, options);
	}
}
