package com.eulersbridge.isegoria.friends

import android.arch.lifecycle.Observer
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
import com.eulersbridge.isegoria.FRAGMENT_EXTRA_CONTACT
import com.eulersbridge.isegoria.FRAGMENT_EXTRA_USER
import com.eulersbridge.isegoria.MainActivity
import com.eulersbridge.isegoria.R
import com.eulersbridge.isegoria.network.api.models.Contact
import com.eulersbridge.isegoria.network.api.models.FriendRequest
import com.eulersbridge.isegoria.network.api.models.User
import com.eulersbridge.isegoria.profile.ProfileOverviewFragment
import com.eulersbridge.isegoria.util.ui.TitledFragment
import kotlinx.android.synthetic.main.friends_fragment.*
import java.lang.ref.WeakReference

class FriendsFragment : Fragment(), TitledFragment, MainActivity.TabbedFragment,
    FriendAdapter.Delegate, SearchAdapter.UserDelegate, FriendRequestAdapter.Delegate {

    private val searchAdapter = SearchAdapter(this)
    private val receivedAdapter = FriendRequestAdapter(RECEIVED, this)
    private val sentAdapter = FriendRequestAdapter(SENT, this)
    private var friendsAdapter: FriendAdapter = FriendAdapter(this)

    private lateinit var viewModel: FriendsViewModel

    private var mainActivity: MainActivity? = null

    @IntDef(RECEIVED.toLong(), SENT.toLong())
    @Retention(AnnotationRetention.SOURCE)
    internal annotation class FriendRequestType

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.friends_fragment, container, false)

        mainActivity = activity as MainActivity?
        mainActivity?.invalidateOptionsMenu()

        setHasOptionsMenu(true)

        viewModel = ViewModelProviders.of(this).get(FriendsViewModel::class.java)

        viewModel.searchSectionVisible.observe(this, observeVisibility { visible ->
            searchContainer.visibility = visible
        })

        viewModel.friendsVisible.observe(this, observeVisibility { visible ->
            friendsContainer.visibility = visible
        })

        viewModel.sentRequestsVisible.observe(this, observeVisibility { visible ->
            sentRequestsContainer.visibility = visible
        })

        viewModel.receivedRequestsVisible.observe(this, observeVisibility { visible ->
            receivedRequestsContainer.visibility = visible
        })

        viewModel.receivedRequestsVisible.observe(this, observeVisibility { visible ->
            receivedRequestsContainer.visibility = visible
        })

        viewModel.getFriends().observe(this, Observer { friends ->
            if (friends != null)
                friendsAdapter.setItems(friends)
        })

        viewModel.getSentFriendRequests()?.observe(this, Observer { requests ->
            if (requests != null)
                sentAdapter.setItems(requests)
        })

        getReceivedFriendRequests()

        return rootView
    }

    /**
     * Convenience function to map a boolean to a view visibility int
     */
    private inline fun observeVisibility(crossinline f: (visibility: Int) -> Unit) : Observer<Boolean>
            = Observer { f(if (it == true) View.VISIBLE else View.GONE) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        searchResultsList.adapter = searchAdapter
        receivedRequestsList.adapter = receivedAdapter
        sentRequestsList.adapter = sentAdapter
        friendsList.adapter = friendsAdapter
    }

    override fun onDestroy() {
        super.onDestroy()

        viewModel.onExit()
    }

    private fun getReceivedFriendRequests() {
        viewModel.getReceivedFriendRequests()?.observe(this, Observer { requests ->
            if (requests != null)
                receivedAdapter.setItems(requests)
        })
    }

    override fun getContactInstitution(
        institutionId: Long,
        weakViewHolder: WeakReference<UserViewHolder>
    ) {
        viewModel.getInstitution(institutionId).observe(
            this,
            Observer { institution -> friendsAdapter.setInstitution(institution, weakViewHolder) }
        )
    }

    override fun getSearchedUserInstitution(
        institutionId: Long,
        weakViewHolder: WeakReference<UserViewHolder>
    ) {
        viewModel.getInstitution(institutionId).observe(
            this,
            Observer { institution -> searchAdapter.setInstitution(institution, weakViewHolder) }
        )
    }

    override fun getFriendRequestInstitution(
        institutionId: Long, @FriendRequestType type: Int,
        weakViewHolder: WeakReference<RecyclerView.ViewHolder>
    ) {
        viewModel.getInstitution(institutionId).observe(this, Observer { institution ->
            if (type == RECEIVED)
                receivedAdapter.setInstitution(institution, weakViewHolder)
            else
                sentAdapter.setInstitution(institution, weakViewHolder)
        })
    }

    override fun performFriendRequestAction(type: Int, request: FriendRequest) {
        if (type == RECEIVED) {

            // Show accept or reject dialog
            if (context != null)
                AlertDialog.Builder(context!!)
                    .setTitle(getString(R.string.friend_request_action_dialog_title) + request.requester!!.fullName)
                    .setPositiveButton(
                        R.string.friend_request_action_dialog_positive
                    ) { _, _ -> acceptFriendRequest(request) }
                    .setNegativeButton(
                        R.string.friend_request_action_dialog_negative
                    ) { _, _ -> rejectFriendRequest(request) }
                    .setNeutralButton(R.string.friend_request_action_dialog_neutral, null)
                    .show()

        } else if (type == SENT && mainActivity != null) {
            val user = request.requestReceiver

            val args = Bundle()
            args.putParcelable(FRAGMENT_EXTRA_USER, user)

            val profileFragment = ProfileOverviewFragment()
            profileFragment.arguments = args

            mainActivity!!.presentContent(profileFragment)
        }
    }

    private fun acceptFriendRequest(friendRequest: FriendRequest) {
        viewModel.acceptFriendRequest(friendRequest.id).observe(this, Observer { success ->
            if (success == true)
                showAcceptedMessage(friendRequest.requester!!.fullName)
        })
    }

    private fun rejectFriendRequest(friendRequest: FriendRequest) {
        viewModel.rejectFriendRequest(friendRequest.id).observe(this, Observer { success ->
            if (success == true)
                showRejectedMessage()
        })
    }

    override fun onSearchedUserClick(user: User?) {
        val args = Bundle()
        args.putParcelable(FRAGMENT_EXTRA_USER, user)

        val profileFragment = ProfileOverviewFragment()
        profileFragment.arguments = args

        mainActivity?.presentContent(profileFragment)
    }

    override fun onSearchedUserActionClick(user: User?) {
        if (user != null)
            viewModel.addFriend(user.email).observe(this, Observer { showAddedMessage() })
    }

    override fun onContactClick(contact: Contact) {
        val args = Bundle()
        args.putParcelable(FRAGMENT_EXTRA_CONTACT, contact)

        val profileFragment = ProfileOverviewFragment()
        profileFragment.arguments = args

        mainActivity?.presentContent(profileFragment)
    }

    override fun getTitle(context: Context?) = context?.getString(R.string.section_title_friends)

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater!!.inflate(R.menu.friends, menu)
        super.onCreateOptionsMenu(menu, inflater)

        val searchItem = menu!!.findItem(R.id.friends_search_view)

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
            mainActivity!!.setToolbarShowsTitle(!hasFocus)

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
        viewModel.onSearchQueryChanged(query)?.observe(this, Observer { searchResults ->
            searchAdapter.clearItems()

            if (searchResults != null)
                searchAdapter.setItems(searchResults)
        })
    }

    override fun setupTabLayout(tabLayout: TabLayout) {
        tabLayout.visibility = View.GONE
    }

    private fun showMessage(message: String)
            = Toast.makeText(context, message, Toast.LENGTH_LONG).show()

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
