package com.eulersbridge.isegoria.friends;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.Group;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.eulersbridge.isegoria.MainActivity;
import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.network.api.models.Contact;
import com.eulersbridge.isegoria.network.api.models.FriendRequest;
import com.eulersbridge.isegoria.network.api.models.User;
import com.eulersbridge.isegoria.profile.ProfileOverviewFragment;
import com.eulersbridge.isegoria.util.Constants;
import com.eulersbridge.isegoria.util.ui.TitledFragment;

import org.parceler.Parcels;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;

public class FriendsFragment extends Fragment implements TitledFragment, MainActivity.TabbedFragment,
        FriendAdapter.Delegate, SearchAdapter.UserDelegate, FriendRequestAdapter.Delegate {

    private final SearchAdapter searchAdapter = new SearchAdapter(this);
    private Group searchContainer;

    private final FriendRequestAdapter receivedAdapter = new FriendRequestAdapter(RECEIVED, this);
    private Group receivedRequestsContainer;

    private final FriendRequestAdapter sentAdapter = new FriendRequestAdapter(SENT, this);
    private Group sentRequestsContainer;

    private FriendAdapter friendsAdapter;
    private Group friendsContainer;

    private FriendsViewModel viewModel;

    private MainActivity mainActivity;

    // FriendRequestType IntDef
    public static final int RECEIVED = 0;
    public static final int SENT = 1;

    @IntDef({RECEIVED,SENT})
    @Retention(RetentionPolicy.SOURCE)
    @interface FriendRequestType {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.friends_fragment, container, false);

        mainActivity = (MainActivity) getActivity();
        if (mainActivity != null)
            mainActivity.invalidateOptionsMenu();

        setHasOptionsMenu(true);

        viewModel = ViewModelProviders.of(this).get(FriendsViewModel.class);

        RecyclerView searchResultsList = rootView.findViewById(R.id.friends_search_list);
        searchResultsList.setAdapter(searchAdapter);
        searchContainer = rootView.findViewById(R.id.friends_search);

        RecyclerView requestsReceivedList = rootView.findViewById(R.id.friends_received_list);
        requestsReceivedList.setAdapter(receivedAdapter);
        receivedRequestsContainer = rootView.findViewById(R.id.friends_received);

        RecyclerView requestsSentList = rootView.findViewById(R.id.friends_sent_list);
        requestsSentList.setAdapter(sentAdapter);
        sentRequestsContainer = rootView.findViewById(R.id.friends_sent);

        friendsAdapter = new FriendAdapter(this);

        RecyclerView friendsList = rootView.findViewById(R.id.friends_list);
        friendsList.setAdapter(friendsAdapter);
        friendsContainer = rootView.findViewById(R.id.friends);



        viewModel.searchSectionVisible.observe(this, visibleValue -> {
            boolean visible = visibleValue != null && visibleValue;
            searchContainer.setVisibility(visible? View.VISIBLE : View.GONE);
        });

        viewModel.friendsVisible.observe(this, visibleValue -> {
            boolean visible = visibleValue != null && visibleValue;
            friendsContainer.setVisibility(visible? View.VISIBLE : View.GONE);
        });

        viewModel.sentRequestsVisible.observe(this, visibleValue -> {
            boolean visible = visibleValue != null && visibleValue;
            sentRequestsContainer.setVisibility(visible? View.VISIBLE : View.GONE);
        });

        viewModel.receivedRequestsVisible.observe(this, visibleValue -> {
            boolean visible = visibleValue != null && visibleValue;
            receivedRequestsContainer.setVisibility(visible? View.VISIBLE : View.GONE);
        });

        viewModel.getFriends().observe(this, friends -> {
            if (friends != null)
                friendsAdapter.setItems(friends);
        });

        viewModel.getSentFriendRequests().observe(this, requests -> {
            if (requests != null)
                sentAdapter.setItems(requests);
        });

        getReceivedFriendRequests();

        return rootView;
    }

    private void getReceivedFriendRequests() {
        viewModel.getReceivedFriendRequests().observe(this, requests -> {
            if (requests != null)
                receivedAdapter.setItems(requests);
        });
    }

    @Override
    public void getContactInstitution(long institutionId, WeakReference<UserViewHolder> weakViewHolder) {
        viewModel.getInstitution(institutionId).observe(this, institution ->
            friendsAdapter.setInstitution(institution, weakViewHolder)
        );
    }

    @Override
    public void getSearchedUserInstitution(long institutionId, WeakReference<UserViewHolder> weakViewHolder) {
        viewModel.getInstitution(institutionId).observe(this, institution ->
                searchAdapter.setInstitution(institution, weakViewHolder)
        );
    }

    @Override
    public void getFriendRequestInstitution(long institutionId, @FriendRequestType int type, WeakReference<RecyclerView.ViewHolder> weakViewHolder) {
        viewModel.getInstitution(institutionId).observe(this, institution -> {
            if (type == RECEIVED)
                receivedAdapter.setInstitution(institution, weakViewHolder);
            else
                sentAdapter.setInstitution(institution, weakViewHolder);
        });
    }

    @Override
    public void performFriendRequestAction(int type, FriendRequest friendRequest) {
        if (type == RECEIVED) {

            // Show accept or reject dialog
            if (getContext() != null)
                new AlertDialog.Builder(getContext())
                        .setTitle(getString(R.string.friend_request_action_dialog_title)+friendRequest.requester.getFullName())
                        .setPositiveButton(R.string.friend_request_action_dialog_positive, (dialogInterface, choice) ->
                            acceptFriendRequest(friendRequest)
                        )
                        .setNegativeButton(R.string.friend_request_action_dialog_negative, (dialogInterface, choice) ->
                            rejectFriendRequest(friendRequest)
                        )
                        .setNeutralButton(R.string.friend_request_action_dialog_neutral, null)
                        .show();

        } else if (type == SENT && mainActivity != null) {
            User user = friendRequest.requestReceiver;

            Bundle args = new Bundle();
            args.putParcelable(Constants.FRAGMENT_EXTRA_USER, Parcels.wrap(user));

            ProfileOverviewFragment profileFragment = new ProfileOverviewFragment();
            profileFragment.setArguments(args);

            mainActivity.presentContent(profileFragment);
        }
    }

    private void acceptFriendRequest(@NonNull FriendRequest friendRequest) {
        viewModel.acceptFriendRequest(friendRequest.id).observe(this, success -> {
            if (success != null && success)
                showAcceptedMessage(friendRequest.requester.getFullName());
        });
    }

    private void rejectFriendRequest(@NonNull FriendRequest friendRequest) {
        viewModel.rejectFriendRequest(friendRequest.id).observe(this, success -> {
            if (success != null && success)
                showRejectedMessage();
        });
    }

    @Override
    public void onSearchedUserClick(@Nullable User user) {
        ProfileOverviewFragment profileOverviewFragment = new ProfileOverviewFragment();

        Bundle args = new Bundle();
        args.putParcelable(Constants.FRAGMENT_EXTRA_USER, Parcels.wrap(user));
        profileOverviewFragment.setArguments(args);

        if (mainActivity != null)
            mainActivity.presentContent(profileOverviewFragment);
    }

    @Override
    public void onSearchedUserActionClick(@Nullable User user) {
        if (user != null)
            viewModel.addFriend(user.email).observe(this, __ -> showAddedMessage());
    }

    @Override
    public void onContactClick(@NonNull Contact contact) {
        ProfileOverviewFragment profileOverviewFragment = new ProfileOverviewFragment();

        Bundle args = new Bundle();
        args.putParcelable(Constants.FRAGMENT_EXTRA_CONTACT, Parcels.wrap(contact));
        profileOverviewFragment.setArguments(args);

        if (mainActivity != null)
            mainActivity.presentContent(profileOverviewFragment);
    }

    public String getTitle(Context context) {
        return context.getString(R.string.section_title_friends);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.friends, menu);
        super.onCreateOptionsMenu(menu, inflater);

        MenuItem searchItem = menu.findItem(R.id.friends_search_view);

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

            if (!hasFocus)
                viewModel.hideSearch();
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchQueryChanged(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                searchQueryChanged(query);
                return true;
            }
        });
    }

    private void searchQueryChanged(String query) {
        viewModel.onSearchQueryChanged(query).observe(this, searchResults -> {
            searchAdapter.clearItems();

            if (searchResults != null)
                searchAdapter.setItems(searchResults);
        });
    }

    @Override
    public void setupTabLayout(TabLayout tabLayout) {
        tabLayout.setVisibility(View.GONE);
    }

    private void showMessage(@NonNull String message) {
        if (getActivity() != null)
            getActivity().runOnUiThread(() -> Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show());
    }

    private void showAddedMessage() {
        showMessage(getString(R.string.friend_request_sent_message));
    }

    private void showAcceptedMessage(String friendName) {
        showMessage(getString(R.string.friend_request_accepted_message, friendName));
    }

    private void showRejectedMessage() {
        showMessage(getString(R.string.friend_request_rejected_message));
    }
}
