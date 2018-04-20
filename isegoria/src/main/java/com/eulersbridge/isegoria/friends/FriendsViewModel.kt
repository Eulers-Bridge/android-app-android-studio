package com.eulersbridge.isegoria.friends

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.eulersbridge.isegoria.AppRouter
import com.eulersbridge.isegoria.Repository
import com.eulersbridge.isegoria.network.api.model.Contact
import com.eulersbridge.isegoria.network.api.model.FriendRequest
import com.eulersbridge.isegoria.network.api.model.Institution
import com.eulersbridge.isegoria.network.api.model.User
import com.eulersbridge.isegoria.util.data.SingleLiveData
import com.eulersbridge.isegoria.util.extension.subscribeSuccess
import com.eulersbridge.isegoria.util.extension.toBooleanSingle
import com.eulersbridge.isegoria.util.extension.toLiveData
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import javax.inject.Inject

class FriendsViewModel
@Inject constructor(
    private val appRouter: AppRouter,
    private val repository: Repository
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

    override fun onCleared() {
        compositeDisposable.dispose()
    }

    internal fun onDestroy() {
        appRouter.setFriendsScreenVisible(false)
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
            repository.searchForUsers(query).subscribeSuccess {
                searchResults.postValue(it)
            }

        } else {
            searchResults.value = emptyList()
        }
    }

    private fun getFriends() {
        repository.getFriends().subscribeSuccess {
            friends.postValue(it)
        }.addTo(compositeDisposable)
    }

    private fun getSentFriendRequests() {
        repository.getSentFriendRequests().subscribeSuccess {
            sentFriendRequests.postValue(it)
            sentRequestsVisible.postValue(it.isNotEmpty())
        }.addTo(compositeDisposable)
    }

    private fun getReceivedFriendRequests() {
        repository.getReceivedFriendRequests().subscribeSuccess {
            receivedFriendRequests.postValue(it)
            receivedRequestsVisible.postValue(it.isNotEmpty())
        }.addTo(compositeDisposable)
    }

    internal fun addFriend(newFriendEmail: String): LiveData<Boolean> {
        if (!newFriendEmail.isBlank())
            repository.addFriend(newFriendEmail).toLiveData()

        return SingleLiveData(false)
    }

    internal fun acceptFriendRequest(requestId: Long): LiveData<Boolean> {
        return repository.acceptFriendRequest(requestId).doOnComplete {
            getReceivedFriendRequests()
            getFriends()
        }.toBooleanSingle().toLiveData()
    }

    internal fun rejectFriendRequest(requestId: Long): LiveData<Boolean> {
        return repository.rejectFriendRequest(requestId).doOnComplete {
            getReceivedFriendRequests()
            getFriends()
        }.toBooleanSingle().toLiveData()
    }

    //TODO: Fix
    internal fun getInstitution(institutionId: Long): LiveData<Institution>
            = repository.getInstitution(institutionId).toLiveData()

}
