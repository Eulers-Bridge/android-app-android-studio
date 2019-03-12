package com.eulersbridge.isegoria.election.candidates.profile

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.eulersbridge.isegoria.data.Repository
import com.eulersbridge.isegoria.network.api.model.Candidate
import com.eulersbridge.isegoria.network.api.model.Like
import com.eulersbridge.isegoria.util.data.SingleLiveEvent
import com.eulersbridge.isegoria.util.extension.toLiveData
import io.reactivex.BackpressureStrategy
import io.reactivex.Completable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.processors.BehaviorProcessor
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.BehaviorSubject
import kotlinx.android.synthetic.main.candidate_profile_fragment.*
import javax.inject.Inject

class CandidateProfileViewModel @Inject constructor(
                                private val repository: Repository) : ViewModel() {

     // treat this variable as a lateinit val (can't actually create lateinit for a primitive type)

    // Subjects
    private val compositeDisposable = CompositeDisposable()

    private var candidateIdSubject =  BehaviorSubject.create<Long>()
    private val candidateSubject = BehaviorSubject.create<Candidate>()
    private val candidateLikesSubject = BehaviorSubject.create<List<Like>>()
    private val candidateLikedByUserSubject = BehaviorSubject.create<Boolean>()
    private val candidateAddedAsFriendSubject = BehaviorSubject.create<Boolean>()

    // Live Data
    internal var candidateName = MutableLiveData<String>()
    internal var candidateProfilePhotoUrl = MutableLiveData<String>()
    internal var candidateInstitutionName = MutableLiveData<String>()
    internal var candidateDescription = MutableLiveData<String>()
    internal var candidateLikedByUser = candidateLikedByUserSubject.toLiveData(BackpressureStrategy.LATEST)
    internal var candidateAddedAsFriend = candidateAddedAsFriendSubject.toLiveData(BackpressureStrategy.LATEST)

    internal val toastMessage =  SingleLiveEvent<String>()

    init {
        createCandidateIdObserver()
        createCandidateObserver()
        createCandidateLikesObserver()
    }

    // events

    internal fun setCandidateId(candidateId: Long?) {
        if (candidateId != null) {
            this.candidateIdSubject.onNext(candidateId)
        } else {
            toastMessage.postValue("Error occurred")
        }
    }

    internal fun onCandidateLiked() {
        if (candidateIdSubject.hasValue()) {
            val candidateId = candidateIdSubject.value!!

            repository.likeCandidate(candidateId)
                    .doOnComplete {
                        refreshCandidateLikes(candidateId)
                    }
                    .doOnError {
                        toastMessage.postValue("Error Occurred")
                    }
                    .subscribe()
                    .addTo(compositeDisposable)
        }


    }

    internal fun onCandidateUnliked() {
        if (candidateIdSubject.hasValue()) {
            val candidateId = candidateIdSubject.value!!

            repository.unlikeCandidate(candidateId)
                    .doOnComplete {
                        refreshCandidateLikes(candidateId)
                    }
                    .doOnError {
                        toastMessage.postValue("Error Occurred")
                    }
                    .subscribe()
                    .addTo(compositeDisposable)
        }
    }

    // Sends a friend request to the candidate
    internal fun onAddCandidateAsFriend() {
        repository.addFriend(candidateSubject.value!!.userProfile.email)
                .doOnSuccess {
                    toastMessage.postValue("Added as friend")
                    refreshFriendStatus(candidateSubject.value!!.userProfile.email)
                }
                .doOnError {
                    toastMessage.postValue("Error Occurred")
                }
                .subscribe()
                .addTo(compositeDisposable)
    }

    // Unfriends the candidate
    internal fun onRemoveCandidateAsFriend() {
        val candidateEmail = candidateSubject.value!!.userProfile.email

        repository
                .getFriendStatusAndPendingFriendRequest(candidateEmail)
                .doOnSuccess {
                    val candidateIsFriend = it.first
                    val friendRquest = it.second

                    if (candidateIsFriend || friendRquest != null) {
                        val completable: Completable = if (candidateIsFriend) {
                            repository.removeFriend(candidateEmail)
                                    .doOnComplete {
                                        toastMessage.postValue("Candidate removed as friend")
                                    }
                        } else {
                            repository.revokeFriendRequest(friendRquest!!.id)
                                    .doOnComplete {
                                        toastMessage.postValue("Friend request revoked")
                                    }
                        }

                        completable
                                .doOnComplete {refreshFriendStatus(candidateSubject.value!!.userProfile.email) }
                                .doOnError {
                                    toastMessage.postValue("Error Occurred")
                                }
                                .subscribe()
                                .addTo(compositeDisposable)
                    }
                }
                .doOnError {
                    toastMessage.postValue("Error Occurred")
                }
                .subscribe()
                .addTo(compositeDisposable)
    }

    // observers

    private fun createCandidateIdObserver() {
        candidateIdSubject
                .doOnNext {candidateId ->
                    refreshCandidate(candidateId)
                    refreshCandidateLikes(candidateId)
                }
                .subscribe()
                .addTo(compositeDisposable)
    }

    private fun createCandidateObserver() {
        candidateSubject
            .doOnNext { candidate ->
                candidateName.postValue(candidate.name)
                candidateProfilePhotoUrl.postValue(candidate.userProfile.profilePhotoURL)
                candidateDescription.postValue(candidate.information)

                if (candidate.userProfile.institutionId != null) {
                    repository.getInstitution(candidate.userProfile.institutionId!!)
                            .doOnSuccess {
                                val institution = it.value

                                if (institution != null) {
                                    candidateInstitutionName.postValue(institution.getName())
                                }
                            }
                            .subscribe()
                            .addTo(compositeDisposable)
                }

                refreshFriendStatus(candidate.userProfile.email)
            }
            .doOnError {
                toastMessage.postValue("Error occurred")
            }
            .subscribe()
            .addTo(compositeDisposable)
    }

    private fun createCandidateLikesObserver() {
        val usersEmail = repository.getUserFromLoginState().email

        candidateLikesSubject
            .doOnNext {candidateLikes ->
                candidateLikedByUserSubject
                        .onNext(
                                candidateLikes.any { like -> like.email == usersEmail }
                        )
            }
            .doOnError {
                toastMessage.postValue("Error occurred")
            }
            .subscribe()
            .addTo(compositeDisposable)
    }

    private fun refreshCandidate(candidateId: Long) {
        repository
                .getCandidate(candidateId!!)
                .doOnSuccess {
                    candidateSubject.onNext(it)
                }
                .doOnError {
                    candidateSubject.onError(it)
                }
                .subscribe()
                .addTo(compositeDisposable)
    }

    private fun refreshCandidateLikes(candidateId: Long) {
        repository
                .getCandidateLikes(candidateId)
                .doOnSuccess {
                    candidateLikesSubject.onNext(it)
                }
                .doOnError {
                    candidateLikesSubject.onError(it)
                }
                .subscribe()
                .addTo(compositeDisposable)
    }

    private fun refreshFriendStatus(candidateEmail: String) {
        repository
                .getUserAddedAsFriend(candidateEmail)
                .doOnSuccess {
                    candidateAddedAsFriendSubject.onNext(it)
                }
                .doOnError {
                    candidateAddedAsFriendSubject.onError(it)
                }
                .subscribe()
                .addTo(compositeDisposable)
    }
}
