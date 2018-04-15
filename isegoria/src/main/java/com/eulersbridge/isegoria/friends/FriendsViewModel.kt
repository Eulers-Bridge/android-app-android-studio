package com.eulersbridge.isegoria.friends

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.eulersbridge.isegoria.IsegoriaApp
import com.eulersbridge.isegoria.network.api.API
import com.eulersbridge.isegoria.network.api.models.Contact
import com.eulersbridge.isegoria.network.api.models.FriendRequest
import com.eulersbridge.isegoria.network.api.models.Institution
import com.eulersbridge.isegoria.network.api.models.User
import com.eulersbridge.isegoria.subscribeSuccess
import com.eulersbridge.isegoria.toBooleanSingle
import com.eulersbridge.isegoria.toLiveData
import com.eulersbridge.isegoria.util.data.RetrofitLiveData
import com.eulersbridge.isegoria.util.data.SingleLiveData
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import javax.inject.Inject

class FriendsViewModel
@Inject constructor(
    private val app: IsegoriaApp,
    private val api: API
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    internal var searchResults = MutableLiveData<List<User>>()
    internal var sentFriendRequests = MutableLiveData<List<FriendRequest>>()
    internal var receivedFriendRequests = MutableLiveData<List<FriendRequest>>()
    internal var friends = MutableLiveData<List<Contact>>()

    internal val searchSectionVisible = MutableLiveData<Boolean>()
    internal val sentRequestsVisible = MutableLiveData<Boolean>()
    internal val receivedRequestsVisible = MutableLiveData<Boolean>()
    internal val friendsVisible = MutableLiveData<Boolean>()

    init {
        searchSectionVisible.value = false
        sentRequestsVisible.value = false
        receivedRequestsVisible.value = false
        friendsVisible.value = true

        getFriends()
        getReceivedFriendRequests()
        getSentFriendRequests()
    }

    internal fun onExit() {
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
        cancelIfPossible(searchResults)
        compositeDisposable.dispose()
    }

    private fun showSearch() {
        searchSectionVisible.value = true
        sentRequestsVisible.value = false
        receivedRequestsVisible.value = false
        friendsVisible.value = false
    }

    internal fun hideSearch() {
        searchSectionVisible.value = false

        val haveSentRequests = sentFriendRequests.value?.isNotEmpty() ?: false
        sentRequestsVisible.value = haveSentRequests

        val haveReceivedRequests = receivedFriendRequests.value?.isNotEmpty() ?: false
        receivedRequestsVisible.value = haveReceivedRequests

        friendsVisible.value = true
    }

    internal fun onSearchQueryChanged(query: String) {
        showSearch()

        if (!query.isBlank() && query.length > 2) {
            api.searchForUsers(query)
                    .onErrorReturnItem(emptyList())
                    .subscribeSuccess {
                        searchResults.postValue(it)
                    }

        } else {
            searchResults.value = emptyList()
        }
    }

    private fun getFriends() {
        api.getFriends()
                .onErrorReturnItem(emptyList())
                .subscribeSuccess {
                    friends.postValue(it)
                }.addTo(compositeDisposable)
    }

    private fun getSentFriendRequests() {
        app.loggedInUser.value?.let { user ->
            api.getFriendRequestsSent(user.getId())
                    .onErrorReturnItem(emptyList())
                    .subscribeSuccess {
                        sentFriendRequests.postValue(it)
                        sentRequestsVisible.postValue(it.isNotEmpty())
                    }.addTo(compositeDisposable)
        }
    }

    private fun getReceivedFriendRequests() {
        app.loggedInUser.value?.let { user ->
            api.getFriendRequestsReceived(user.getId())
                    .onErrorReturnItem(emptyList())
                    .subscribeSuccess {
                        val filteredRequests = it.filter { it.accepted == null }

                        receivedFriendRequests.postValue(filteredRequests)
                        receivedRequestsVisible.postValue(filteredRequests.isNotEmpty())
                    }.addTo(compositeDisposable)
        }
    }

    internal fun addFriend(newFriendEmail: String): LiveData<Boolean> {
        if (!newFriendEmail.isBlank())
            api.addFriend(app.loggedInUser.value!!.email, newFriendEmail)
                    .toBooleanSingle()

        return SingleLiveData(false)
    }

    internal fun acceptFriendRequest(requestId: Long): LiveData<Boolean> {
        return api.acceptFriendRequest(requestId).doOnComplete {
            getReceivedFriendRequests()
            getFriends()
        } .toBooleanSingle().toLiveData()
    }

    internal fun rejectFriendRequest(requestId: Long): LiveData<Boolean> {
        return api.rejectFriendRequest(requestId).doOnComplete {
            getReceivedFriendRequests()
            getFriends()
        } .toBooleanSingle().toLiveData()
    }

    //TODO: Tidy
    internal fun getInstitution(institutionId: Long): LiveData<Institution>
        = api.getInstitution(institutionId).onErrorReturnItem(Institution(-1,-1,null,null,null, null, null)).toLiveData()

}
