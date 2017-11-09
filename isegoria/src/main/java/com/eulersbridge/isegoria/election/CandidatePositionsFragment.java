package com.eulersbridge.isegoria.election;


import android.graphics.Color;
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
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TableRow.LayoutParams;
import android.widget.TextView;

import com.eulersbridge.isegoria.GlideApp;
import com.eulersbridge.isegoria.Isegoria;
import com.eulersbridge.isegoria.models.Election;
import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.models.Position;
import com.eulersbridge.isegoria.network.PhotosResponse;
import com.eulersbridge.isegoria.network.SimpleCallback;
import com.eulersbridge.isegoria.utilities.TintTransformation;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CandidatePositionsFragment extends Fragment {
	private TableLayout positionsTableLayout;
    private Isegoria isegoria;
	
	private float dpWidth;

    private long lastElectionId;
    private long lastPositionId;
    private String lastName;
    private String lastDesc;

    private boolean addRow = false;
    private int addedPositionsCount = 0;

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		DisplayMetrics displayMetrics = getActivity().getResources().getDisplayMetrics();

		View rootView = inflater.inflate(R.layout.election_positions_fragment, container, false);
		positionsTableLayout = rootView.findViewById(R.id.positionsTableLayout);

		dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        
        /*addTableRow(R.drawable.photo0, R.drawable.photo1, true, false, "President", "Secretary");
        addTableRow(R.drawable.photo2, R.drawable.photo3, true, false, "Women's Officer", "LGBT Officer");
        addTableRow(R.drawable.photo4, R.drawable.photopaddingMargin, true, false, "Clubs and Societies", "Environment Officer");
        addTableRow(R.drawable.photo6, R.drawable.photo7, true, false, "Welfare Officer", "Creative Arts Officer");
        addTableRow(R.drawable.photo8, R.drawable.photo9, true, false, "Faculty Liaison", "");*/

        isegoria = (Isegoria)getActivity().getApplication();

		long institutionId = isegoria.getLoggedInUser().institutionId;

        isegoria.getAPI().getElections(institutionId).enqueue(electionsCallback);
        
		return rootView;
	}

	private final Callback<List<Election>> electionsCallback = new Callback<List<Election>>() {
		@Override
		public void onResponse(Call<List<Election>> call, Response<List<Election>> response) {
			List<Election> elections = response.body();
			if (elections != null && elections.size() > 0) {
				Election election = elections.get(0);

				isegoria.getAPI().getElectionPositions(election.id).enqueue(positionsCallback);
			}
		}

		@Override
		public void onFailure(Call<List<Election>> call, Throwable t) {
			t.printStackTrace();
		}
	};

	private final Callback<List<Position>> positionsCallback = new Callback<List<Position>>() {
		@Override
		public void onResponse(Call<List<Position>> call, Response<List<Position>> response) {
			List<Position> positions = response.body();
			if (positions != null) {
				addPositions(positions);
			}
		}

		@Override
		public void onFailure(Call<List<Position>> call, Throwable t) {
			t.printStackTrace();
		}
	};

	private void addPositions(List<Position> positions) {
	    if (getActivity() != null && positions.size() > 0) {
	        getActivity().runOnUiThread(() -> {

	            for (Position position : positions) {

                    addedPositionsCount = addedPositionsCount + 1;
                    if(addRow) {
                        addTableRow(lastElectionId, position.electionId, lastPositionId, position.id, true, false, lastName, position.name);
                    }

                    lastElectionId = position.electionId;
                    lastPositionId = position.id;
                    lastName = position.name;
                    lastDesc = position.description;

                    addRow = !addRow;

                    if (positions.size() == addedPositionsCount) {
                        this.addTableRowOneSquare(position.electionId, position.id, position.name, position.description);
                    }
                }
            });
        }
    }
	
	private void addTableRow(long lastElectionId, long electionId, long lastPositionId, long positionId, boolean doubleCell, boolean lastCell, String title1, String title2) {
		TableRow tr;

        int paddingMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                (float) 3.2, getResources().getDisplayMetrics());
        int imageHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                (float) 170, getResources().getDisplayMetrics());
		
		if(doubleCell) {
			tr = new TableRow(getActivity());
			TableRow.LayoutParams rowParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT);
			tr.setLayoutParams(rowParams);
			
			RelativeLayout relativeLayout = new RelativeLayout(getActivity());
			relativeLayout.setLayoutParams(new TableRow.LayoutParams((int)(dpWidth / 2), imageHeight));
			if(lastCell)
				((ViewGroup.MarginLayoutParams) relativeLayout.getLayoutParams()).setMargins(paddingMargin, paddingMargin, paddingMargin, paddingMargin);
			else
				((ViewGroup.MarginLayoutParams) relativeLayout.getLayoutParams()).setMargins(paddingMargin, paddingMargin, paddingMargin, 0);

	        TextView textViewTitle = new TextView(getActivity());
	        textViewTitle.setTextColor(Color.parseColor("#F8F8F8"));
	        textViewTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP,16.0f);
	        textViewTitle.setText(title1);
	        textViewTitle.setPadding(10, 0, 10, 0);
	        textViewTitle.setGravity(Gravity.CENTER);
	        
	        RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);  
	        params1.addRule(RelativeLayout.CENTER_HORIZONTAL, textViewTitle.getId());
	        params1.addRule(RelativeLayout.CENTER_VERTICAL, textViewTitle.getId());
	        
	        RelativeLayout.LayoutParams params2 = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);  
	        params2.addRule(RelativeLayout.CENTER_HORIZONTAL, textViewTitle.getId());
	        params2.addRule(RelativeLayout.CENTER_VERTICAL, textViewTitle.getId());
			
			ImageView view = new ImageView(getActivity());
			view.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT));
			view.setScaleType(ScaleType.CENTER_CROP);

			isegoria.getAPI().getPhotos(lastPositionId).enqueue(new SimpleCallback<PhotosResponse>() {
				@Override
				protected void handleResponse(Response<PhotosResponse> response) {
					PhotosResponse body = response.body();

					if (body != null && body.totalPhotos > 0) {
						GlideApp.with(CandidatePositionsFragment.this)
								.load(body.photos.get(0).thumbnailUrl)
                                .transforms(new TintTransformation())
								.into(view);
					}
				}
			});

	        view.setOnClickListener(view12 -> {
                FragmentManager fragmentManager2 = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction2 = fragmentManager2.beginTransaction();
                CandidatePositionFragment fragment2 = new CandidatePositionFragment();
                Bundle args = new Bundle();
                args.putLong("PositionId", positionId);
                fragment2.setArguments(args);
                fragmentTransaction2.addToBackStack(null);
                fragmentTransaction2.add(R.id.candidate_frame1, fragment2);
                fragmentTransaction2.commit();
            });
	        relativeLayout.addView(view);
	        relativeLayout.addView(textViewTitle, params1);
	        tr.addView(relativeLayout);
	        
			relativeLayout = new RelativeLayout(getActivity());
			relativeLayout.setLayoutParams(new TableRow.LayoutParams((int)(dpWidth / 2), imageHeight));
			if(lastCell)
				((ViewGroup.MarginLayoutParams) relativeLayout.getLayoutParams()).setMargins(0, paddingMargin, paddingMargin, paddingMargin);
			else
				((ViewGroup.MarginLayoutParams) relativeLayout.getLayoutParams()).setMargins(0, paddingMargin, paddingMargin, 0);
			
	        textViewTitle = new TextView(getActivity());
	        textViewTitle.setTextColor(Color.parseColor("#F8F8F8"));
	        textViewTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP,16.0f);
	        textViewTitle.setText(title2);
	        textViewTitle.setPadding(10, 0, 10, 0);
	        textViewTitle.setGravity(Gravity.CENTER);
	        
	        params1 = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);  
	        params1.addRule(RelativeLayout.CENTER_HORIZONTAL, textViewTitle.getId());
	        params1.addRule(RelativeLayout.CENTER_VERTICAL, textViewTitle.getId());
	        
	        params2 = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);  
	        params2.addRule(RelativeLayout.CENTER_HORIZONTAL, textViewTitle.getId());
	        params2.addRule(RelativeLayout.CENTER_VERTICAL, textViewTitle.getId());
			
			ImageView view2 = new ImageView(getActivity());
            view2.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT));
            view2.setScaleType(ScaleType.CENTER_CROP);

            isegoria.getAPI().getPhotos(positionId).enqueue(new SimpleCallback<PhotosResponse>() {
                @Override
                protected void handleResponse(Response<PhotosResponse> response) {
                    PhotosResponse body = response.body();

                    if (body != null && body.totalPhotos > 0) {
                        GlideApp.with(CandidatePositionsFragment.this)
                                .load(body.photos.get(0).thumbnailUrl)
                                .transforms(new TintTransformation())
                                .into(view2);
                    }
                }
            });

            view2.setOnClickListener(view1 -> {
                FragmentManager fragmentManager2 = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction2 = fragmentManager2.beginTransaction();
                CandidatePositionFragment fragment2 = new CandidatePositionFragment();
                Bundle args = new Bundle();
                args.putLong("PositionId", positionId);
                fragment2.setArguments(args);
                fragmentTransaction2.addToBackStack(null);
                fragmentTransaction2.add(R.id.candidate_frame1, fragment2);
                fragmentTransaction2.commit();
            });
	        relativeLayout.addView(view2);
	        relativeLayout.addView(textViewTitle, params1);
	        tr.addView(relativeLayout);
	        
	        positionsTableLayout.addView(tr);
		}
		else {
			tr = new TableRow(getActivity());
			TableRow.LayoutParams rowParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT);
			tr.setLayoutParams(rowParams);
			
			RelativeLayout relativeLayout = new RelativeLayout(getActivity());
			relativeLayout.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, imageHeight));
			((TableRow.LayoutParams) relativeLayout.getLayoutParams()).span = 2;
			if(lastCell)
				((ViewGroup.MarginLayoutParams) relativeLayout.getLayoutParams()).setMargins(paddingMargin, paddingMargin, paddingMargin, paddingMargin);
			else
				((ViewGroup.MarginLayoutParams) relativeLayout.getLayoutParams()).setMargins(paddingMargin, paddingMargin, paddingMargin, 0);
			
			ImageView view = new ImageView(getActivity());
			view.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, imageHeight));
	        
	        TextView textViewTitle = new TextView(getActivity());
	        textViewTitle.setTextColor(Color.parseColor("#F8F8F8"));
	        textViewTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP,20.0f);
	        textViewTitle.setText(title1);
	        textViewTitle.setGravity(Gravity.CENTER);
	        
	        RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);  
	        params1.addRule(RelativeLayout.CENTER_HORIZONTAL, textViewTitle.getId());
	        params1.addRule(RelativeLayout.CENTER_VERTICAL, textViewTitle.getId());
	        
	        RelativeLayout.LayoutParams params2 = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);  
	        params2.addRule(RelativeLayout.CENTER_HORIZONTAL, textViewTitle.getId());
	        params2.addRule(RelativeLayout.CENTER_VERTICAL, textViewTitle.getId());
	        
	        relativeLayout.addView(view);
	        relativeLayout.addView(textViewTitle, params1);
	        
	        tr.addView(relativeLayout);	
	        positionsTableLayout.addView(tr);
		}
	}

    private void addTableRowOneSquare(long electionId, long positionId,
									  String title1, String desc1) {
        TableRow tr;

        int paddingMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                (float) 3.2, getResources().getDisplayMetrics());
        int paddingMargin2 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                (float) 90, getResources().getDisplayMetrics());
        int paddingMargin3 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                (float) 23.333, getResources().getDisplayMetrics());
        int paddingMargin4 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                (float) 83.33, getResources().getDisplayMetrics());
        int imageHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                (float) 170, getResources().getDisplayMetrics());

            tr = new TableRow(getActivity());
            TableRow.LayoutParams rowParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT);
            tr.setLayoutParams(rowParams);

            RelativeLayout relativeLayout = new RelativeLayout(getActivity());
            relativeLayout.setLayoutParams(new TableRow.LayoutParams((int)(dpWidth / 2), imageHeight));
            ((ViewGroup.MarginLayoutParams) relativeLayout.getLayoutParams()).setMargins(paddingMargin, paddingMargin, paddingMargin, 0);

            TextView textViewTitle = new TextView(getActivity());
            textViewTitle.setTextColor(Color.parseColor("#F8F8F8"));
            textViewTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP,16.0f);
            textViewTitle.setText(title1);
            textViewTitle.setPadding(10, 0, 10, 0);
            textViewTitle.setGravity(Gravity.CENTER);

            RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            params1.addRule(RelativeLayout.CENTER_HORIZONTAL, textViewTitle.getId());
            params1.addRule(RelativeLayout.CENTER_VERTICAL, textViewTitle.getId());

            RelativeLayout.LayoutParams params2 = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            params2.addRule(RelativeLayout.CENTER_HORIZONTAL, textViewTitle.getId());
            params2.addRule(RelativeLayout.CENTER_VERTICAL, textViewTitle.getId());

            ImageView view = new ImageView(getActivity());
            view.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT));

			isegoria.getAPI().getPhotos(lastPositionId).enqueue(new SimpleCallback<PhotosResponse>() {
                @Override
                protected void handleResponse(Response<PhotosResponse> response) {
                    PhotosResponse body = response.body();

                    if (body != null && body.totalPhotos > 0) {
                        GlideApp.with(CandidatePositionsFragment.this)
                                .load(body.photos.get(0).thumbnailUrl)
                                .transforms(new TintTransformation())
                                .into(view);
                    }
                }
			});

            view.setOnClickListener(view1 -> {
                FragmentManager fragmentManager2 = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction2 = fragmentManager2.beginTransaction();
                CandidatePositionFragment fragment2 = new CandidatePositionFragment();
                Bundle args = new Bundle();
                args.putLong("PositionId", positionId);
                fragment2.setArguments(args);
                fragmentTransaction2.addToBackStack(null);
                fragmentTransaction2.add(R.id.candidate_frame1, fragment2);
                fragmentTransaction2.commit();
            });
            relativeLayout.addView(view);
            relativeLayout.addView(textViewTitle, params1);
            tr.addView(relativeLayout);

            positionsTableLayout.addView(tr);
    }
}
