package com.eulersbridge.isegoria.data

import android.net.Uri
import androidx.core.net.toFile
import com.eulersbridge.isegoria.USER_EMAIL_KEY
import com.eulersbridge.isegoria.USER_PASSWORD_KEY
import com.eulersbridge.isegoria.network.NetworkService
import com.eulersbridge.isegoria.network.api.API
import com.eulersbridge.isegoria.network.api.model.*
import com.eulersbridge.isegoria.util.data.Optional
import com.eulersbridge.isegoria.util.extension.edit
import com.eulersbridge.isegoria.util.extension.toBooleanSingle
import com.securepreferences.SecurePreferences
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.subjects.BehaviorSubject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Repository @Inject constructor(
        private val api: API,
        private val networkService: NetworkService,
        private val securePreferences: SecurePreferences
) {

    val loginState = BehaviorSubject.createDefault<LoginState>(LoginState.LoggedOut())!!

    private var cachedLoginArticles: List<NewsArticle>? = null

    fun getSavedEmail(): String? = securePreferences.getString(USER_EMAIL_KEY, null)
    fun getSavedPassword(): String? = securePreferences.getString(USER_PASSWORD_KEY, null)

    @Throws(NullPointerException::class)
    private fun requireUser(): User {
        val loginStateData = loginState.value as? LoginState.LoggedIn
        return loginStateData?.user!!
    }

    @Throws(NullPointerException::class)
    fun getUser(): User {
        return requireUser()
    }

    fun resendVerificationEmail(): Completable {
        val user = requireUser()

        return api.resendVerificationEmail(user.email)
    }

    fun login(email: String, password: String) {
        loginState.onNext(LoginState.LoggingIn())

        networkService.login(email, password).subscribeBy(
                onSuccess = {
                    cachedLoginArticles = it.articles

                    val user = it.user.copy(id = it.userId)

                    api.getInstitutionNewsFeed(user.institutionId!!)
                            .map { it.newsFeedId }
                            .subscribeBy(
                                    onSuccess = { newsFeedId ->
                                        val updatedUser = user.copy(newsFeedId = newsFeedId)
                                        loginState.onNext(LoginState.LoggedIn(updatedUser))
                                    },
                                    onError = {
                                        loginState.onNext(LoginState.LoggedIn(user))
                                    }
                            )

                    securePreferences.edit {
                        putString(USER_EMAIL_KEY, email)
                        putString(USER_PASSWORD_KEY, password)
                    }
                },
                onError = {
                    loginState.onNext(LoginState.LoginFailure())
                }
        )
    }

    fun logout(): Completable {
        cachedLoginArticles = null

        securePreferences.edit {
            remove(USER_PASSWORD_KEY)
        }

        loginState.onNext(LoginState.LoggedOut())

        return networkService.logout()
    }

    private fun updateUser(updatedUser: User): Completable {
        val userSettings = UserSettings(updatedUser.trackingOff, updatedUser.isOptedOutOfDataCollection)

        return api.updateUser(updatedUser.email, userSettings).doOnComplete {
            loginState.onNext(LoginState.LoggedIn(updatedUser))
        }
    }

    fun setUserTrackingOff(trackingOff: Boolean): Completable {
        val updatedUser = requireUser().copy(trackingOff = trackingOff)
        return updateUser(updatedUser)
    }

    fun setUserOptedOutOfDataCollection(isOptedOutOfDataCollection: Boolean): Completable {
        val updatedUser = requireUser().copy(isOptedOutOfDataCollection = isOptedOutOfDataCollection)
        return updateUser(updatedUser)
    }

    private fun setUserSelfEfficacyCompleted(): Completable {
        // TODO: Rework – updateUser useless for this
        val updatedUser = requireUser().copy(hasPPSEQuestions = true)
        return updateUser(updatedUser)
    }

    fun addUserSelfEfficacyAnswers(answers: UserSelfEfficacy): Completable {
        val user = requireUser()

        return api.addUserEfficacy(user.email, answers).doOnComplete {
            setUserSelfEfficacyCompleted()
        }
    }

    private fun setUserPersonalityCompleted(): Completable {
        // TODO: Rework – updateUser useless for this
        val updatedUser = requireUser().copy(hasPersonality = true)
        return updateUser(updatedUser)
    }

    fun addUserPersonality(personality: UserPersonality): Completable {
        val user = requireUser()
        return api.addUserPersonality(user.email, personality).doOnSuccess {
            setUserPersonalityCompleted()
        }.toCompletable()
    }

    fun getUserProfilePhotoUrl(): String? = requireUser().profilePhotoURL

    fun getUserPhoto(): Single<Optional<Photo>> {
        val user = requireUser()

        return api.getPhotos(user.email)
                .map { it.photos }
                .onErrorReturnItem(emptyList())
                .map {
                    Optional(it.firstOrNull())
                }
    }

    fun setUserPhoto(imageUri: Uri): Completable {
        return networkService.uploadNewUserPhoto(requireUser(), imageUri.toFile())
    }

    fun getTasks(): Single<List<Task>> {
        return api.getTasks().onErrorReturnItem(emptyList())
    }

    fun getRemainingTasks(): Single<List<Task>> {
        val user = requireUser()
        return api.getRemainingTasks(user.getId()).onErrorReturnItem(emptyList())
    }

    fun getCompletedTasks(): Single<List<Task>> {
        val user = requireUser()
        return api.getCompletedTasks(user.getId()).onErrorReturnItem(emptyList())
    }

    fun getNewsArticles(): Single<List<NewsArticle>> {
        return cachedLoginArticles?.let {
            Single.just(it)
        } ?: api.getNewsArticles(requireUser().institutionId!!)
                .onErrorReturnItem(emptyList())
                .doOnSuccess { cachedLoginArticles = null }
    }

    fun getNewsArticleLikes(articleId: Long): Single<List<Like>> {
        return api.getNewsArticleLikes(articleId)
                .onErrorReturnItem(emptyList())
    }

    fun likeArticle(articleId: Long): Single<Boolean> {
        val user = requireUser()

        return api.likeArticle(articleId, user.email)
                .map { it.success }
                .onErrorReturnItem(false)
    }

    fun unlikeArticle(articleId: Long): Single<Boolean> {
        val user = requireUser()
        return api.unlikeArticle(articleId, user.email).toBooleanSingle()
    }

    private fun getElections(): Single<List<Election>> {
        return api.getElections(requireUser().institutionId!!)
                .onErrorReturnItem(emptyList())
    }

    fun getLatestElection(): Single<Optional<Election>> {
        return getElections()
                .map { Optional(it.firstOrNull()) }
                .onErrorReturnItem(Optional())
    }

    fun getLatestElectionCandidates(): Single<List<Candidate>> {
        return getLatestElection().flatMap {
            it.value?.let {
                api.getElectionCandidates(it.id)

            } ?: Single.just(emptyList())
        }.onErrorReturnItem(emptyList())
    }

    fun getLatestElectionTickets(): Single<List<CandidateTicket>> {
        return getLatestElection().flatMap {
            it.value?.let {
                api.getTickets(it.id)

            } ?: Single.just(emptyList())
        }.onErrorReturnItem(emptyList())
    }

    fun getLatestElectionPositions(): Single<List<Position>> {
        return getLatestElection().flatMap {
            it.value?.let {
                api.getElectionPositions(it.id)

            } ?: Single.just(emptyList())
        }.onErrorReturnItem(emptyList())
    }

    fun getVoteLocations(): Single<List<VoteLocation>> {
        val user = requireUser()
        return api.getVoteLocations(user.institutionId!!).onErrorReturnItem(emptyList())
    }

    fun createUserVoteReminder(electionId: Long, voteLocation: String, date: Long): Completable {
        val user = requireUser()
        val reminder = VoteReminder(user.email, electionId, voteLocation, date)

        return api.addVoteReminder(user.email, reminder)
    }

    fun getUserVoteReminderExists(): Single<Boolean> {
        val user = requireUser()
        return api.getVoteReminders(user.email)
                .map { it.firstOrNull() == null }
                .onErrorReturnItem(false)
    }

    fun getUserSupportedTicket(ticketId: Long): Single<Boolean> {
        val user = requireUser()
        return api.getUserSupportedTickets(user.email).map {
            it.singleOrNull { ticket ->
                ticket.id == ticketId
            } != null
        }
    }

    fun supportTicket(ticketId: Long): Completable {
        val user = requireUser()
        return api.supportTicket(ticketId, user.email)
    }

    fun unsupportTicket(ticketId: Long): Completable {
        val user = requireUser()
        return api.unsupportTicket(ticketId, user.email)
    }

    fun getEvents(): Single<List<Event>> {
        val user = requireUser()
        return api.getEvents(user.institutionId!!).onErrorReturnItem(emptyList())
    }

    fun getPhotoAlbums(): Single<List<PhotoAlbum>> {
        val user = requireUser()
        return api.getPhotoAlbums(user.newsFeedId)
                .onErrorReturnItem(emptyList())
    }

    fun getPhotoLikes(photoId: Long): Single<List<Like>> {
        return api.getPhotoLikes(photoId).onErrorReturnItem(emptyList())
    }

    fun likePhoto(photoId: Long): Single<Boolean> {
        val user = requireUser()
        return api.likePhoto(photoId, user.email)
                .map { it.success }
                .onErrorReturnItem(false)
    }

    fun unlikePhoto(photoId: Long): Single<Boolean> {
        val user = requireUser()
        return api.unlikePhoto(photoId, user.email).toBooleanSingle()
    }

    fun getFriends(): Single<List<Contact>> {
        return api.getFriends().onErrorReturnItem(emptyList())
    }

    fun getSentFriendRequests(): Single<List<FriendRequest>> {
        val user = requireUser()
        return api.getFriendRequestsSent(user.getId())
                .onErrorReturnItem(emptyList())
    }

    fun getReceivedFriendRequests(): Single<List<FriendRequest>> {
        val user = requireUser()
        return api.getFriendRequestsReceived(user.getId())
                .onErrorReturnItem(emptyList())
                .map { it.filter { it.accepted == null } }
    }

    fun addFriend(newFriendEmail: String): Single<Boolean> {
        val user = requireUser()
        return api.addFriend(user.email, newFriendEmail).toBooleanSingle()
    }

    fun acceptFriendRequest(requestId: Long): Completable {
        return api.acceptFriendRequest(requestId)
    }

    fun rejectFriendRequest(requestId: Long): Completable {
        return api.rejectFriendRequest(requestId)
    }

    fun searchForUsers(query: String): Single<List<User>> {
        return api.searchForUsers(query).onErrorReturnItem(emptyList())
    }

    fun getInstitution(institutionId: Long): Single<Optional<Institution>> {
        return api.getInstitution(institutionId)
                .map { Optional(it) }
                .onErrorReturnItem(Optional())
    }

    fun getPolls(): Single<List<Poll>> {
        val user = requireUser()
        return api.getPolls(user.institutionId!!)
                .map { it.polls ?: emptyList() }
                .onErrorReturnItem(emptyList())
    }
}