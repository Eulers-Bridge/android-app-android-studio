package com.eulersbridge.isegoria.poll;

import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
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

import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.eulersbridge.isegoria.GlideApp;
import com.eulersbridge.isegoria.Isegoria;
import com.eulersbridge.isegoria.models.Contact;
import com.eulersbridge.isegoria.models.PollResult;
import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.models.Poll;
import com.eulersbridge.isegoria.models.PollOption;
import com.eulersbridge.isegoria.network.PollResultsResponse;
import com.eulersbridge.isegoria.network.SimpleCallback;

import org.parceler.Parcels;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import retrofit2.Response;

public class PollVoteFragment extends Fragment {
    private TableLayout pollTableLayout;
	
	private boolean insertedFirstRow = false;

    private long pollId;
    private Contact creator;
    private List<PollOption> options = new ArrayList<>();

    private Isegoria isegoria;

    private final int pollOptionImageViewId;

    private ImageView creatorImageView;

    private final ArrayList<ImageView> tickBoxes = new ArrayList<>();
    private final ArrayList<TextView> pollResults = new ArrayList<>();
    private final ArrayList<ProgressBar> progressBars = new ArrayList<>();

    private int pixelWidth;

	public PollVoteFragment() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            pollOptionImageViewId = View.generateViewId();
        } else {
            pollOptionImageViewId = new Random().nextInt(0xFFFFFF);
        }
    }
	
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.poll_fragment, container, false);

		pollTableLayout = rootView.findViewById(R.id.pollTableLayout);

        DisplayMetrics displayMetrics = getActivity().getResources().getDisplayMetrics();
        pixelWidth = displayMetrics.widthPixels;

        isegoria = (Isegoria)getActivity().getApplication();

        Poll poll = Parcels.unwrap(getArguments().getParcelable("poll"));

        pollId = poll.id;
        creator = poll.getCreator();
        String question = poll.question;
        options = poll.options;

        addTableRow(question, "");

        if (poll.getCreator() == null && !TextUtils.isEmpty(poll.creatorEmail)) {
            isegoria.getAPI().getContact(poll.creatorEmail).enqueue(new SimpleCallback<Contact>() {
                @Override
                protected void handleResponse(Response<Contact> response) {
                    Contact user = response.body();
                    if (user != null) {
                        creator = user;
                        fetchCreatorPhoto();
                    }
                }
            });

        } else if (creator != null) {
            fetchCreatorPhoto();
        }

        for (int i = 0; i < options.size(); i++) {
            PollOption option = options.get(i);

            createProgressBar("#0000FF", option);

            if (option.photo != null && !TextUtils.isEmpty(option.photo.thumbnailUrl)) {
                // Add one to the index, as the first table row is the poll question itself
                int index = i + 1;

                GlideApp.with(this)
                        .load(option.photo.thumbnailUrl)
                        .into(new SimpleTarget<Drawable>() {
                            @Override
                            public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
                                if (resource != null) {
                                    updateTableRowImage(index, resource);
                                }
                            }
                        });
            }
        }

		return rootView;
	}

    private void fetchCreatorPhoto() {
	    if (creator != null && !TextUtils.isEmpty(creator.profilePhotoURL)) {
	        GlideApp.with(this)
                    .load(creator.profilePhotoURL)
                    .into(creatorImageView);
        }
    }

	private final SimpleCallback<Void> pollVoteCallback =  new SimpleCallback<Void>() {
        @Override
        protected void handleResponse(Response response) {
            // After voting successfully, fetch poll results and update with this data
            isegoria.getAPI().getPollResults(pollId).enqueue(new SimpleCallback<PollResultsResponse>() {
                @Override
                protected void handleResponse(Response<PollResultsResponse> response) {
                    PollResultsResponse body = response.body();
                    if (body != null && body.results != null) {

                        List<PollResult> results = body.results;

                        for (int i = 0; i < results.size(); i++) {
                            setPollOptionResult(i, results.get(i).count);
                        }
                    }
                }
            });
        }
    };

    private void updateTableRowImage(int index, @NonNull Drawable drawable) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                View row = pollTableLayout.getChildAt(index);

                if (row != null) {
                    ImageView imageView = row.findViewById(pollOptionImageViewId);

                    if (imageView != null) {
                        imageView.setImageDrawable(drawable);
                    }
                }
            });
        }
    }

    @UiThread
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

		ImageView creatorImageView = new ImageView(getActivity());
		//view.setColorFilter(Color.argb(125, 35, 35, 35));
        creatorImageView.setLayoutParams(new TableRow.LayoutParams(imageSize, imageSize));
        creatorImageView.setScaleType(ScaleType.CENTER_CROP);
        viewLinearLayout.addView(creatorImageView);
        this.creatorImageView = creatorImageView;
		
		LinearLayout linearLayout = new LinearLayout(getActivity());
		linearLayout.setOrientation(LinearLayout.VERTICAL);
		linearLayout.setGravity(Gravity.CENTER_VERTICAL);
		linearLayout.setLayoutParams(new TableRow.LayoutParams((int)(pixelWidth*0.6), TableRow.LayoutParams.WRAP_CONTENT));
		linearLayout.setPadding(paddingMargin1, 0, 0, 0);
        
        TextView textViewArticle = new TextView(getActivity());
        textViewArticle.setSingleLine(false);
        textViewArticle.setTextColor(Color.BLACK);
        //textViewArticle.setBackgroundColor(Color.parseColor("#FFFF00"));
        textViewArticle.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        textViewArticle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18.0f);
        textViewArticle.setText(label);
        textViewArticle.setGravity(Gravity.START);
        
        TextView textViewQuestionAskedBy = new TextView(getActivity());
        textViewQuestionAskedBy.setTextColor(Color.BLACK);
        textViewQuestionAskedBy.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        textViewQuestionAskedBy.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12.0f);
        textViewQuestionAskedBy.setText(caption);
        textViewQuestionAskedBy.setPadding(0, 0, 0, 0);
        textViewQuestionAskedBy.setGravity(Gravity.START);

        if (creator != null) {
            textViewQuestionAskedBy.setText(getString(R.string.poll_asked_by, creator.getFullName()));
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
        noOfAnswersTextView.setText(String.valueOf(options.size()));
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
	
	private void createProgressBar(String color, final PollOption option) {
		TableRow tr = new TableRow(getActivity());
        int paddingMargin1 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                (float)  6.666666667, getResources().getDisplayMetrics());
        int paddingMargin2 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                (float)   20.00000, getResources().getDisplayMetrics());
        int tickImageSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                (float)    26.666666667, getResources().getDisplayMetrics());
        int optionImageSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                (float)    200, getResources().getDisplayMetrics());

		if(!insertedFirstRow) {
			insertedFirstRow = true;
			tr.setPadding(paddingMargin1, paddingMargin1, 0, paddingMargin1);
		}
		else {
			tr.setPadding(paddingMargin1, 0, 0, paddingMargin1);
		}
		TableRow.LayoutParams rowParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
		tr.setLayoutParams(rowParams);
		
		ProgressBar progressBar = new ProgressBar(getActivity(), null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setProgress(0);
        progressBar.setMax(1000);
        progressBar.getProgressDrawable().setColorFilter(Color.parseColor(color), Mode.SRC_IN);
        progressBars.add(progressBar);
		
		LinearLayout linearLayout = new LinearLayout(getActivity());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(paddingMargin1, 0, paddingMargin2, 0);
        linearLayout.setGravity(Gravity.CENTER_VERTICAL);

        if (option.photo != null && !TextUtils.isEmpty(option.photo.thumbnailUrl)) {
            ImageView optionImageView = new ImageView(getActivity());
            optionImageView.setId(pollOptionImageViewId);
            optionImageView.setScaleType(ScaleType.FIT_CENTER);
            optionImageView.setLayoutParams(new TableRow.LayoutParams(optionImageSize, optionImageSize));

            linearLayout.addView(optionImageView);
        }

        LinearLayout.LayoutParams linearWrapHeightParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        linearLayout.addView(progressBar, linearWrapHeightParams);
		
        TextView textViewParty = new TextView(getActivity());
        textViewParty.setSingleLine(false);
        textViewParty.setTextColor(Color.BLACK);
        textViewParty.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16.0f);
        textViewParty.setText(option.text);
        textViewParty.setGravity(Gravity.END);
        
        TextView textViewVotes = new TextView(getActivity());
        textViewVotes.setTextColor(Color.parseColor("#000000"));
        textViewVotes.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16.0f);
        DecimalFormat formatter = new DecimalFormat("#,###");
        textViewVotes.setText(formatter.format(option.votersCount));
        if(!option.hasVoted) {
            textViewVotes.setText("");
        }
        textViewVotes.setPadding(0, 0, 0, 0);
        textViewVotes.setGravity(Gravity.START);
        pollResults.add(textViewVotes);
		
		FrameLayout frameLayout = new FrameLayout(getActivity());

		FrameLayout.LayoutParams frameWrapHeightParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        frameLayout.setPadding(0, 0, 0, 0);
        frameLayout.addView(textViewParty, frameWrapHeightParams);
        frameLayout.addView(textViewVotes, frameWrapHeightParams);
		
		linearLayout.addView(frameLayout, frameWrapHeightParams);
		
		ImageView tickImageView = new ImageView(getActivity());
        tickImageView.setLayoutParams(new TableRow.LayoutParams(tickImageSize, tickImageSize));
        tickImageView.setScaleType(ScaleType.CENTER_INSIDE);
        tickImageView.setImageResource(option.hasVoted? R.drawable.tickgreen : R.drawable.tickempty);

        tickBoxes.add(tickImageView);

        tickImageView.setOnClickListener(view -> {
            for (ImageView tickBox : tickBoxes) {
                tickBox.setImageResource(R.drawable.tickempty);
            }
            ((ImageView) view).setImageResource(R.drawable.tickgreen);

            isegoria.getAPI().answerPoll(pollId, option.id).enqueue(pollVoteCallback);
        });
		
        tr.addView(tickImageView);
		tr.addView(linearLayout);

		pollTableLayout.addView(tr);
	}

    private void setPollOptionResult(int index, long count) {
        pollResults.get(index).setText(String.valueOf(count));
        progressBars.get(index).setProgress((int)count);
    }
}
