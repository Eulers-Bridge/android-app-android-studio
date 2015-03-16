package com.eulersbridge.isegoria;

import java.text.DecimalFormat;

import com.actionbarsherlock.app.SherlockFragment;

import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.ImageView.ScaleType;

public class PollVoteFragment extends SherlockFragment {
	private View rootView;
	private TableLayout pollTableLayout;
	
	private float dpWidth;
	private float dpHeight;
	
	private boolean insertedFirstRow = false;
	
	private String question;
	private String answers;
	
	public PollVoteFragment() {
		
	}
	
	public void setData(String question, String answers) {
		this.question = question;
		this.answers = answers;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {   
		rootView = inflater.inflate(R.layout.poll_fragment, container, false);
		DisplayMetrics displayMetrics = getActivity().getResources().getDisplayMetrics();
		pollTableLayout = (TableLayout) rootView.findViewById(R.id.pollTableLayout);
		
		dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        dpHeight = displayMetrics.heightPixels / displayMetrics.density;  
		
        addTableRow(question, "Asked by Eve Menedez");
        String[] answersSplit = answers.split(",");
        
        for(int i=0; i<answersSplit.length; i++) {
        	createProgressBars("#0000FF", answersSplit[i], 0000);
        }
        
        //createProgressBars("#FF0000", "Australian Labor Party", 0000);
        //createProgressBars("#00FF00", "The Greens", 0000);
        //createProgressBars("#000000", "Other", 0000);
        //addTableComment("Eva Mendendez", "Lorem ipsum dolor sit er elit lamet, consectetaur cillium adipisicing pecu, sed do eiusmod tempor incididunt ut labore");
        //addTableComment("Eva Mendendez", "Lorem ipsum dolor sit er elit lamet, consectetaur cillium adipisicing pecu, sed do eiusmod tempor incididunt ut labore");
        //addTableComment("Eva Mendendez", "Lorem ipsum dolor sit er elit lamet, consectetaur cillium adipisicing pecu, sed do eiusmod tempor incididunt ut labore");
        //addTableComment("Eva Mendendez", "Lorem ipsum dolor sit er elit lamet, consectetaur cillium adipisicing pecu, sed do eiusmod tempor incididunt ut labore");

		return rootView;
	}
	
	public void addTableRow(String label, String caption) {
		TableRow tr = new TableRow(getActivity());
		if(!insertedFirstRow) {
			insertedFirstRow = true;
			tr.setPadding(10, 10, 0, 10);
		}
		else {
			tr.setPadding(10, 0, 0, 10);
		}
		TableRow.LayoutParams rowParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
		tr.setLayoutParams(rowParams);
		tr.setGravity(Gravity.TOP);
		
		ImageView view = new ImageView(getActivity());
		//view.setColorFilter(Color.argb(125, 35, 35, 35));
		view.setLayoutParams(new TableRow.LayoutParams(75, (int)(75)));
		view.setScaleType(ScaleType.CENTER_CROP);
        view.setImageResource(R.drawable.head1);
		
		LinearLayout linearLayout = new LinearLayout(getActivity());
		linearLayout.setOrientation(LinearLayout.VERTICAL);
		linearLayout.setGravity(Gravity.CENTER_VERTICAL);
		linearLayout.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
		linearLayout.setPadding(10, 0, 0, 0);
        
        TextView textViewArticle = new TextView(getActivity());
        textViewArticle.setSingleLine(false);
        textViewArticle.setTextColor(Color.parseColor("#000000"));
        textViewArticle.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        textViewArticle.setTextSize(18.0f);
        textViewArticle.setText(label);
        textViewArticle.setGravity(Gravity.LEFT);
        
        TextView textViewArticleTime = new TextView(getActivity());
        textViewArticleTime.setTextColor(Color.parseColor("#000000"));
        textViewArticleTime.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        textViewArticleTime.setTextSize(12.0f);
        textViewArticleTime.setText(caption);
        textViewArticleTime.setPadding(0, 0, 0, 0);
        textViewArticleTime.setGravity(Gravity.LEFT);
        
        linearLayout.addView(textViewArticle);
        linearLayout.addView(textViewArticleTime);
        
        tr.addView(view);
        tr.addView(linearLayout);	
        pollTableLayout.addView(tr);
	}
	
	public void createProgressBars(String color, String party, int votes) {
		TableRow tr = new TableRow(getActivity());
		if(!insertedFirstRow) {
			insertedFirstRow = true;
			tr.setPadding(10, 10, 0, 10);
		}
		else {
			tr.setPadding(10, 0, 0, 10);
		}
		TableRow.LayoutParams rowParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
		tr.setLayoutParams(rowParams);
		
		ProgressBar pb = new ProgressBar(getActivity(), null, android.R.attr.progressBarStyleHorizontal);
		pb.setProgress(50);
		pb.setMax(100);
		pb.getProgressDrawable().setColorFilter(Color.parseColor(color), Mode.SRC_IN);
		
		LinearLayout layout1 = new LinearLayout(getActivity());
		layout1.setOrientation(LinearLayout.VERTICAL);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		layout1.setPadding(10, 0, 30, 0);
		layout1.setGravity(Gravity.CENTER_VERTICAL);
		layout1.addView(pb, params);
		
        TextView textViewParty = new TextView(getActivity());
        textViewParty.setSingleLine(false);
        textViewParty.setTextColor(Color.parseColor("#000000"));
        textViewParty.setTextSize(16.0f);
        textViewParty.setText(party);
        textViewParty.setGravity(Gravity.RIGHT);
        
        TextView textViewVotes = new TextView(getActivity());
        textViewVotes.setTextColor(Color.parseColor("#000000"));
        textViewVotes.setTextSize(16.0f);
        DecimalFormat formatter = new DecimalFormat("#,###");
        textViewVotes.setText(formatter.format(votes));
        textViewVotes.setPadding(0, 0, 0, 0);
        textViewVotes.setGravity(Gravity.LEFT);
		
		FrameLayout layout2 = new FrameLayout(getActivity());
		FrameLayout.LayoutParams params1 = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
		layout2.setPadding(0, 0, 0, 0);
		layout2.addView(textViewParty, params1);
		layout2.addView(textViewVotes, params1);
		
		layout1.addView(layout2, params1);
		
		ImageView view = new ImageView(getActivity());
		view.setLayoutParams(new TableRow.LayoutParams(40, (int)(40)));
		view.setScaleType(ScaleType.CENTER_INSIDE);
        view.setImageResource(R.drawable.tick);
		
        tr.addView(view);
		tr.addView(layout1);
		pollTableLayout.addView(tr);
	}
	
	public void addTableComment(String name, String comment) {
		TableRow tr = new TableRow(getActivity());
		if(!insertedFirstRow) {
			insertedFirstRow = true;
			tr.setPadding(10, 10, 0, 10);
		}
		else {
			tr.setPadding(10, 0, 0, 10);
		}
		TableRow.LayoutParams rowParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
		tr.setLayoutParams(rowParams);
		
		ImageView view = new ImageView(getActivity());
		view.setLayoutParams(new TableRow.LayoutParams(75, (int)(75)));
		view.setScaleType(ScaleType.CENTER_CROP);
        view.setImageResource(R.drawable.head1);
		
		LinearLayout linearLayout = new LinearLayout(getActivity());
		linearLayout.setOrientation(LinearLayout.VERTICAL);
		linearLayout.setGravity(Gravity.TOP);
		linearLayout.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
		linearLayout.setPadding(10, 0, 0, 0);
        
        TextView textViewName = new TextView(getActivity());
        textViewName.setSingleLine(false);
        textViewName.setTextColor(Color.parseColor("#000000"));
        textViewName.setTextSize(18.0f);
        textViewName.setText(name);
        textViewName.setGravity(Gravity.LEFT);
        textViewName.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        
        TextView textViewComment = new TextView(getActivity());
        textViewComment.setTextColor(Color.parseColor("#000000"));
        textViewComment.setTextSize(12.0f);
        textViewComment.setText(comment);
        textViewComment.setPadding(0, 0, 0, 0);
        textViewComment.setGravity(Gravity.LEFT);
        textViewComment.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
 
        
        linearLayout.addView(textViewName);
        linearLayout.addView(textViewComment);
        
        tr.addView(view);
        tr.addView(linearLayout);	
        pollTableLayout.addView(tr);
	}
}
