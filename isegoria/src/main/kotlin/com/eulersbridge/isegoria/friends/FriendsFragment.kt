package com.eulersbridge.isegoria.friends

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.support.annotation.IntDef
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.text.InputType
import android.view.*
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.widget.toast
import com.eulersbridge.isegoria.FRAGMENT_EXTRA_CONTACT
import com.eulersbridge.isegoria.FRAGMENT_EXTRA_USER
import com.eulersbridge.isegoria.MainActivity
import com.eulersbridge.isegoria.R
import com.eulersbridge.isegoria.data.Repository
import com.eulersbridge.isegoria.network.api.model.Contact
import com.eulersbridge.isegoria.network.api.model.FriendRequest
import com.eulersbridge.isegoria.network.api.model.User
import com.eulersbridge.isegoria.profile.ProfileOverviewFragment
import com.eulersbridge.isegoria.util.extension.observe
import com.eulersbridge.isegoria.util.extension.observeBoolean
import com.eulersbridge.isegoria.util.ui.TitledFragment
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.friends_fragment.*
import java.lang.ref.WeakReference
import javax.inject.Inject

class FriendsFragment : Fragment(), TitledFragment, MainActivity.TabbedFragment,
    FriendAdapter.Delegate, SearchAdapter.UserDelegate, FriendRequestAdapter.Delegate {

    private val searchAdapter = SearchAdapter(this)
    private val receivedAdapter = FriendRequestAdapter(RECEIVED, this)
    private val sentAdapter = FriendRequestAdapter(SENT, this)
    private var friendsAdapter: FriendAdapter = FriendAdapter(this)

    @Inject
    lateinit var modelFactory: ViewModelProvider.Factory
    private lateinit var viewModel: FriendsViewModel

    @Inject
    lateinit var repository: Repository

    private var mainActivity: MainActivity? = null

    @IntDef(RECEIVED, SENT)
    @Retention(AnnotationRetention.SOURCE)
    internal annotation class FriendRequestType

    override fun onAttach(context: Context?) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
        viewModel = ViewModelProviders.of(this, modelFactory)[FriendsViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.friends_fragment, container, false)

        activity?.invalidateOptionsMenu()

        setHasOptionsMenu(true)

        observeBoolean(viewModel.searchSectionVisible) { searchContainer.isVisible = it }
        observeBoolean(viewModel.friendsVisible) { friendsContainer.isVisible = it }
        observeBoolean(viewModel.receivedRequestsVisible) { receivedRequestsContainer.isVisible = it }
        observeBoolean(viewModel.sentRequestsVisible) { sentRequestsContainer.isVisible = it }
        observe(viewModel.friends) { friendsAdapter.setItems(it!!) }
        observe(viewModel.receivedFriendRequests) { receivedAdapter.setItems(it!!) }
        observe(viewModel.sentFriendRequests) { sentAdapter.setItems(it!!) }
        observe(viewModel.searchResults) { searchAdapter.setItems(it!!) }

        mainActivity = activity as? MainActivity

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        searchResultsList.adapter = searchAdapter
        receivedRequestsList.adapter = receivedAdapter
        sentRequestsList.adapter = sentAdapter
        friendsList.adapter = friendsAdapter
    }

    override fun onDestroy() {
        viewModel.onDestroy()
        super.onDestroy()
    }

    private fun getReceivedFriendRequests() {
        viewModel.refreshReceivedFriendRequests()
    }

    override fun getContactInstitution(
        institutionId: Long,
        weakViewHolder: WeakReference<UserViewHolder>
    ) {
        observe(viewModel.getInstitution(institutionId)) {
            friendsAdapter.setInstitution(it, weakViewHolder)
        }
    }

    override fun getSearchedUserInstitution(
        institutionId: Long,
        weakViewHolder: WeakReference<UserViewHolder>
    ) {
        observe(viewModel.getInstitution(institutionId)) {
            searchAdapter.setInstitution(it, weakViewHolder)
        }
    }

    override fun getFriendRequestInstitution(
        institutionId: Long, @FriendRequestType type: Int,
        weakViewHolder: WeakReference<RecyclerView.ViewHolder>
    ) {
        observe(viewModel.getInstitution(institutionId)) {
            if (type == RECEIVED)
                receivedAdapter.setInstitution(it, weakViewHolder)
            else
                sentAdapter.setInstitution(it, weakViewHolder)
        }
    }

    override fun performFriendRequestAction(type: Int, request: FriendRequest) {
        if (type == RECEIVED) {

            // Show accept/reject dialog
            context?.let {
                AlertDialog.Builder(it)
                    .setTitle(getString(R.string.friend_request_action_dialog_title, request.requester!!.fullName))
                    .setPositiveButton(
                        R.string.friend_request_action_dialog_positive
                    ) { _, _ -> acceptFriendRequest(request) }
                    .setNegativeButton(
                        R.string.friend_request_action_dialog_negative
                    ) { _, _ -> rejectFriendRequest(request) }
                    .setNeutralButton(R.string.friend_request_action_dialog_neutral, null)
                    .show()
            }

        } else if (type == SENT && mainActivity != null) {
            val user = request.requestReceiver

            val profileFragment = ProfileOverviewFragment.create(repository, null)
            profileFragment.arguments = bundleOf(FRAGMENT_EXTRA_USER to user)

            mainActivity?.presentContent(profileFragment)
        }
    }

    private fun acceptFriendRequest(friendRequest: FriendRequest) {
        observe(viewModel.acceptFriendRequest(friendRequest.id)) { success ->
            if (success == true)
                showAcceptedMessage(friendRequest.requester!!.fullName)
        }
    }

    private fun rejectFriendRequest(friendRequest: FriendRequest) {
        observe(viewModel.rejectFriendRequest(friendRequest.id)) { success ->
            if (success == true)
                showRejectedMessage()
        }
    }

    override fun onSearchedUserClick(user: User?) {
        val profileFragment = ProfileOverviewFragment.create(repository, null)
        profileFragment.arguments = bundleOf(FRAGMENT_EXTRA_USER to user)

        mainActivity?.presentContent(profileFragment)
    }

    override fun onSearchedUserActionClick(user: User?) {
        if (user != null)
            observe(viewModel.addFriend(user.email)) { showAddedMessage() }
    }

    override fun onContactClick(contact: Contact) {
        val profileFragment = ProfileOverviewFragment.create(repository, null)
        profileFragment.arguments = bundleOf(FRAGMENT_EXTRA_CONTACT to contact)

        mainActivity!!.presentContent(profileFragment)
    }

    override fun getTitle(context: Context?) = context?.getString(R.string.section_title_friends)

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.friends, menu)
        super.onCreateOptionsMenu(menu, inflater)

        if (menu == null)
            return

        val searchItem = menu.findItem(R.id.friends_search_view)

        val searchView = searchItem.actionView as SearchView
        searchView.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PERSON_NAME

        // Set max width, remove left padding to assist in getting search view text aligned
        // with where Toolbar title previously was
        searchView.maxWidth = Integer.MAX_VALUE
        searchView.setPadding(
            0,
            searchView.paddingTop,
            searchView.paddingRight,
            searchView.paddingBottom
        )

        // Get the search view's inner LinearLayout and remove the left margin
        val searchEditFrame = searchView.findViewById<LinearLayout>(R.id.search_edit_frame)
        (searchEditFrame.layoutParams as LinearLayout.LayoutParams).leftMargin = 0

        searchView.setOnQueryTextFocusChangeListener { _, hasFocus ->
            mainActivity?.setToolbarShowsTitle(!hasFocus)

            if (!hasFocus)
                viewModel.hideSearch()
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                searchQueryChanged(query)
                return true
            }

            override fun onQueryTextChange(query: String): Boolean {
                searchQueryChanged(query)
                return true
            }
        })
    }

    private fun searchQueryChanged(query: String) {
        viewModel.onSearchQueryChanged(query)
    }

    override fun setupTabLayout(tabLayout: TabLayout) {
        tabLayout.isGone = true
    }

    private fun showMessage(message: String)
            = context?.toast(message, Toast.LENGTH_LONG)?.show()

    private fun showAddedMessage()
            = showMessage(getString(R.string.friend_request_sent_message))

    private fun showAcceptedMessage(friendName: String)
            = showMessage(getString(R.string.friend_request_accepted_message, friendName))

    private fun showRejectedMessage()
            = showMessage(getString(R.string.friend_request_rejected_message))

    companion object {
        // FriendRequestType IntDef
        const val RECEIVED = 0
        const val SENT = 1
    }
}
