package com.eulersbridge.isegoria;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SearchView;
import android.text.InputType;
import android.util.DisplayMetrics;
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

import com.eulersbridge.isegoria.models.FriendRequest;
import com.eulersbridge.isegoria.models.User;
import com.eulersbridge.isegoria.utilities.Utils;

import java.util.ArrayList;

public class FindAddContactFragment extends Fragment {
    private View rootView;

    private TableLayout searchResultsTableLayout;
    private TableLayout friendsAllTableLayout;

    private Network network;

    private MainActivity mainActivity;

    public enum UserType {
        SEARCH, FRIEND, FRIEND_REQUEST
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.find_add_contact_fragment, container, false);

        setHasOptionsMenu(true);

        searchResultsTableLayout = rootView.findViewById(R.id.searchResultsTable);
        friendsAllTableLayout = rootView.findViewById(R.id.friendsAllTableLayout);

        mainActivity = (MainActivity) getActivity();
        mainActivity.setToolbarTitle(getString(R.string.section_title_friends));

        network = mainActivity.getIsegoriaApplication().getNetwork();
        network.getFriends(this);
        network.getFriendRequestsSent(this);
        network.getFriendRequestsReceived(this);

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.friends, menu);
        super.onCreateOptionsMenu(menu, inflater);

        MenuItem searchItem = menu.findItem(R.id.search);

        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PERSON_NAME);

        // Set max width, remove left padding to assist in getting search view text aligned
        // with where Toolbar title previously was
        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.setPadding(0,searchView.getPaddingTop(),searchView.getPaddingRight(),searchView.getPaddingBottom());

        // Get the search view's inner LinearLayout and remove the left margin
        LinearLayout searchEditFrame = searchView.findViewById(R.id.search_edit_frame);
        ((LinearLayout.LayoutParams) searchEditFrame.getLayoutParams()).leftMargin = 0;

        searchView.setOnQueryTextFocusChangeListener((view, hasFocus) -> {
            mainActivity.setToolbarShowsTitle(!hasFocus);

            if (!hasFocus) {
                clearSearchResults();
                hideSearchResultsSection();
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query.length() > 2) {
                    network.findContacts(query, FindAddContactFragment.this);
                    return true;
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.length() > 2) {
                    network.findContacts(newText, FindAddContactFragment.this);

                } else {
                    clearSearchResults();
                    hideSearchResultsSection();
                }

                return true;
            }
        });
    }

    public void setTabLayout(TabLayout tabLayout) {
        tabLayout.setVisibility(View.GONE);
    }

    public void clearSearchResults() {
        searchResultsTableLayout.removeAllViews();
    }

    private void hideSearchResultsSection() {
        rootView.findViewById(R.id.searchResultsSection).setVisibility(View.GONE);
    }

    public void addUsers(ArrayList<User> users) {
        clearSearchResults();

        for (User user : users) {
            addTableRow(searchResultsTableLayout, user, null, UserType.SEARCH, FriendRequest.Type.UNKNOWN);

            rootView.findViewById(R.id.searchResultsSection).setVisibility(View.VISIBLE);
        }
    }

    public void setFriends(ArrayList<User> friends) {
        for (User friend : friends) {
            addTableRow(friendsAllTableLayout, friend, null, UserType.FRIEND, FriendRequest.Type.UNKNOWN);
        }
    }

    public void addFriendRequest(FriendRequest friendRequest) {
        View sectionContainer;
        TableLayout tableLayout;

        if (friendRequest.getType() == FriendRequest.Type.RECEIVED) {
            sectionContainer = rootView.findViewById(R.id.friendsReceivedSection);
            tableLayout = rootView.findViewById(R.id.friendsReceivedTableLayout);

        } else {
            sectionContainer = rootView.findViewById(R.id.friendsSentSection);
            tableLayout = rootView.findViewById(R.id.friendsSentTableLayout);
        }

        addTableRow(tableLayout, friendRequest.getUser(), String.valueOf(friendRequest.getId()), UserType.FRIEND_REQUEST, friendRequest.getType());

        sectionContainer.setVisibility(View.VISIBLE);
    }

    private void addTableRow(TableLayout tableLayout, final User user, final String contactRequestId, UserType type, FriendRequest.Type friendRequestType) {
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
        network.getPictureVolley(user.getProfilePhotoURL(), candidateProfileView);

        TextView textViewCandidate = new TextView(getActivity());
        textViewCandidate.setTextColor(Color.parseColor("#3A3F43"));
        textViewCandidate.setTextSize(16.0f);
        textViewCandidate.setText(user.getFullName());
        textViewCandidate.setPadding(paddingMargin, 0, paddingMargin, 0);
        textViewCandidate.setGravity(Gravity.START);

        TextView textViewPosition = new TextView(getActivity());
        textViewPosition.setTextColor(Color.parseColor("#3A3F43"));
        textViewPosition.setTextSize(12.0f);
        textViewPosition.setText("The University of Melbourne");
        textViewPosition.setPadding(paddingMargin, 0, paddingMargin, 0);
        textViewPosition.setGravity(Gravity.START);

        //network.getPositionText(textViewPosition, positionId);

        LinearLayout linLayout2 = new LinearLayout(getActivity());
        //linLayout2.setBackgroundColor((Color.parseColor("#000000")));
        linLayout2.setOrientation(LinearLayout.HORIZONTAL);
        if (type == UserType.SEARCH) {
            final ImageView candidateProfileImage = new ImageView(getActivity());
            candidateProfileImage.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, Gravity.END));
            candidateProfileImage.setScaleType(ScaleType.CENTER_CROP);
            candidateProfileImage.setImageBitmap(Utils.decodeSampledBitmapFromResource(getResources(), R.drawable.addedinactive, imageSize, imageSize));
            candidateProfileImage.setPadding(paddingMargin, 0, paddingMargin, 0);
            candidateProfileImage.setOnClickListener(view -> {
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
                network.addFriend(user.getEmail(), FindAddContactFragment.this);
            });

            linLayout2.addView(candidateProfileImage);
        }

        if (type == UserType.FRIEND) {
            final ImageView viewProfileImage = new ImageView(getActivity());
            viewProfileImage.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, Gravity.END));
            viewProfileImage.setScaleType(ScaleType.CENTER_CROP);
            viewProfileImage.setImageBitmap(Utils.decodeSampledBitmapFromResource(getResources(), R.drawable.profileactive, imageSize, imageSize));
            viewProfileImage.setPadding(paddingMargin, 0, paddingMargin, 0);
            viewProfileImage.setOnClickListener(view -> {
                viewProfileImage.setImageBitmap(Utils.decodeSampledBitmapFromResource(getResources(), R.drawable.profiledark, imageSize, imageSize));

                ContactProfileFragment profileFragment = new ContactProfileFragment();

                Bundle args = new Bundle();
                args.putParcelable("profile", user);
                profileFragment.setArguments(args);

                getFragmentManager().beginTransaction()
                        .addToBackStack(null)
                        .replace(R.id.container, profileFragment)
                        .commit();
            });

            linLayout2.addView(viewProfileImage);
        }

        if (friendRequestType == FriendRequest.Type.RECEIVED) {
            final ImageView acceptImage = new ImageView(getActivity());
            acceptImage.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, Gravity.END));
            acceptImage.setScaleType(ScaleType.CENTER_CROP);
            acceptImage.setImageBitmap(Utils.decodeSampledBitmapFromResource(getResources(), R.drawable.addedinactive, imageSize, imageSize));
            acceptImage.setPadding(paddingMargin, 0, paddingMargin, 0);
            acceptImage.setOnClickListener(view -> {
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
                FindAddContactFragment.this.addTableRow(friendsAllTableLayout, user, contactRequestId, UserType.FRIEND, FriendRequest.Type.UNKNOWN);
                network.acceptContact(String.valueOf(contactRequestId), FindAddContactFragment.this);
            });

            final ImageView denyImage = new ImageView(getActivity());
            denyImage.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, Gravity.END));
            denyImage.setScaleType(ScaleType.CENTER_CROP);
            denyImage.setImageBitmap(Utils.decodeSampledBitmapFromResource(getResources(), R.drawable.addedinactive, imageSize, imageSize));
            denyImage.setPadding(paddingMargin, 0, paddingMargin, 0);
            denyImage.setOnClickListener(view -> {
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
                network.denyContact(String.valueOf(contactRequestId), FindAddContactFragment.this);
            });

            linLayout2.addView(denyImage);
            linLayout2.addView(acceptImage);
        }

        RelativeLayout.LayoutParams relativeParamsLeft = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        relativeParamsLeft.addRule(RelativeLayout.ALIGN_PARENT_LEFT);

        RelativeLayout.LayoutParams relativeParamsRight = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        relativeParamsRight.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

        linLayout2.setGravity(Gravity.END);
        linLayout2.setLayoutParams(relativeParamsRight);

        layout.addView(candidateProfileView);

        LinearLayout linLayout = new LinearLayout(getActivity());
        linLayout.setOrientation(LinearLayout.VERTICAL);
        linLayout.addView(textViewCandidate);
        linLayout.addView(textViewPosition);

        layout.addView(linLayout);

        layout.setLayoutParams(relativeParamsLeft);

        RelativeLayout relLayoutMaster = new RelativeLayout(getActivity());
        TableRow.LayoutParams relLayoutMasterParam = new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
        relLayoutMaster.setLayoutParams(relLayoutMasterParam);

        relLayoutMaster.addView(layout);
        relLayoutMaster.addView(linLayout2);

        tr.addView(relLayoutMaster);

        tableLayout.addView(tr);
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
}
