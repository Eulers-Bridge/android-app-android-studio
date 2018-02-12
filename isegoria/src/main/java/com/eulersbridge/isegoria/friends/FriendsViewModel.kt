package com.eulersbridge.isegoria.friends

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import com.eulersbridge.isegoria.IsegoriaApp
import com.eulersbridge.isegoria.network.api.models.Contact
import com.eulersbridge.isegoria.network.api.models.FriendRequest
import com.eulersbridge.isegoria.network.api.models.Institution
import com.eulersbridge.isegoria.network.api.models.User
import com.eulersbridge.isegoria.util.data.RetrofitLiveData
import com.eulersbridge.isegoria.util.data.SingleLiveData

class FriendsViewModel(application: Application) : AndroidViewModel(application) {

    private var searchResults: LiveData<List<User>?>? = null
    private var sentFriendRequests: LiveData<List<FriendRequest>?>? = null
    private var receivedFriendRequests: LiveData<List<FriendRequest>?>? = null
    private var friends: LiveData<List<Contact>>? = null

    internal val searchSectionVisible = MutableLiveData<Boolean>()
    internal val sentRequestsVisible = MutableLiveData<Boolean>()
    internal val receivedRequestsVisible = MutableLiveData<Boolean>()
    internal val friendsVisible = MutableLiveData<Boolean>()

    init {
        searchSectionVisible.value = false
        sentRequestsVisible.value = false
        receivedRequestsVisible.value = false
        friendsVisible.value = true
    }

    internal fun onExit() {
        val app = getApplication<IsegoriaApp>()
        app.friendsVisible.value = false
    }

    /**
     * Convenience method to cancel a LiveData object if it exists and is a Retrofit API request.
     */
    private fun cancelIfPossible(liveData: LiveData<*>?) {
        (liveData as? RetrofitLiveData)?.cancel()
    }

    override fun onCleared() {
        cancelIfPossible(friends)
        cancelIfPossible(sentFriendRequests)
        cancelIfPossible(receivedFriendRequests)
        cancelIfPossible(searchResults)
    }

    private fun showSearch() {
        searchSectionVisible.value = true
        sentRequestsVisible.value = false
        receivedRequestsVisible.value = false
        friendsVisible.value = false
    }

    internal fun hideSearch() {
        searchSectionVisible.value = false

        val haveSentRequests: Boolean = sentFriendRequests?.value?.isNotEmpty() ?: false
        sentRequestsVisible.value = haveSentRequests

        val haveReceivedRequests: Boolean = receivedFriendRequests?.value?.isNotEmpty() ?: false
        receivedRequestsVisible.value = haveReceivedRequests

        friendsVisible.value = true
    }

    internal fun onSearchQueryChanged(query: String): LiveData<List<User>?>? {
        showSearch()

        searchResults = if (!query.isBlank() && query.length > 2) {
            val app = getApplication<IsegoriaApp>()
            RetrofitLiveData(app.api.searchForUsers(query))

        } else {
            SingleLiveData(null)
        }

        return searchResults
    }

    internal fun getFriends(): LiveData<List<Contact>> {
        if (friends == null) {
            val app = getApplication<IsegoriaApp>()
            friends = RetrofitLiveData(app.api.getFriends())
        }

        return friends!!
    }

    internal fun getSentFriendRequests(): LiveData<List<FriendRequest>?>? {
        if (sentFriendRequests == null) {
            val app = getApplication<IsegoriaApp>()

            return Transformations.switchMap(app.loggedInUser) { user ->
                if (user != null) {

                    val requests = RetrofitLiveData(app.api.getFriendRequestsSent(user.getId()))
                    sentFriendRequests = Transformations.switchMap(requests) { sentFriendRequests ->
                        sentRequestsVisible.value = sentFriendRequests != null && sentFriendRequests.isNotEmpty()
                        SingleLiveData(sentFriendRequests)
                    }

                    sentFriendRequests
                }

                SingleLiveData<List<FriendRequest>?>(null)
            }
        }

        return sentFriendRequests
    }

    internal fun getReceivedFriendRequests(): LiveData<List<FriendRequest>?>? {
        if (receivedFriendRequests == null) {
            val app = getApplication<IsegoriaApp>()

            return Transformations.switchMap(app.loggedInUser) { user ->
                if (user != null) {
                    val requests = RetrofitLiveData(app.api.getFriendRequestsReceived(user.getId()))
                    receivedFriendRequests =
                            Transformations.switchMap(requests) { sentFriendRequests ->

                                receivedRequestsVisible.value =
                                        sentFriendRequests != null && sentFriendRequests.isNotEmpty()

                                SingleLiveData(sentFriendRequests)
                            }

                    receivedFriendRequests
                }

                SingleLiveData<List<FriendRequest>?>(null)
            }
        }

        return receivedFriendRequests
    }

    internal fun addFriend(newFriendEmail: String): LiveData<Boolean> {
        if (!newFriendEmail.isBlank()) {

            val app = getApplication<IsegoriaApp>()

            return Transformations.switchMap(app.loggedInUser) { (_, _, email) ->
                val friendRequest = RetrofitLiveData(app.api.addFriend(email, newFriendEmail))
                Transformations.switchMap(friendRequest) { SingleLiveData(true) }
            }
        }

        return SingleLiveData(false)
    }

    internal fun acceptFriendRequest(requestId: Long): LiveData<Boolean> {
        val app = getApplication<IsegoriaApp>()

        val friendRequest = RetrofitLiveData(app.api.acceptFriendRequest(requestId))
        return Transformations.switchMap(friendRequest) {
            getReceivedFriendRequests()
            getFriends()

            SingleLiveData(true)
        }
    }

    internal fun rejectFriendRequest(requestId: Long): LiveData<Boolean> {
        val app = getApplication<IsegoriaApp>()

        val friendRequest = RetrofitLiveData(app.api.rejectFriendRequest(requestId))
        return Transformations.switchMap(friendRequest) {
            getReceivedFriendRequests()
            getFriends()

            SingleLiveData(true)
        }
    }

    internal fun getInstitution(institutionId: Long): LiveData<Institution> {
        val app = getApplication<IsegoriaApp>()

        return RetrofitLiveData(app.api.getInstitution(institutionId))
    }

}
