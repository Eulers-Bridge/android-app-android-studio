package com.eulersbridge.isegoria.election;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Build;
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
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TableRow.LayoutParams;
import android.widget.TextView;

import com.eulersbridge.isegoria.MainActivity;
import com.eulersbridge.isegoria.Network;
import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.models.CandidateTicket;

import java.util.ArrayList;

@SuppressWarnings("deprecation")
public class CandidateTicketFragment extends Fragment {
	private TableLayout positionsTableLayout;
	
	private float dpWidth;

	private long lastTicketId;
    private String lastName;
    private String lastNoOfSupporters;
    private String lastColour;
    private String lastInformation;
    private String lastLogo;

    private boolean added = false;
    private int addedCounter = 0;

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		DisplayMetrics displayMetrics = getActivity().getResources().getDisplayMetrics();

		View rootView = inflater.inflate(R.layout.election_positions_fragment, container, false);
		positionsTableLayout = rootView.findViewById(R.id.positionsTableLayout);

		dpWidth = displayMetrics.widthPixels / displayMetrics.density;

        MainActivity mainActivity = (MainActivity) getActivity();
		Network network = mainActivity.getIsegoriaApplication().getNetwork();
        network.getTickets(new Network.TicketsListener() {
			@Override
			public void onFetchSuccess(ArrayList<CandidateTicket> tickets) {
				addTickets(tickets);
			}

			@Override
			public void onFetchFailure(Exception e) { }
		});
        network.getUserSupportedTickets();
        
