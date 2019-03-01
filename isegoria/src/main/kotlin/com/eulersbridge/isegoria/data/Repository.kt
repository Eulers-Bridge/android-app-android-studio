package com.eulersbridge.isegoria.data

import android.net.Uri
import com.eulersbridge.isegoria.auth.signup.SignUpUser
import com.eulersbridge.isegoria.network.api.model.*
import com.eulersbridge.isegoria.network.api.response.PhotosResponse
import com.eulersbridge.isegoria.util.data.Optional
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

interface Repository {
    fun getLoginState(): Observable<LoginState>

    fun getSavedEmail(): String?
    fun getSavedPassword(): String?

    fun getUser(): User

    fun resendVerificationEmail(): Completable
    fun login(email: String, password: String)
    fun logOut(): Completable
    fun signUp(user: SignUpUser): Completable

    fun setUserTrackingOff(trackingOff: Boolean): Completable
    fun setUserOptedOutOfDataCollection(isOptedOutOfDataCollection: Boolean): Completable
    fun addUserSelfEfficacyAnswers(answers: UserSelfEfficacy): Completable
    fun addUserPersonality(personality: UserPersonality): Completable

    fun getUserProfilePhotoUrl(): String?
    fun getUserPhoto(): Single<Optional<Photo>>
    fun setUserPhoto(imageUri: Uri): Completable

    fun getSignUpCountries(): Single<List<Country>>

    fun getTasks(): Single<List<Task>>
    fun getRemainingTasks(): Single<List<Task>>
    fun getCompletedTasks(): Single<List<Task>>

    fun getUserRemainingBadges(userId: Long?): Single<List<Badge>>
    fun getUserCompletedBadges(userId: Long?): Single<List<Badge>>

    fun getContact(email: String): Single<Optional<Contact>>

    fun getNewsArticles(): Single<List<NewsArticle>>
    fun getNewsArticleLikes(articleId: Long): Single<List<Like>>
    fun likeArticle(articleId: Long): Single<Boolean>
    fun unlikeArticle(articleId: Long): Single<Boolean>

    fun getLatestElection(): Single<Optional<Election>>
    fun getLatestElectionCandidates(): Single<List<Candidate>>
    fun getLatestElectionTickets(): Single<List<CandidateTicket>>
    fun getLatestElectionPositions(): Single<List<Position>>

    fun getPosition(id: Long): Single<Optional<Position>>
    fun getPositionCandidates(positionId: Long): Single<List<Candidate>>
    fun getTicket(id: Long): Single<Optional<Ticket>>
    fun getTicketCandidates(ticketId: Long): Single<List<Candidate>>

    fun getVoteLocations(): Single<List<VoteLocation>>
    fun createUserVoteReminder(electionId: Long, voteLocation: String, date: Long): Completable
    fun getLatestUserVoteReminder(): Single<VoteReminder>
    fun getUserVoteReminderExists(): Single<Boolean>
    fun getUserSupportedTicket(ticketId: Long): Single<Boolean>

    fun supportTicket(ticketId: Long): Completable
    fun unsupportTicket(ticketId: Long): Completable

    fun getEvents(): Single<List<Event>>

    fun getPhotoAlbums(): Single<List<PhotoAlbum>>
    fun getPhotoLikes(photoId: Long): Single<List<Like>>
    fun likePhoto(photoId: Long): Single<Boolean>
    fun unlikePhoto(photoId: Long): Single<Boolean>
    fun getPhoto(id: Long): Single<Optional<Photo>>
    fun getPhotos(id: Long): Single<PhotosResponse>
    fun getAlbumPhotos(photoAlbumId: Long): Single<List<Photo>>

    fun getFriends(): Single<List<Contact>>
    fun getSentFriendRequests(): Single<List<FriendRequest>>
    fun getReceivedFriendRequests(): Single<List<FriendRequest>>
    fun addFriend(newFriendEmail: String): Single<Boolean>
    fun acceptFriendRequest(requestId: Long): Completable
    fun rejectFriendRequest(requestId: Long): Completable
    fun searchForUsers(query: String): Single<List<User>>

    fun getInstitution(institutionId: Long): Single<Optional<Institution>>
    fun getInstitutionName(institutionId: Long): Single<Optional<String>>

    fun getPolls(): Single<List<Poll>>
    fun answerPoll(pollId: Long, answerId: Long): Completable
    fun getPollResults(pollId: Long): Single<List<PollResult>>
}