package com.eulersbridge.isegoria;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.SearchView;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.util.Patterns;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TableRow.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

public class FindAddContactFragment extends Fragment {
    private View rootView;

    private TableLayout usersAllTableLayout;
    private TableLayout friendsAllTableLayout;
    private TableLayout pendingTableLayout;

    private float dpWidth;

    private FindAddContactFragment findAddContactFragment;
    private Network network;

    private MainActivity mainActivity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        DisplayMetrics displayMetrics = getActivity().getResources().getDisplayMetrics();
        findAddContactFragment = this;

        rootView = inflater.inflate(R.layout.find_add_contact_fragment, container, false);

        setHasOptionsMenu(true);

        usersAllTableLayout = rootView.findViewById(R.id.usersAllTable);
        friendsAllTableLayout = rootView.findViewById(R.id.friendsAllTableLayout);
        pendingTableLayout = rootView.findViewById(R.id.friendsPendingTableLayout);

        dpWidth = displayMetrics.widthPixels;

        View dividerView = new View(getActivity());
        dividerView.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, 1));
        dividerView.setBackgroundColor(Color.parseColor("#676475"));
        usersAllTableLayout.addView(dividerView);

        mainActivity = (MainActivity) getActivity();
        mainActivity.setToolbarTitle(getString(R.string.section_title_friends));

        network = mainActivity.getIsegoriaApplication().getNetwork();
        network.findFriends(this);
        network.findPendingContacts(this);

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.friends, menu);
        super.onCreateOptionsMenu(menu, inflater);

        MenuItem searchItem = menu.findItem(R.id.search);

        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

        // Set max width, remove left padding to assist in getting search view text aligned
        // with where Toolbar title previously was
        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.setPadding(0,searchView.getPaddingTop(),searchView.getPaddingRight(),searchView.getPaddingBottom());

        // Get the search view's inner LinearLayout and remove the left margin
        LinearLayout searchEditFrame = searchView.findViewById(R.id.search_edit_frame);
        ((LinearLayout.LayoutParams) searchEditFrame.getLayoutParams()).leftMargin = 0;

        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                mainActivity.setToolbarShowsTitle(!hasFocus);
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query.length() != 0 && Patterns.EMAIL_ADDRESS.matcher(query).matches()) {
                    network.findContacts(query, findAddContactFragment);
                    return true;
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.length() != 0 && Patterns.EMAIL_ADDRESS.matcher(newText).matches()) {
                    network.findContacts(newText, findAddContactFragment);
                    return true;
                }

                return false;
            }
        });
    }

    public void setTabLayout(TabLayout tabLayout) {
        tabLayout.setVisibility(View.GONE);
    }

    public void clearSearchResults() {
        usersAllTableLayout.removeAllViews();
    }

    public void addUser(User user) {
        addTableRow(-1, usersAllTableLayout, user.getFullName(), user.getEmail(), "The University of Melbourne", user.getProfilePhotoURL(), 1);
        LinearLayout searchResultsLinearLayout = rootView.findViewById(R.id.searchResultsLinearLayout);
        searchResultsLinearLayout.setVisibility(ViewGroup.VISIBLE);
    }

    public void addFriend(int userId, User user) {
        addTableRow(userId, friendsAllTableLayout, user.getFullName(), user.getEmail(), "The University of Melbourne", user.getProfilePhotoURL(), 2);
    }

    public void addPendingFriend(int userId, User user) {
        LinearLayout pendingRequestsLinearLayout = rootView.findViewById(R.id.pendingRequestsLinearLayout);
        pendingRequestsLinearLayout.setVisibility(ViewGroup.VISIBLE);
        addTableRow(userId, pendingTableLayout, user.getFullName(), user.getEmail(), "The University of Melbourne", user.getProfilePhotoURL(), 3);
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
        linLayout.addView(textViewCandidate);
        linLayout.addView(textViewPosition);

        LinearLayout linLayout2 = new LinearLayout(getActivity());
        //linLayout2.setBackgroundColor((Color.parseColor("#000000")));
        linLayout2.setOrientation(LinearLayout.HORIZONTAL);
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

    public void showAddedMessage() {
        Toast.makeText(getContext(), "Friend request has been accepted", Toast.LENGTH_LONG).show();
    }

    public void showAcceptMessage() {
        Toast.makeText(getContext(), "Friend request has been accepted", Toast.LENGTH_LONG).show();
    }

    public void showDenyMessage() {
        Toast.makeText(getContext(), "Friend request has been accepted", Toast.LENGTH_LONG).show();
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
