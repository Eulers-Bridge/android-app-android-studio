package com.eulersbridge.isegoria.election;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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

import com.eulersbridge.isegoria.ContactProfileFragment;
import com.eulersbridge.isegoria.MainActivity;
import com.eulersbridge.isegoria.Network;
import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.utilities.Utils;

@SuppressWarnings("deprecation")
public class CandidateTicketDetailFragment extends Fragment {
	private View rootView;
	private TableLayout candidateTicketDetialTableLayout;
    private Button ticketSupportButton;
	
	private float dpWidth;

    private String code;
    private String colour;
    private String partyColour = "";
    private String partyLogo = "";

    private TextView partyDetailSupporters;
    private CandidateTicketDetailFragment candidateTicketDetailFragment;
    private Network network;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        candidateTicketDetailFragment = this;
		rootView = inflater.inflate(R.layout.candidate_ticket_detail_fragment, container, false);
		Bundle bundle = this.getArguments();
		int backgroundDrawableResource = R.drawable.me;
        final int ticketId = bundle.getInt("TicketId");
        final String ticketName = bundle.getString("TicketName");
        final int noOfSupporters = bundle.getInt("NoOfSupporters");
        partyColour = bundle.getString("Colour");
        partyLogo = bundle.getString("Logo");

		candidateTicketDetialTableLayout = rootView.findViewById(R.id.candidateTicketDetailTable);
        partyDetailSupporters = rootView.findViewById(R.id.partyDetailSupporters);

		DisplayMetrics displayMetrics = getActivity().getResources().getDisplayMetrics();
		dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        float dpHeight = displayMetrics.heightPixels / displayMetrics.density;
		
		LinearLayout backgroundLinearLayout = rootView.findViewById(R.id.topBackgroundDetail);
		Bitmap original = BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.birmingham);
		Bitmap b = Bitmap.createScaledBitmap(original, (int)dpWidth, (int) dpHeight /2, false);
		Drawable d = new BitmapDrawable(getActivity().getResources(), Utils.fastBlur(b, 25));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            backgroundLinearLayout.setBackground(d);
        } else {
            backgroundLinearLayout.setBackgroundDrawable(d);
        }

        MainActivity mainActivity = (MainActivity) getActivity();
        network = mainActivity.getIsegoriaApplication().getNetwork();
        network.getTicketDetail(ticketId, candidateTicketDetailFragment);

        ImageView partyDetailLogo = rootView.findViewById(R.id.partyDetailLogo);
        network.getFirstPhoto(ticketId, partyDetailLogo);

        ticketSupportButton = rootView.findViewById(R.id.supportButton);

        if(network.getUserTickets().contains(ticketId)) {
            ticketSupportButton.setText("Unsupport");
        }

        ticketSupportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ticketSupportButton.getText().equals("Support")) {
                    network.supportTicket(ticketId, candidateTicketDetailFragment);
                    TextView partyDetailSupporters = rootView.findViewById(R.id.partyDetailSupporters);
                    String value = String.valueOf(partyDetailSupporters.getText());
                    partyDetailSupporters.setText(String.valueOf(Integer.parseInt(value) + 1));
                    ticketSupportButton.setText("Unsupport");
                }
                else if(ticketSupportButton.getText().equals("Unsupport")) {
                    network.unsupportTicket(ticketId, candidateTicketDetailFragment);
                    TextView partyDetailSupporters = rootView.findViewById(R.id.partyDetailSupporters);
                    String value = String.valueOf(partyDetailSupporters.getText());
                    partyDetailSupporters.setText(String.valueOf(Integer.parseInt(value) - 1));
                    ticketSupportButton.setText("Support");
                }
            }
        });

        TextView partyDetailName = rootView.findViewById(R.id.partyNameDetail);
        partyDetailSupporters.setText(String.valueOf(noOfSupporters));
        partyDetailName.setText(ticketName);

		return rootView;
	}

    public void updateInformation(int ticketId, int electionId, String name, String code,
                                  String information, String colour) {
        this.code = code;
        this.colour = colour;

        network.getTicketCandidates(this, ticketId);
        rootView.invalidate();
    }

    public void addCandidate(int userId, int ticketId, int positionId, int candidateId,
                             String firstName, String lastName) {
        addTableRow(userId, code, colour, firstName + " " + lastName, "", userId, positionId);
    }
	
	private void addTableRow(final int profileDrawable, String partyAbr, String colour, String candidateName, String candidatePosition, int userId, final int positionId) {
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
		candidateProfileView.setImageBitmap(decodeSampledBitmapFromResource(getResources(), profileDrawable, imageHeight, imageHeight));
		candidateProfileView.setPadding(paddingMargin3, 0, paddingMargin3, 0);

        network.getFirstPhoto(userId, candidateProfileView);
		
		ImageView candidateProfileImage = new ImageView(getActivity());
		candidateProfileImage.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 
				Gravity.END));
		candidateProfileImage.setScaleType(ScaleType.CENTER_CROP);
		candidateProfileImage.setImageBitmap(decodeSampledBitmapFromResource(getResources(), R.drawable.profilelight, imageHeight, imageHeight));
		candidateProfileImage.setPadding(paddingMargin3, 0, paddingMargin3, 0);

        candidateProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fragmentManager2 = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction2 = fragmentManager2.beginTransaction();
                ContactProfileFragment fragment2 = new ContactProfileFragment();
                Bundle args = new Bundle();
                args.putInt("ProfileId", profileDrawable);
                fragment2.setArguments(args);
                fragmentTransaction2.addToBackStack(null);
                fragmentTransaction2.replace(android.R.id.content, fragment2);
                fragmentTransaction2.commit();
            }
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            partyLayout.setBackground(rectShapeDrawable);
        } else {
            partyLayout.setBackgroundDrawable(rectShapeDrawable);
        }
        partyLayout.addView(textViewParty);
		
        TextView textViewCandidate = new TextView(getActivity());
        textViewCandidate.setTextColor(Color.parseColor("#3A3F43"));
        textViewCandidate.setTextSize(TypedValue.COMPLEX_UNIT_DIP,16.0f);
        textViewCandidate.setText(candidateName);
        textViewCandidate.setPadding(paddingMargin3, 0, paddingMargin3, 0);
        textViewCandidate.setGravity(Gravity.START);
        
        TextView textViewPosition = new TextView(getActivity());
        textViewPosition.setTextColor(Color.parseColor("#3A3F43"));
        textViewPosition.setTextSize(TypedValue.COMPLEX_UNIT_DIP,12.0f);
        textViewPosition.setText(candidatePosition);
        textViewPosition.setPadding(paddingMargin3, 0, paddingMargin3, 0);
        textViewPosition.setGravity(Gravity.START);

        network.getPositionText(textViewPosition, positionId);
        
        View dividierView = new View(getActivity());
        dividierView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, 1));
        dividierView.setBackgroundColor(Color.parseColor("#676475"));

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
        candidateTicketDetialTableLayout.addView(dividierView);
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