		return rootView;
	}

	private void addTickets(ArrayList<CandidateTicket> tickets) {
		if (getActivity() != null && tickets.size() > 0) {
			getActivity().runOnUiThread(() -> {

				for (CandidateTicket ticket : tickets) {

					addedCounter = addedCounter + 1;
					if (added) {
						this.addTableRow(
								lastTicketId,
								ticket.getId(),
								lastColour, ticket.getColour(),
								true,
								false,
								lastName,
								ticket.getName(),
								lastNoOfSupporters,
								ticket.getSupportersCount(),
								lastLogo,
								ticket.getLogo());
					}

					lastTicketId = ticket.getId();
					lastName = ticket.getName();
					lastInformation = ticket.getInformation();
					lastNoOfSupporters = ticket.getSupportersCount();
					lastColour = ticket.getColour();
					lastLogo = ticket.getLogo();

					added = !added;

					if (tickets.size() == addedCounter && (tickets.size() % 2) != 0) {
						this.addTableRowOneSquare(ticket.getId(), ticket.getColour(), ticket.getName(), ticket.getSupportersCount(), ticket.getLogo());
					}
				}
			});
		}
	}
	
	private void addTableRow(long lastTicketId, long ticketId, String colour1,
							 String colour2, boolean doubleCell, boolean lastCell,
							 String title1, String title2, String supporters1,
							 String supporters2, String logo1, String logo2) {
		TableRow tr;

        int paddingMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                (float) 3.2, getResources().getDisplayMetrics());
        int paddingMargin2 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                (float) 90, getResources().getDisplayMetrics());
        int paddingMargin3 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                (float) 6.666666667, getResources().getDisplayMetrics());
        int paddingMargin4 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                (float) 83.33, getResources().getDisplayMetrics());
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
	        textViewTitle.setTextColor(Color.parseColor("#3A3F43"));
	        textViewTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP,16.0f);
	        textViewTitle.setText(title1);
	        textViewTitle.setPadding(paddingMargin3, 0, paddingMargin3, 0);
	        textViewTitle.setGravity(Gravity.CENTER);
	        
	        TextView textViewTitleSuport1 = new TextView(getActivity());
	        textViewTitleSuport1.setTextColor(Color.parseColor(colour1));
	        textViewTitleSuport1.setTextSize(TypedValue.COMPLEX_UNIT_DIP,16.0f);
	        textViewTitleSuport1.setText(supporters1);
	        textViewTitleSuport1.setPadding(paddingMargin3, 0, paddingMargin3, 0);
	        textViewTitleSuport1.setGravity(Gravity.CENTER);
	        
	        RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);  
	        params1.addRule(RelativeLayout.CENTER_HORIZONTAL, textViewTitle.getId());
	        params1.addRule(RelativeLayout.CENTER_VERTICAL, textViewTitle.getId());
			
	        RectShape rect = new RectShape();
	        ShapeDrawable rectShapeDrawable = new ShapeDrawable(rect);
	        Paint paint = rectShapeDrawable.getPaint();
	        paint.setColor(Color.parseColor(colour1));
	        paint.setStyle(Style.STROKE);
	        paint.setStrokeWidth(paddingMargin);	        
	        
			View view = new View(getActivity());
			view.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT));
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
				view.setBackground(rectShapeDrawable);
			} else {
				view.setBackgroundDrawable(rectShapeDrawable);
			}
	        view.setOnClickListener(view12 -> {
                    FragmentManager fragmentManager2 = getActivity().getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction2 = fragmentManager2.beginTransaction();
                    CandidateTicketDetailFragment fragment2 = new CandidateTicketDetailFragment();
                    Bundle args = new Bundle();
                    args.putLong("TicketId", lastTicketId);
                    args.putString("TicketName", title1);
                    args.putString("Colour", colour1);
                    args.putInt("NoOfSupporters", Integer.parseInt(supporters1));
                    args.putString("Logo", logo1);

                    fragment2.setArguments(args);
                    fragmentTransaction2.addToBackStack(null);
                    fragmentTransaction2.add(R.id.candidate_frame1, fragment2);
                    fragmentTransaction2.commit();
            });

	        LinearLayout linLayout = new LinearLayout(getActivity());
	        linLayout.setOrientation(LinearLayout.VERTICAL);
	        LayoutParams linLayoutParam = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT); 
	        linLayout.addView(textViewTitle);
	        linLayout.addView(textViewTitleSuport1);
			
	        relativeLayout.addView(view);
	        relativeLayout.addView(linLayout, params1);
	        tr.addView(relativeLayout);
	        
			relativeLayout = new RelativeLayout(getActivity());
			relativeLayout.setLayoutParams(new TableRow.LayoutParams((int)(dpWidth / 2), imageHeight));
			if(lastCell)
				((ViewGroup.MarginLayoutParams) relativeLayout.getLayoutParams()).setMargins(0, paddingMargin, paddingMargin, paddingMargin);
			else
				((ViewGroup.MarginLayoutParams) relativeLayout.getLayoutParams()).setMargins(0, paddingMargin, paddingMargin, 0);
			
	        textViewTitle = new TextView(getActivity());
	        textViewTitle.setTextColor(Color.parseColor("#3A3F43"));
	        textViewTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP,16.0f);
	        textViewTitle.setText(title2);
	        textViewTitle.setPadding(paddingMargin3, 0, paddingMargin3, 0);
	        textViewTitle.setGravity(Gravity.CENTER);
	        
	        TextView textViewTitleSuport2 = new TextView(getActivity());
	        textViewTitleSuport2.setTextColor(Color.parseColor(colour2));
	        textViewTitleSuport2.setTextSize(TypedValue.COMPLEX_UNIT_DIP,16.0f);
	        textViewTitleSuport2.setText(supporters1);
	        textViewTitleSuport2.setPadding(paddingMargin3, 0, paddingMargin3, 0);
	        textViewTitleSuport2.setGravity(Gravity.CENTER);
	        
	        params1 = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);  
	        params1.addRule(RelativeLayout.CENTER_HORIZONTAL, textViewTitle.getId());
	        params1.addRule(RelativeLayout.CENTER_VERTICAL, textViewTitle.getId());
			
	        RectShape rect2 = new RectShape();
	        ShapeDrawable rect2ShapeDrawable = new ShapeDrawable(rect2);
	        Paint paint2 = rect2ShapeDrawable.getPaint();
	        paint2.setColor(Color.parseColor(colour2));
	        paint2.setStyle(Style.STROKE);
	        paint2.setStrokeWidth(paddingMargin);
	        
	        linLayout = new LinearLayout(getActivity());
	        linLayout.setOrientation(LinearLayout.VERTICAL);
	        linLayoutParam = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT); 
	        linLayout.addView(textViewTitle);
	        linLayout.addView(textViewTitleSuport2);
	        
			view = new View(getActivity());
			view.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT));
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
				view.setBackground(rect2ShapeDrawable);
			} else {
				view.setBackgroundDrawable(rect2ShapeDrawable);
			}
	        view.setOnClickListener(view1 -> {
                    FragmentManager fragmentManager2 = getActivity().getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction2 = fragmentManager2.beginTransaction();
                    CandidateTicketDetailFragment fragment2 = new CandidateTicketDetailFragment();
                    Bundle args = new Bundle();
                    args.putLong("TicketId", ticketId);
                    args.putString("TicketName", title2);
                    args.putString("Colour", colour2);
                    args.putInt("NoOfSupporters", Integer.parseInt(supporters2));
                    args.putString("Logo", logo2);
                    fragment2.setArguments(args);
                    fragmentTransaction2.addToBackStack(null);
                    fragmentTransaction2.add(R.id.candidate_frame1, fragment2);
                    fragmentTransaction2.commit();
            });
	        
	        relativeLayout.addView(view);
	        relativeLayout.addView(linLayout, params1);
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
			
			View view = new View(getActivity());
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

    private void addTableRowOneSquare(long ticketId, String colour1, String title1,
									  String supporters1, String logo1) {
        TableRow tr;

        int paddingMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                (float) 3.2, getResources().getDisplayMetrics());
        int paddingMargin2 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                (float) 90, getResources().getDisplayMetrics());
        int paddingMargin3 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                (float) 6.666666667, getResources().getDisplayMetrics());
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
            textViewTitle.setTextColor(Color.parseColor("#3A3F43"));
            textViewTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP,16.0f);
            textViewTitle.setText(title1);
            textViewTitle.setPadding(paddingMargin3, 0, paddingMargin3, 0);
            textViewTitle.setGravity(Gravity.CENTER);

            TextView textViewTitleSuport1 = new TextView(getActivity());
            textViewTitleSuport1.setTextColor(Color.parseColor(colour1));
            textViewTitleSuport1.setTextSize(TypedValue.COMPLEX_UNIT_DIP,16.0f);
            textViewTitleSuport1.setText(supporters1);
            textViewTitleSuport1.setPadding(paddingMargin3, 0, paddingMargin3, 0);
            textViewTitleSuport1.setGravity(Gravity.CENTER);

            RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            params1.addRule(RelativeLayout.CENTER_HORIZONTAL, textViewTitle.getId());
            params1.addRule(RelativeLayout.CENTER_VERTICAL, textViewTitle.getId());

            RectShape rect = new RectShape();
            ShapeDrawable rectShapeDrawable = new ShapeDrawable(rect);
            Paint paint = rectShapeDrawable.getPaint();
            paint.setColor(Color.parseColor(colour1));
            paint.setStyle(Style.STROKE);
            paint.setStrokeWidth(paddingMargin);

            View view = new View(getActivity());
            view.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT));
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
				view.setBackground(rectShapeDrawable);
			} else {
				view.setBackgroundDrawable(rectShapeDrawable);
			}

            view.setOnClickListener(view1 -> {
                FragmentManager fragmentManager2 = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction2 = fragmentManager2.beginTransaction();
                CandidateTicketDetailFragment fragment2 = new CandidateTicketDetailFragment();
                Bundle args = new Bundle();
                args.putLong("TicketId", lastTicketId);
                args.putString("TicketName", title1);
                args.putString("Colour", colour1);
                args.putInt("NoOfSupporters", Integer.parseInt(supporters1));
                args.putString("Logo", logo1);

                fragment2.setArguments(args);
                fragmentTransaction2.addToBackStack(null);
                fragmentTransaction2.add(R.id.candidate_frame1, fragment2);
                fragmentTransaction2.commit();
            });

            LinearLayout linLayout = new LinearLayout(getActivity());
            linLayout.setOrientation(LinearLayout.VERTICAL);
            LayoutParams linLayoutParam = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            linLayout.addView(textViewTitle);
            linLayout.addView(textViewTitleSuport1);

            relativeLayout.addView(view);
            relativeLayout.addView(linLayout, params1);
            tr.addView(relativeLayout);

            positionsTableLayout.addView(tr);
    }
}
