package com.eulersbridge.isegoria;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
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

public class FindAddContactFragment extends Fragment {
    private View rootView;
    private TableLayout usersAllTableLayout;
    private TableLayout friendsAllTableLayout;
    private TableLayout pendingTableLayout;

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

        //TODO: No tabs

        usersAllTableLayout = rootView.findViewById(R.id.usersAllTable);
        friendsAllTableLayout = rootView.findViewById(R.id.friendsAllTableLayout);
        pendingTableLayout = rootView.findViewById(R.id.friendsPendingTableLayout);

        dpWidth = displayMetrics.widthPixels;
        dpHeight = displayMetrics.heightPixels / displayMetrics.density;

        View dividierView = new View(getActivity());
        dividierView.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, 1));
        dividierView.setBackgroundColor(Color.parseColor("#676475"));
        usersAllTableLayout.addView(dividierView);

        MainActivity mainActivity = (MainActivity) getActivity();
        network = mainActivity.getIsegoriaApplication().getNetwork();
        network.findFriends(this);
        network.findPendingContacts(this);

        searchFriendsView = rootView.findViewById(R.id.searchFriendsView);
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
        addTableRow(-1, usersAllTableLayout, firstName + " " + lastName, email, institution, url, 1);
        LinearLayout searchResultsLinearLayout = rootView.findViewById(R.id.searchResultsLinearLayout);
        searchResultsLinearLayout.setVisibility(ViewGroup.VISIBLE);
    }

    public void addFriend(int userId, String firstName, String lastName, final String email, String institution, String url) {
        addTableRow(userId, friendsAllTableLayout, firstName + " " + lastName, email, institution, url, 2);
    }

    public void addPendingFriend(int userId, String firstName, String lastName, final String email, String institution, String url) {
        LinearLayout pendingRequestsLinearLayout = rootView.findViewById(R.id.pendingRequestsLinearLayout);
        pendingRequestsLinearLayout.setVisibility(ViewGroup.VISIBLE);
        addTableRow(userId, pendingTableLayout, firstName + " " + lastName, email, institution, url, 3);
    }

    // 1 = Search
    // 2 = Current Contact
    // 3 = Pending Contact
    private void addTableRow(final int userId, TableLayout tableLayout, final String name, final String email, final String institution, final String url, int type) {
        final TableRow tr;

        final int paddingMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                (float) 6.666666667, getResources().getDisplayMetrics());
        final int imageSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                (float) 53.333333333, getResources().getDisplayMetrics());

        LinearLayout layout = new LinearLayout(getActivity());
        layout.setOrientation(LinearLayout.HORIZONTAL);

        tr = new TableRow(getActivity());
        TableRow.LayoutParams rowParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
        tr.setLayoutParams(rowParams);
        tr.setPadding(0, paddingMargin, 0, paddingMargin);

        ImageView candidateProfileView = new ImageView(getActivity());
        candidateProfileView.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(imageSize, imageSize);
        candidateProfileView.setLayoutParams(layoutParams);
        candidateProfileView.setScaleType(ScaleType.CENTER_CROP);
        //network.getFirstPhoto(0, userId, candidateProfileView);
        candidateProfileView.setPadding(paddingMargin, 0, paddingMargin, 0);
        network.getPictureVolley(url, candidateProfileView);

        final ImageView viewProfileImage = new ImageView(getActivity());
        viewProfileImage.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, Gravity.END));
        viewProfileImage.setScaleType(ScaleType.CENTER_CROP);
        viewProfileImage.setImageBitmap(decodeSampledBitmapFromResource(getResources(), R.drawable.profileactive, imageSize, imageSize));
        viewProfileImage.setPadding(paddingMargin, 0, paddingMargin, 0);
        viewProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewProfileImage.setImageBitmap(decodeSampledBitmapFromResource(getResources(), R.drawable.profiledark, imageSize, imageSize));
                FragmentManager fragmentManager2 = getFragmentManager();
                FragmentTransaction fragmentTransaction2 = fragmentManager2.beginTransaction();
                ContactProfileFragment fragment2 = new ContactProfileFragment();
                Bundle args = new Bundle();
                args.putInt("ProfileId", userId);
                fragment2.setArguments(args);
                fragmentTransaction2.addToBackStack(null);
                fragmentTransaction2.replace(android.R.id.content, fragment2);
                fragmentTransaction2.commit();
            }
        });

        final ImageView candidateProfileImage = new ImageView(getActivity());
        candidateProfileImage.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, Gravity.END));
        candidateProfileImage.setScaleType(ScaleType.CENTER_CROP);
        candidateProfileImage.setImageBitmap(decodeSampledBitmapFromResource(getResources(), R.drawable.addedinactive, imageSize, imageSize));
        candidateProfileImage.setPadding(paddingMargin, 0, paddingMargin, 0);
        candidateProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               // candidateProfileImage.setImageBitmap(decodeSampledBitmapFromResource(getResources(), R.drawable.addedactive, imageSize, imageSize));
                /*FragmentManager fragmentManager2 = getFragmentManager();
                FragmentTransaction fragmentTransaction2 = fragmentManager2.beginTransaction();
                ContactProfileFragment fragment2 = new ContactProfileFragment();
                Bundle args = new Bundle();
                //args.putInt("ProfileId", userId);
                fragment2.setArguments(args);
                fragmentTransaction2.addToBackStack(null);
                fragmentTransaction2.replace(android.R.id.content, fragment2);
                fragmentTransaction2.commit();*/
                network.addFriend(String.valueOf(userId), findAddContactFragment);
            }
        });

        final ImageView acceptImage = new ImageView(getActivity());
        acceptImage.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, Gravity.END));
        acceptImage.setScaleType(ScaleType.CENTER_CROP);
        acceptImage.setImageBitmap(decodeSampledBitmapFromResource(getResources(), R.drawable.addedinactive, imageSize, imageSize));
        acceptImage.setPadding(paddingMargin, 0, paddingMargin, 0);
        acceptImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //candidateProfileImage.setImageBitmap(decodeSampledBitmapFromResource(getResources(), R.drawable.addedactive, imageSize, imageSize));
                /*FragmentManager fragmentManager2 = getFragmentManager();
                FragmentTransaction fragmentTransaction2 = fragmentManager2.beginTransaction();
                ContactProfileFragment fragment2 = new ContactProfileFragment();
                Bundle args = new Bundle();
                //args.putInt("ProfileId", userId);
                fragment2.setArguments(args);
                fragmentTransaction2.addToBackStack(null);
                fragmentTransaction2.replace(android.R.id.content, fragment2);
                fragmentTransaction2.commit();*/
                tr.setVisibility(ViewGroup.GONE);
                findAddContactFragment.addTableRow(userId, friendsAllTableLayout, name, email, institution, url, 2);
                network.acceptContact(String.valueOf(userId), findAddContactFragment);
            }
        });

        final ImageView denyImage = new ImageView(getActivity());
        denyImage.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, Gravity.END));
        denyImage.setScaleType(ScaleType.CENTER_CROP);
        denyImage.setImageBitmap(decodeSampledBitmapFromResource(getResources(), R.drawable.addedinactive, imageSize, imageSize));
        denyImage.setPadding(paddingMargin, 0, paddingMargin, 0);
        denyImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //candidateProfileImage.setImageBitmap(decodeSampledBitmapFromResource(getResources(), R.drawable.addedactive, imageSize, imageSize));
                /*FragmentManager fragmentManager2 = getFragmentManager();
                FragmentTransaction fragmentTransaction2 = fragmentManager2.beginTransaction();
                ContactProfileFragment fragment2 = new ContactProfileFragment();
                Bundle args = new Bundle();
                //args.putInt("ProfileId", userId);
                fragment2.setArguments(args);
                fragmentTransaction2.addToBackStack(null);
                fragmentTransaction2.replace(android.R.id.content, fragment2);
                fragmentTransaction2.commit();*/
                tr.setVisibility(ViewGroup.GONE);
                network.denyContact(String.valueOf(userId), findAddContactFragment);
            }
        });

        TextView textViewCandidate = new TextView(getActivity());
        textViewCandidate.setTextColor(Color.parseColor("#3A3F43"));
        textViewCandidate.setTextSize(16.0f);
        textViewCandidate.setText(name);
        textViewCandidate.setPadding(paddingMargin, 0, paddingMargin, 0);
        textViewCandidate.setGravity(Gravity.START);

        TextView textViewPosition = new TextView(getActivity());
        textViewPosition.setTextColor(Color.parseColor("#3A3F43"));
        textViewPosition.setTextSize(12.0f);
        textViewPosition.setText(institution);
        textViewPosition.setPadding(paddingMargin, 0, paddingMargin, 0);
        textViewPosition.setGravity(Gravity.START);

        //network.getPositionText(textViewPosition, positionId);

        View dividierView = new View(getActivity());
        dividierView.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, 1));
        dividierView.setBackgroundColor(Color.parseColor("#676475"));

        RelativeLayout relLayoutMaster = new RelativeLayout(getActivity());
        TableRow.LayoutParams relLayoutMasterParam = new TableRow.LayoutParams((int)dpWidth, TableRow.LayoutParams.WRAP_CONTENT);
        relLayoutMaster.setLayoutParams(relLayoutMasterParam);

        RelativeLayout.LayoutParams relativeParamsLeft = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        relativeParamsLeft.addRule(RelativeLayout.ALIGN_PARENT_LEFT);

        RelativeLayout.LayoutParams relativeParamsRight = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        relativeParamsRight.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

        LinearLayout linLayout = new LinearLayout(getActivity());
        linLayout.setOrientation(LinearLayout.VERTICAL);
        LayoutParams linLayoutParam = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        linLayout.addView(textViewCandidate);
        linLayout.addView(textViewPosition);

        LinearLayout linLayout2 = new LinearLayout(getActivity());
        //linLayout2.setBackgroundColor((Color.parseColor("#000000")));
        linLayout2.setOrientation(LinearLayout.HORIZONTAL);
        LayoutParams linLayoutParam2 = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        if(type == 1) {
            linLayout2.addView(candidateProfileImage);
        }
        if(type == 2) {
            linLayout2.addView(viewProfileImage);
        }
        if(type == 3) {
            linLayout2.addView(denyImage);
            linLayout2.addView(acceptImage);
        }
        linLayout2.setGravity(Gravity.END);
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

    public void showAcceptDialog() {
        new AlertDialog.Builder(getActivity())
                .setTitle("Isegoria")
                .setMessage("Friend Request has Been Accepted")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    public void showDenyDialog() {
        new AlertDialog.Builder(getActivity())
                .setTitle("Isegoria")
                .setMessage("Friend Request has Been Denied")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private static int calculateInSampleSize(
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

    private static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
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
