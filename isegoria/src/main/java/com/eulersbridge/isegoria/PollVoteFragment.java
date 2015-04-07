package com.eulersbridge.isegoria;

import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class PollVoteFragment extends SherlockFragment {
	private View rootView;
	private TableLayout pollTableLayout;
	
	private float dpWidth;
	private float dpHeight;
	
	private boolean insertedFirstRow = false;

    private int nodeId;
    private int creatorId;
	private String question;
	private String answers;
    private Network network;

    private boolean voted = false;

    private PollVoteFragment pollVoteFragment;
    private ArrayList<ImageView> tickBoxes = new ArrayList<ImageView>();
    private ArrayList<TextView> pollResults = new ArrayList<TextView>();
    private ArrayList<ProgressBar> progressBars = new ArrayList<ProgressBar>();

	public PollVoteFragment() {
        pollVoteFragment = this;
	}
	
	public void setData(int nodeId, int creatorId, String question, String answers) {
        this.nodeId = nodeId;
        this.creatorId = creatorId;
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

        MainActivity mainActivity = (MainActivity) getActivity();
        network = mainActivity.getIsegoriaApplication().getNetwork();

        addTableRow(creatorId, question, "");
        String[] answersSplit = answers.split(",");

        for(int i=0; i<answersSplit.length; i++) {
            createProgressBars("#0000FF", answersSplit[i], 0000);
        }

		return rootView;
	}
	
	public void addTableRow(int userId, String label, String caption) {
		TableRow tr = new TableRow(getActivity());

        int paddingMargin1 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                (float)  6.666666667, getResources().getDisplayMetrics());
        int imageSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                (float)   50.0000, getResources().getDisplayMetrics());

		if(!insertedFirstRow) {
			insertedFirstRow = true;
			tr.setPadding(paddingMargin1, paddingMargin1, 0, paddingMargin1);
		}
		else {
			tr.setPadding(paddingMargin1, 0, 0, paddingMargin1);
		}
		TableRow.LayoutParams rowParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
		tr.setLayoutParams(rowParams);
		tr.setGravity(Gravity.TOP);

		ImageView view = new ImageView(getActivity());
		//view.setColorFilter(Color.argb(125, 35, 35, 35));
		view.setLayoutParams(new TableRow.LayoutParams(imageSize, (int)(imageSize)));
		view.setScaleType(ScaleType.CENTER_CROP);
        network.getFirstPhoto(0, creatorId, view);
		
		LinearLayout linearLayout = new LinearLayout(getActivity());
		linearLayout.setOrientation(LinearLayout.VERTICAL);
		linearLayout.setGravity(Gravity.CENTER_VERTICAL);
		linearLayout.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
		linearLayout.setPadding(paddingMargin1, 0, 0, 0);
        
        TextView textViewArticle = new TextView(getActivity());
        textViewArticle.setSingleLine(false);
        textViewArticle.setTextColor(Color.parseColor("#000000"));
        textViewArticle.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        textViewArticle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18.0f);
        textViewArticle.setText(label);
        textViewArticle.setGravity(Gravity.LEFT);
        
        TextView textViewArticleTime = new TextView(getActivity());
        textViewArticleTime.setTextColor(Color.parseColor("#000000"));
        textViewArticleTime.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        textViewArticleTime.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12.0f);
        textViewArticleTime.setText(caption);
        textViewArticleTime.setPadding(0, 0, 0, 0);
        textViewArticleTime.setGravity(Gravity.LEFT);

        network.getUserFullName(pollVoteFragment.getCreatorId(), textViewArticleTime, "Asked By ");

        linearLayout.addView(textViewArticle);
        linearLayout.addView(textViewArticleTime);
        
        tr.addView(view);
        tr.addView(linearLayout);	
        pollTableLayout.addView(tr);
	}
	
	public void createProgressBars(String color, String party, int votes) {
		TableRow tr = new TableRow(getActivity());
        int paddingMargin1 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                (float)  6.666666667, getResources().getDisplayMetrics());
        int paddingMargin2 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                (float)   20.00000, getResources().getDisplayMetrics());
        int imageSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                (float)    26.666666667, getResources().getDisplayMetrics());

		if(!insertedFirstRow) {
			insertedFirstRow = true;
			tr.setPadding(paddingMargin1, paddingMargin1, 0, paddingMargin1);
		}
		else {
			tr.setPadding(paddingMargin1, 0, 0, paddingMargin1);
		}
		TableRow.LayoutParams rowParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
		tr.setLayoutParams(rowParams);
		
		ProgressBar pb = new ProgressBar(getActivity(), null, android.R.attr.progressBarStyleHorizontal);
		pb.setProgress(0);
		pb.setMax(1000);
		pb.getProgressDrawable().setColorFilter(Color.parseColor(color), Mode.SRC_IN);
        progressBars.add(pb);
		
		LinearLayout layout1 = new LinearLayout(getActivity());
		layout1.setOrientation(LinearLayout.VERTICAL);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		layout1.setPadding(paddingMargin1, 0, paddingMargin2, 0);
		layout1.setGravity(Gravity.CENTER_VERTICAL);
		layout1.addView(pb, params);
		
        TextView textViewParty = new TextView(getActivity());
        textViewParty.setSingleLine(false);
        textViewParty.setTextColor(Color.parseColor("#000000"));
        textViewParty.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16.0f);
        textViewParty.setText(party);
        textViewParty.setGravity(Gravity.RIGHT);
        
        TextView textViewVotes = new TextView(getActivity());
        textViewVotes.setTextColor(Color.parseColor("#000000"));
        textViewVotes.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16.0f);
        DecimalFormat formatter = new DecimalFormat("#,###");
        textViewVotes.setText(formatter.format(votes));
        if(voted == false) {
            textViewVotes.setText("");
        }
        textViewVotes.setPadding(0, 0, 0, 0);
        textViewVotes.setGravity(Gravity.LEFT);
        pollResults.add(textViewVotes);
		
		FrameLayout layout2 = new FrameLayout(getActivity());
		FrameLayout.LayoutParams params1 = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
		layout2.setPadding(0, 0, 0, 0);
		layout2.addView(textViewParty, params1);
		layout2.addView(textViewVotes, params1);
		
		layout1.addView(layout2, params1);
		
		ImageView view = new ImageView(getActivity());
		view.setLayoutParams(new TableRow.LayoutParams(imageSize, (int)(imageSize)));
		view.setScaleType(ScaleType.CENTER_INSIDE);
        view.setImageResource(R.drawable.tickempty);
        tickBoxes.add(view);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int answerIndex = 0;
                for(int i=0; i<tickBoxes.size(); i++) {
                    tickBoxes.get(i).setImageResource(R.drawable.tickempty);

                    if(tickBoxes.get(i) == view) {
                        answerIndex = i;
                    }
                }
                ((ImageView) view).setImageResource(R.drawable.tickgreen);
                pollVoteFragment.network.answerPoll(nodeId, answerIndex, pollVoteFragment);
            }
        });
		
        tr.addView(view);
		tr.addView(layout1);
		pollTableLayout.addView(tr);
	}
	
	public void addTableComment(String name, String comment) {
		TableRow tr = new TableRow(getActivity());

        int paddingMargin1 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                (float)  6.666666667, getResources().getDisplayMetrics());
        int imageSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                (float)   50.0000, getResources().getDisplayMetrics());

		if(!insertedFirstRow) {
			insertedFirstRow = true;
			tr.setPadding(paddingMargin1, paddingMargin1, 0, paddingMargin1);
		}
		else {
			tr.setPadding(paddingMargin1, 0, 0, paddingMargin1);
		}
		TableRow.LayoutParams rowParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
		tr.setLayoutParams(rowParams);
		
		ImageView view = new ImageView(getActivity());
		view.setLayoutParams(new TableRow.LayoutParams(imageSize, (int)(imageSize)));
		view.setScaleType(ScaleType.CENTER_CROP);
        //view.setImageResource(R.drawable.head1);
		
		LinearLayout linearLayout = new LinearLayout(getActivity());
		linearLayout.setOrientation(LinearLayout.VERTICAL);
		linearLayout.setGravity(Gravity.TOP);
		linearLayout.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
		linearLayout.setPadding(paddingMargin1, 0, 0, 0);
        
        TextView textViewName = new TextView(getActivity());
        textViewName.setSingleLine(false);
        textViewName.setTextColor(Color.parseColor("#000000"));
        textViewName.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18.0f);
        textViewName.setText(name);
        textViewName.setGravity(Gravity.LEFT);
        textViewName.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        
        TextView textViewComment = new TextView(getActivity());
        textViewComment.setTextColor(Color.parseColor("#000000"));
        textViewComment.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12.0f);
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

    public void setPollResult(int index, int count) {
        pollResults.get(index).setText(String.valueOf(count));
        progressBars.get(index).setProgress(count);
    }

    public int getNodeId() {
        return nodeId;
    }

    public int getCreatorId() {
        return creatorId;
    }
}
