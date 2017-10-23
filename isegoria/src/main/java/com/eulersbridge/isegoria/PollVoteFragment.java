package com.eulersbridge.isegoria;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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

import com.android.volley.VolleyError;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class PollVoteFragment extends Fragment {
    private TableLayout pollTableLayout;
	
	private boolean insertedFirstRow = false;

    private int nodeId;
    private User creator;
	private String question;
	private String answers;
    private int numOfAnswers;
    private Network network;

    private ImageView creatorImageView;
    private Bitmap creatorPhoto;

    private final boolean voted = false;

    private final PollVoteFragment pollVoteFragment;
    private final ArrayList<ImageView> tickBoxes = new ArrayList<>();
    private final ArrayList<TextView> pollResults = new ArrayList<>();
    private final ArrayList<ProgressBar> progressBars = new ArrayList<>();

    private int pixelWidth = 0;

	public PollVoteFragment() {
        pollVoteFragment = this;
	}
	
	public void setData(int nodeId, @Nullable User creator, String question, String answers, int numOfAnswers) {
        this.nodeId = nodeId;
        this.creator = creator;
		this.question = question;
		this.answers = answers;
        this.numOfAnswers = numOfAnswers;

        if (creator != null) {
            network.getPicture(creator.getProfilePhotoURL(), new Network.PictureDownloadListener() {
                @Override
                public void onDownloadFinished(String url, @Nullable Bitmap bitmap) {
                    creatorPhoto = bitmap;

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateCreatorPhoto();
                        }
                    });
                }

                @Override
                public void onDownloadFailed(String url, VolleyError error) { }
            });
        }
	}

	private void updateCreatorPhoto() {
        creatorImageView.setImageBitmap(creatorPhoto);
        creatorImageView.refreshDrawableState();
    }
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.poll_fragment, container, false);
		DisplayMetrics displayMetrics = getActivity().getResources().getDisplayMetrics();
		pollTableLayout = rootView.findViewById(R.id.pollTableLayout);

        pixelWidth = displayMetrics.widthPixels;

        MainActivity mainActivity = (MainActivity) getActivity();
        network = mainActivity.getIsegoriaApplication().getNetwork();

        addTableRow(question, "");
        String[] answersSplit = answers.split(",");

        for (String anAnswersSplit : answersSplit) {
            createProgressBars("#0000FF", anAnswersSplit, 0000);
        }

		return rootView;
	}
	
	private void addTableRow(String label, String caption) {
		TableRow tr = new TableRow(getActivity());

        int paddingMargin1 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                (float)  6.666666667, getResources().getDisplayMetrics());
        int paddingMarginRight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                (float)  10.0, getResources().getDisplayMetrics());
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

        LinearLayout viewLinearLayout = new LinearLayout(getActivity());
        viewLinearLayout.setOrientation(LinearLayout.VERTICAL);
        viewLinearLayout.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
        viewLinearLayout.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT));
        viewLinearLayout.setPadding(0, 0, 0, 0);

		ImageView view = new ImageView(getActivity());
		//view.setColorFilter(Color.argb(125, 35, 35, 35));
		view.setLayoutParams(new TableRow.LayoutParams(imageSize, imageSize));
		view.setScaleType(ScaleType.CENTER_CROP);
        viewLinearLayout.addView(view);
        creatorImageView = view;
		
		LinearLayout linearLayout = new LinearLayout(getActivity());
		linearLayout.setOrientation(LinearLayout.VERTICAL);
		linearLayout.setGravity(Gravity.CENTER_VERTICAL);
		linearLayout.setLayoutParams(new TableRow.LayoutParams((int)(pixelWidth*0.6), TableRow.LayoutParams.WRAP_CONTENT));
		linearLayout.setPadding(paddingMargin1, 0, 0, 0);
        
        TextView textViewArticle = new TextView(getActivity());
        textViewArticle.setSingleLine(false);
        textViewArticle.setTextColor(Color.parseColor("#000000"));
        //textViewArticle.setBackgroundColor(Color.parseColor("#FFFF00"));
        textViewArticle.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        textViewArticle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18.0f);
        textViewArticle.setText(label);
        textViewArticle.setGravity(Gravity.START);
        
        TextView textViewQuestionAskedBy = new TextView(getActivity());
        textViewQuestionAskedBy.setTextColor(Color.parseColor("#000000"));
        textViewQuestionAskedBy.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        textViewQuestionAskedBy.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12.0f);
        textViewQuestionAskedBy.setText(caption);
        textViewQuestionAskedBy.setPadding(0, 0, 0, 0);
        textViewQuestionAskedBy.setGravity(Gravity.START);

        if (creator != null) {
            textViewQuestionAskedBy.setText(String.format("Asked by %s", creator.getFullName()));
        }

        linearLayout.addView(textViewArticle);
        linearLayout.addView(textViewQuestionAskedBy);
        
        tr.addView(viewLinearLayout);

        LinearLayout indicatorsLinearLayout = new LinearLayout(getActivity());
        indicatorsLinearLayout.setOrientation(LinearLayout.VERTICAL);
        indicatorsLinearLayout.setGravity(Gravity.CENTER_VERTICAL);
        indicatorsLinearLayout.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
        indicatorsLinearLayout.setPadding(paddingMargin1, 0, 0, 0);

        LinearLayout textImageAlignLayout = new LinearLayout(getActivity());
        textImageAlignLayout.setOrientation(LinearLayout.HORIZONTAL);
        textImageAlignLayout.setGravity(Gravity.CENTER_VERTICAL);
        textImageAlignLayout.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
        textImageAlignLayout.setPadding(0, 0, 0, 0);

        TextView noOfAnswersTextView = new TextView(getActivity());
        noOfAnswersTextView.setText(String.valueOf(numOfAnswers));
        noOfAnswersTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 10.0f);
        ImageView noOfAnswersImageView = new ImageView(getActivity());
        noOfAnswersImageView.setScaleType(ScaleType.FIT_XY);
        noOfAnswersImageView.setAdjustViewBounds(true);
        noOfAnswersImageView.setImageResource(R.drawable.votecount);
        noOfAnswersImageView.setLayoutParams(
                new TableRow.LayoutParams(imageSize /2, imageSize /2));

        textImageAlignLayout.addView(noOfAnswersTextView);
        textImageAlignLayout.addView(noOfAnswersImageView);
        indicatorsLinearLayout.addView(textImageAlignLayout);

        textImageAlignLayout = new LinearLayout(getActivity());
        textImageAlignLayout.setOrientation(LinearLayout.HORIZONTAL);
        textImageAlignLayout.setGravity(Gravity.CENTER_VERTICAL);
        textImageAlignLayout.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
        textImageAlignLayout.setPadding(0, 0, 0, 0);

        indicatorsLinearLayout.addView(textImageAlignLayout);

        LinearLayout questionStatsLinearLayout = new LinearLayout(getActivity());
        questionStatsLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
        questionStatsLinearLayout.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
        questionStatsLinearLayout.setPadding(0, 0, paddingMarginRight, 0);

        questionStatsLinearLayout.addView(linearLayout);
        //linearLayout.setBackgroundColor(Color.parseColor("#FF0000"));
        questionStatsLinearLayout.addView(indicatorsLinearLayout);

        tr.addView(questionStatsLinearLayout);
        pollTableLayout.addView(tr);
	}
	
	private void createProgressBars(String color, String party, int votes) {
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
        textViewParty.setGravity(Gravity.END);
        
        TextView textViewVotes = new TextView(getActivity());
        textViewVotes.setTextColor(Color.parseColor("#000000"));
        textViewVotes.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16.0f);
        DecimalFormat formatter = new DecimalFormat("#,###");
        textViewVotes.setText(formatter.format(votes));
        if(!voted) {
            textViewVotes.setText("");
        }
        textViewVotes.setPadding(0, 0, 0, 0);
        textViewVotes.setGravity(Gravity.START);
        pollResults.add(textViewVotes);
		
		FrameLayout layout2 = new FrameLayout(getActivity());
		FrameLayout.LayoutParams params1 = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
		layout2.setPadding(0, 0, 0, 0);
		layout2.addView(textViewParty, params1);
		layout2.addView(textViewVotes, params1);
		
		layout1.addView(layout2, params1);
		
		ImageView view = new ImageView(getActivity());
		view.setLayoutParams(new TableRow.LayoutParams(imageSize, imageSize));
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

    public void setPollResult(int index, int count) {
        pollResults.get(index).setText(String.valueOf(count));
        progressBars.get(index).setProgress(count);
    }

    public int getNodeId() {
        return nodeId;
    }
}
