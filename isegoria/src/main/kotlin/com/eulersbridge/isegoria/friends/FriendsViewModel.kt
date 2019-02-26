package com.eulersbridge.isegoria.friends

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import com.eulersbridge.isegoria.AppRouter
import com.eulersbridge.isegoria.auth.login.LoginViewModel
import com.eulersbridge.isegoria.data.Repository
import com.eulersbridge.isegoria.network.api.model.*
import com.eulersbridge.isegoria.util.BaseViewModel
import com.eulersbridge.isegoria.util.data.SingleLiveData
import com.eulersbridge.isegoria.util.data.SingleLiveEvent
import com.eulersbridge.isegoria.util.extension.map
import com.eulersbridge.isegoria.util.extension.subscribeSuccess
import com.eulersbridge.isegoria.util.extension.toBooleanSingle
import com.eulersbridge.isegoria.util.extension.toLiveData
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.subjects.BehaviorSubject
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class FriendsViewModel @Inject constructor(private val appRouter: AppRouter, private val repository: Repository) : BaseViewModel() {

    enum class FriendStatus {
        FRIEND,
        PENDING,
        NOT_FRIEND
    }

    internal data class FriendSearchResult(val user: User, val friendStatus: FriendStatus)

    private val searchQuerySubject = BehaviorSubject.create<String>()
    private val searchResultsSubject = BehaviorSubject.create<List<FriendSearchResult>>()
    private val sentFriendRequestsSubject = BehaviorSubject.create<List<FriendRequest>>()
    private val receivedFriendRequestsSubject = BehaviorSubject.create<List<FriendRequest>>()
    private val friendsSubject = BehaviorSubject.create<List<Contact>>()

    internal var searchResults = MutableLiveData<List<FriendSearchResult>>()
    internal var sentFriendRequests = MutableLiveData<List<FriendRequest>>()
    internal var receivedFriendRequests = MutableLiveData<List<FriendRequest>>()
    internal var friends = MutableLiveData<List<Contact>>()

    internal val searchSectionVisible = MutableLiveData<Boolean>()
    internal val sentRequestsVisible = MutableLiveData<Boolean>()
    internal val receivedRequestsVisible = MutableLiveData<Boolean>()
    internal val friendsVisible = MutableLiveData<Boolean>()
    internal val toastMessage =  SingleLiveEvent<String>()

    init {
        searchSectionVisible.value = false
        sentRequestsVisible.value = false
        receivedRequestsVisible.value = false
        friendsVisible.value = true

        // setup observers
        createSearchResultsObserver()
        createReceivedFriendRequestsObserver()
        createSentFriendRequestsObserver()
        createFriendsObserver()

        // initialise subjects
        refreshFriends()
        refreshReceivedFriendRequests()
        refreshSentFriendRequests()

        // setup search results subject from other subjects
        createSearchResultsSubject()
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
        searchQuerySubject.onNext("")
        searchSectionVisible.value = false

        val haveSentRequests = sentFriendRequests.value?.isNotEmpty() ?: false
        sentRequestsVisible.value = haveSentRequests

        val haveReceivedRequests = receivedFriendRequests.value?.isNotEmpty() ?: false
        receivedRequestsVisible.value = haveReceivedRequests

        friendsVisible.value = true
    }

    internal fun onSearchQueryChanged(query: String) {
        showSearch()
        searchQuerySubject.onNext(query)
    }

    internal fun addFriend(newFriendEmail: String) {
        if (newFriendEmail.isBlank()) {
            toastMessage.postValue("Unable to add friend")
        } else {
            repository.addFriend(newFriendEmail)
                .doOnError { toastMessage.postValue("Unable to add friend") }
                .subscribe { success ->
                    if (success) {
                        refreshSentFriendRequests()
                        refreshFriends()
                        toastMessage.postValue("Friend added")
                    } else {
                        toastMessage.postValue("Unable to add friend")
                    }
                 }
                .addToDisposable()
        }
    }

    internal fun acceptFriendRequest(requestId: Long): LiveData<Boolean> {
        return repository.acceptFriendRequest(requestId).doOnComplete {
            refreshReceivedFriendRequests()
            refreshFriends()
        }.toBooleanSingle().toLiveData()
    }

    internal fun rejectFriendRequest(requestId: Long): LiveData<Boolean> {
        return repository.rejectFriendRequest(requestId).doOnComplete {
            refreshReceivedFriendRequests()
            refreshFriends()
        }.toBooleanSingle().toLiveData()
    }

    // Setup Subject Observers

    private fun createSearchResultsObserver() {
        searchResultsSubject
                .subscribe {
                    searchResults.postValue(it)
                }
                .addToDisposable()
    }

    private fun createFriendsObserver() {
        friendsSubject
                .subscribe {
                    friends.postValue(it)
                }
                .addToDisposable()
    }

    private fun createSentFriendRequestsObserver() {
        sentFriendRequestsSubject
                .subscribe {
                    sentFriendRequests.postValue(it)
                    sentRequestsVisible.postValue(it.isNotEmpty())
                }
                .addToDisposable()
    }

    private fun createReceivedFriendRequestsObserver() {
        receivedFriendRequestsSubject
                .subscribe {
                    receivedFriendRequests.postValue(it)
                    receivedRequestsVisible.postValue(it.isNotEmpty())
                }
                .addToDisposable()
    }

    private fun createSearchResultsSubject() {
         val userSearchResultsSubject = BehaviorSubject.create<List<User>>()

         searchQuerySubject
                .debounce(250, TimeUnit.MILLISECONDS)
                .distinctUntilChanged()
                .switchMapSingle {
                    if (it.isBlank() || it.length < 3) {
                        Single.just(emptyList())
                    } else {
                        repository.searchForUsers(it)
                    }
                }
                .onErrorReturnItem(emptyList())
                .subscribe { userSearchResultsSubject.onNext(it) }
                .addToDisposable()



        Observable.combineLatest(arrayOf(
                userSearchResultsSubject,
                sentFriendRequestsSubject,
                receivedFriendRequestsSubject,
                friendsSubject
        )) {
            val latestSearchResults = it[0] as List<User>
            val latestSentFriendRequests = it[1] as List<FriendRequest>
            val latestReceivedFriendRequests = it[2] as List<FriendRequest>
            val latestFriends = it[3] as List<Contact>

            // Search through the list of results
            // return the status of the friendship
            // the status then changes the associated icon
            latestSearchResults.map { searchResult ->
                FriendSearchResult(
                    searchResult,
                    when {
                        latestFriends.filter {
                            it.email == searchResult.email
                        }.isNotEmpty() -> FriendStatus.FRIEND
                        latestSentFriendRequests.filter {
                            if (it.requestReceiver != null) {
                                it.requestReceiver!!.email == searchResult.email
                            } else {
                                false
                            }
                        }.isNotEmpty() -> FriendStatus.PENDING
                        latestReceivedFriendRequests.filter {
                            if (it.requester != null) {
                                it.requester!!.email == searchResult.email
                            } else {
                                false
                            }
                        }.isNotEmpty() -> FriendStatus.PENDING
                        else -> FriendStatus.NOT_FRIEND
                    }
                )
            }
        }
                .subscribeBy { searchResultsSubject.onNext(it) }
                .addToDisposable()
    }


    // Refresh Subjects

    private fun refreshFriends() {
        repository.getFriends()
                .subscribeSuccess {friendsSubject.onNext(it) }
                .addToDisposable()
    }

    private fun refreshSentFriendRequests() {
        repository.getSentFriendRequests()
                .map { friendRequests ->
                    // if a friend request has been accepted or rejected the accepted property will be set
                    friendRequests.filter { it.accepted == null }
                }
                .subscribeSuccess {
                    sentFriendRequestsSubject.onNext(it)
                }
                .addToDisposable()
    }

    private fun refreshReceivedFriendRequests() {
        repository.getReceivedFriendRequests()
                .map { friendRequests ->
                    // if a friend request has been accepted or rejected the accepted property will be set
                    friendRequests.filter { it.accepted == null }
                }
                .subscribeSuccess {
                    receivedFriendRequestsSubject.onNext(it)
                }
                .addToDisposable()
    }
}
