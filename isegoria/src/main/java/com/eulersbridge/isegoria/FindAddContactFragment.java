package com.eulersbridge.isegoria;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
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

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;

public class FindAddContactFragment extends SherlockFragment {
    private View rootView;
    private TableLayout usersAllTableLayout;
    private TableLayout friendsAllTableLayout;

    private float dpWidth;
    private float dpHeight;

    private FindAddContactFragment findAddContactFragment;
    private SearchView searchFriendsView;
    private Network network;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        DisplayMetrics displayMetrics = getActivity().getResources().getDisplayMetrics();
        findAddContactFragment = this;

        rootView = inflater.inflate(R.layout.find_add_contact_fragment, container, false);
        ((SherlockFragmentActivity) getActivity()).getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        usersAllTableLayout = (TableLayout) rootView.findViewById(R.id.usersAllTable);
        friendsAllTableLayout = (TableLayout) rootView.findViewById(R.id.friendsAllTableLayout);

        dpWidth = displayMetrics.widthPixels;
        dpHeight = displayMetrics.heightPixels / displayMetrics.density;

        View dividierView = new View(getActivity());
        dividierView.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, 1));
        dividierView.setBackgroundColor(Color.parseColor("#676475"));
        usersAllTableLayout.addView(dividierView);

        MainActivity mainActivity = (MainActivity) getActivity();
        network = mainActivity.getIsegoriaApplication().getNetwork();
        network.findFriends(this);

        searchFriendsView = (SearchView) rootView.findViewById(R.id.searchFriendsView);
        searchFriendsView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                if(query.length() != 0) {
                    network.findContacts(query, findAddContactFragment);
                    return true;
                }
                return false;
            }
        });

        return rootView;
    }

    public void clearSearchResults() {
        usersAllTableLayout.removeAllViews();
    }

    public void addUser(String firstName, String lastName, final String email, String institution, String url) {
        addTableRow(usersAllTableLayout, firstName + " " + lastName, email, institution, url);
        LinearLayout searchResultsLinearLayout = (LinearLayout) rootView.findViewById(R.id.searchResultsLinearLayout);
        searchResultsLinearLayout.setVisibility(ViewGroup.VISIBLE);
    }

    public void addFriend(String firstName, String lastName, final String email, String institution, String url) {
        addTableRow(friendsAllTableLayout, firstName + " " + lastName, email, institution, url);
    }

    public void addTableRow(TableLayout tableLayout, String name, final String email, String institution, String url) {
        TableRow tr;

        LinearLayout layout = new LinearLayout(getActivity());
        layout.setOrientation(LinearLayout.HORIZONTAL);

        tr = new TableRow(getActivity());
        TableRow.LayoutParams rowParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
        tr.setLayoutParams(rowParams);
        tr.setPadding(0, 10, 0, 10);

        ImageView candidateProfileView = new ImageView(getActivity());
        candidateProfileView.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(80, 80);
        candidateProfileView.setLayoutParams(layoutParams);
        candidateProfileView.setScaleType(ScaleType.CENTER_CROP);
        //network.getFirstPhoto(0, userId, candidateProfileView);
        candidateProfileView.setPadding(10, 0, 10, 0);
        network.getPictureVolley(url, candidateProfileView);

        final ImageView candidateProfileImage = new ImageView(getActivity());
        candidateProfileImage.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, Gravity.RIGHT));
        candidateProfileImage.setScaleType(ScaleType.CENTER_CROP);
        candidateProfileImage.setImageBitmap(decodeSampledBitmapFromResource(getResources(), R.drawable.addedinactive, 80, 80));
        candidateProfileImage.setPadding(10, 0, 10, 0);
        candidateProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                candidateProfileImage.setImageBitmap(decodeSampledBitmapFromResource(getResources(), R.drawable.addedactive, 80, 80));
                /*FragmentManager fragmentManager2 = getFragmentManager();
                FragmentTransaction fragmentTransaction2 = fragmentManager2.beginTransaction();
                ContactProfileFragment fragment2 = new ContactProfileFragment();
                Bundle args = new Bundle();
                //args.putInt("ProfileId", userId);
                fragment2.setArguments(args);
                fragmentTransaction2.addToBackStack(null);
                fragmentTransaction2.replace(android.R.id.content, fragment2);
                fragmentTransaction2.commit();*/
                network.addFriend(email, findAddContactFragment);
            }
        });

        TextView textViewCandidate = new TextView(getActivity());
        textViewCandidate.setTextColor(Color.parseColor("#3A3F43"));
        textViewCandidate.setTextSize(16.0f);
        textViewCandidate.setText(name);
        textViewCandidate.setPadding(10, 0, 10, 0);
        textViewCandidate.setGravity(Gravity.LEFT);

        TextView textViewPosition = new TextView(getActivity());
        textViewPosition.setTextColor(Color.parseColor("#3A3F43"));
        textViewPosition.setTextSize(12.0f);
        textViewPosition.setText(institution);
        textViewPosition.setPadding(10, 0, 10, 0);
        textViewPosition.setGravity(Gravity.LEFT);

        //network.getPositionText(textViewPosition, positionId);

        View dividierView = new View(getActivity());
        dividierView.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, 1));
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
        LayoutParams linLayoutParam = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        linLayout.addView(textViewCandidate);
        linLayout.addView(textViewPosition);

        LinearLayout linLayout2 = new LinearLayout(getActivity());
        linLayout2.setOrientation(LinearLayout.VERTICAL);
        LayoutParams linLayoutParam2 = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        linLayout2.addView(candidateProfileImage);
        linLayout2.setGravity(Gravity.RIGHT);
        linLayout2.setLayoutParams(relativeParamsRight);

        layout.addView(candidateProfileView);
        layout.addView(linLayout);
        layout.setLayoutParams(relativeParamsLeft);

        relLayoutMaster.addView(layout);
        relLayoutMaster.addView(linLayout2);

        tr.addView(relLayoutMaster);

        tableLayout.addView(tr);
        tableLayout.addView(dividierView);
    }

    public void showAddedDialog() {
        new AlertDialog.Builder(getActivity())
                .setTitle("Isegoria")
                .setMessage("Friend Request has Been Sent")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
                                                         int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }
}
