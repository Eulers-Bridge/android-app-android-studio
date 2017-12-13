package com.eulersbridge.isegoria;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SearchView;
import android.text.InputType;
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

import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.eulersbridge.isegoria.models.Contact;
import com.eulersbridge.isegoria.models.FriendRequest;
import com.eulersbridge.isegoria.models.GenericUser;
import com.eulersbridge.isegoria.models.User;
import com.eulersbridge.isegoria.network.SimpleCallback;
import com.eulersbridge.isegoria.profile.ProfileFragment;
import com.eulersbridge.isegoria.utilities.TitledFragment;
import com.eulersbridge.isegoria.utilities.Utils;

import org.parceler.Parcels;

import java.util.List;

import retrofit2.Response;

public class FindAddContactFragment extends Fragment implements TitledFragment {
    private View rootView;

    private TableLayout searchResultsTableLayout;
    private TableLayout friendsAllTableLayout;

    private Isegoria isegoria;

    private MainActivity mainActivity;

    public enum FriendRequestType {
        RECEIVED, SENT, UNKNOWN
    }

    public enum UserType {
        SEARCH, FRIEND, FRIEND_REQUEST
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.find_add_contact_fragment, container, false);

        mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) {
            mainActivity.invalidateOptionsMenu();
            mainActivity.setShowNavigationBackButton(true);
        }

        setHasOptionsMenu(true);

        searchResultsTableLayout = rootView.findViewById(R.id.searchResultsTable);
        friendsAllTableLayout = rootView.findViewById(R.id.friendsAllTableLayout);

        isegoria = (Isegoria) mainActivity.getApplication();

        isegoria.getAPI().getFriends().enqueue(new SimpleCallback<List<Contact>>() {
            @Override
            protected void handleResponse(Response<List<Contact>> response) {
                List<Contact> friends = response.body();
                if (friends != null) setFriends(friends);
            }
        });

        long userId = isegoria.getLoggedInUser().getId();

        isegoria.getAPI().getFriendRequestsSent(userId).enqueue(new SimpleCallback<List<FriendRequest>>() {
            @Override
            protected void handleResponse(Response<List<FriendRequest>> response) {
                List<FriendRequest> friendRequestsSent = response.body();
                if (friendRequestsSent != null) {
                    mainActivity.runOnUiThread(() -> {
                        for (FriendRequest friendRequest : friendRequestsSent) {
                            addFriendRequest(friendRequest, FriendRequestType.SENT);
                        }
                    });
                }
            }
        });

        isegoria.getAPI().getFriendRequestsReceived(userId).enqueue(new SimpleCallback<List<FriendRequest>>() {
            @Override
            protected void handleResponse(Response<List<FriendRequest>> response) {
                List<FriendRequest> friendRequestsReceived = response.body();
                if (friendRequestsReceived != null) {
                    mainActivity.runOnUiThread(() -> {
                        for (FriendRequest friendRequest : friendRequestsReceived) {
                            addFriendRequest(friendRequest, FriendRequestType.RECEIVED);
                        }
                    });
                }
            }
        });

        return rootView;
    }

    public String getTitle(Context context) {
        return context.getString(R.string.section_title_friends);
    }

    @Override
    public void onDetach() {
        super.onDetach();

        mainActivity.setToolbarTitle(getString(R.string.section_title_profile));
        mainActivity.getTabLayout().setVisibility(View.VISIBLE);
        mainActivity.setShowNavigationBackButton(false);
    }

    private final SimpleCallback<List<User>> searchCallback = new SimpleCallback<List<User>>() {
        @Override
        protected void handleResponse(Response<List<User>> response) {
            List<User> users = response.body();
            if (users != null) {
                clearSearchResults();
                addUsers(users);
            }
        }
    };

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
                return query.length() > 2 && search(query);
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.length() > 2) {
                    search(newText);

                } else {
                    clearSearchResults();
                    hideSearchResultsSection();
                }

                return true;
            }
        });
    }

    @SuppressWarnings("SameReturnValue")
    private boolean search(String query) {
        clearSearchResults();
        isegoria.getAPI().searchForUsers(query).enqueue(searchCallback);

        return true;
    }

    public void setTabLayout(TabLayout tabLayout) {
        tabLayout.setVisibility(View.GONE);
    }

    private void clearSearchResults() {
        searchResultsTableLayout.removeAllViews();
    }

    private void hideSearchResultsSection() {
        rootView.findViewById(R.id.searchResultsSection).setVisibility(View.GONE);
    }

    private void addUsers(@NonNull List<User> users) {
        clearSearchResults();

        for (User user : users) {
            addTableRow(searchResultsTableLayout, user, -1, UserType.SEARCH, FriendRequestType.UNKNOWN);

            rootView.findViewById(R.id.searchResultsSection).setVisibility(View.VISIBLE);
        }
    }

    private void setFriends(@NonNull List<Contact> friends) {
        for (Contact friend : friends) {
            addTableRow(friendsAllTableLayout, friend, -1, UserType.FRIEND, FriendRequestType.UNKNOWN);
        }
    }

    private void addFriendRequest(@NonNull FriendRequest friendRequest, FriendRequestType type) {
        View sectionContainer;
        TableLayout tableLayout;

        if (type == FriendRequestType.RECEIVED) {
            sectionContainer = rootView.findViewById(R.id.friendsReceivedSection);
            tableLayout = rootView.findViewById(R.id.friendsReceivedTableLayout);

        } else {
            sectionContainer = rootView.findViewById(R.id.friendsSentSection);
            tableLayout = rootView.findViewById(R.id.friendsSentTableLayout);
        }

        User friendRequestUser = (type == FriendRequestType.SENT)?
                friendRequest.requestReceiver : friendRequest.requester;

        addTableRow(tableLayout, friendRequestUser, friendRequest.id, UserType.FRIEND_REQUEST, type);

        sectionContainer.setVisibility(View.VISIBLE);
    }

    @UiThread
    private void addTableRow(TableLayout tableLayout, final GenericUser user, long contactRequestId, UserType type, FriendRequestType friendRequestType) {
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
        //network.getFirstPhoto(0, userId, candidateProfileView);
        candidateProfileView.setPadding(paddingMargin, 0, paddingMargin, 0);

        GlideApp.with(this)
                .load(user.profilePhotoURL)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(candidateProfileView);

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
            candidateProfileImage.setImageBitmap(Utils.decodeSampledBitmapFromResource(getResources(), R.drawable.addedinactive, imageSize, imageSize));
            candidateProfileImage.setPadding(paddingMargin, 0, paddingMargin, 0);
            candidateProfileImage.setOnClickListener(view -> isegoria.getAPI().addFriend(isegoria.getLoggedInUser().email, user.email)
                    .enqueue(new SimpleCallback<Void>() {
                        @Override
                        protected void handleResponse(Response<Void> response) {
                            showAddedMessage();
                        }
                    }));

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

                ProfileFragment profileFragment = new ProfileFragment();

                Bundle args = new Bundle();
                args.putParcelable(Constant.FRAGMENT_EXTRA_USER, Parcels.wrap(user));
                profileFragment.setArguments(args);

                getChildFragmentManager().beginTransaction()
                        .addToBackStack(null)
                        .replace(R.id.container, profileFragment)
                        .commit();
            });

            linLayout2.addView(viewProfileImage);
        }

        if (friendRequestType == FriendRequestType.RECEIVED) {
            final ImageView acceptImage = new ImageView(getActivity());
            acceptImage.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, Gravity.END));
            acceptImage.setScaleType(ScaleType.CENTER_CROP);
            acceptImage.setImageBitmap(Utils.decodeSampledBitmapFromResource(getResources(), R.drawable.addedinactive, imageSize, imageSize));
            acceptImage.setPadding(paddingMargin, 0, paddingMargin, 0);
            acceptImage.setOnClickListener(view -> {
                tr.setVisibility(ViewGroup.GONE);
                addTableRow(friendsAllTableLayout, user, contactRequestId, UserType.FRIEND, FriendRequestType.UNKNOWN);

                isegoria.getAPI().acceptFriendRequest(contactRequestId).enqueue(new SimpleCallback<Void>() {
                    @Override
                    protected void handleResponse(Response<Void> response) {
                        showAcceptMessage();
                    }
                });
            });

            final ImageView denyImage = new ImageView(getActivity());
            denyImage.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, Gravity.END));
            denyImage.setScaleType(ScaleType.CENTER_CROP);
            denyImage.setImageBitmap(Utils.decodeSampledBitmapFromResource(getResources(), R.drawable.addedinactive, imageSize, imageSize));
            denyImage.setPadding(paddingMargin, 0, paddingMargin, 0);
            denyImage.setOnClickListener(view -> {
                tr.setVisibility(ViewGroup.GONE);

                isegoria.getAPI().rejectFriendRequest(contactRequestId).enqueue(new SimpleCallback<Void>() {
                    @Override
                    protected void handleResponse(Response<Void> response) {
                        showDenyMessage();
                    }
                });
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

    private void showAddedMessage() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Friend request has been accepted", Toast.LENGTH_LONG).show());
        }
    }

    private void showAcceptMessage() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Friend request has been accepted", Toast.LENGTH_LONG).show());
        }
    }

    private void showDenyMessage() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Friend request has been accepted", Toast.LENGTH_LONG).show());
        }
    }
}
