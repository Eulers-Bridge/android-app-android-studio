package com.eulersbridge.isegoria.data

import android.content.Context
import android.net.Uri
import androidx.core.net.toFile
import com.amazonaws.auth.CognitoCachingCredentialsProvider
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3Client
import com.eulersbridge.isegoria.SERVER_URL_KEY
import com.eulersbridge.isegoria.SNS_PLATFORM_APPLICATION_ARN
import com.eulersbridge.isegoria.USER_EMAIL_KEY
import com.eulersbridge.isegoria.USER_PASSWORD_KEY
import com.eulersbridge.isegoria.auth.signup.SignUpUser
import com.eulersbridge.isegoria.network.AuthenticationInterceptor
import com.eulersbridge.isegoria.network.NetworkConfig
import com.eulersbridge.isegoria.network.api.API
import com.eulersbridge.isegoria.network.api.model.*
import com.eulersbridge.isegoria.network.api.response.PhotosResponse
import com.eulersbridge.isegoria.network.toCompletable
import com.eulersbridge.isegoria.util.data.Optional
import com.eulersbridge.isegoria.util.extension.addAppHeaders
import com.eulersbridge.isegoria.util.extension.edit
import com.eulersbridge.isegoria.util.extension.toBooleanSingle
import com.eulersbridge.isegoria.util.extension.toCompletable
import com.google.firebase.iid.FirebaseInstanceId
import com.securepreferences.SecurePreferences
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.subjects.BehaviorSubject
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.HttpException
import java.io.File
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataRepository @Inject constructor(
        private val appContext: Context,
        private val httpClient: OkHttpClient,
        private val api: API,
        private val networkConfig: NetworkConfig,
        private val securePreferences: SecurePreferences
) : Repository {

    @get:JvmName("_loginState")
    private val loginState = BehaviorSubject.createDefault<LoginState>(LoginState.LoggedOut())

    override fun getLoginState(): Observable<LoginState> {
        return loginState.distinctUntilChanged()
    }

    private var cachedLoginArticles: List<NewsArticle>? = null

    private fun saveUserCredentials(email: String, password: String, apiBaseUrl: String) {
        securePreferences.edit {
            putString(USER_EMAIL_KEY, email)
            putString(USER_PASSWORD_KEY, password)
            putString(SERVER_URL_KEY, apiBaseUrl)
        }
    }

    override fun getSavedEmail(): String? = securePreferences.getString(USER_EMAIL_KEY, null)
    override fun getSavedPassword(): String? = securePreferences.getString(USER_PASSWORD_KEY, null)
    override fun getSavedApiBaseUrl(): String? = securePreferences.getString(SERVER_URL_KEY, null)

    override fun setApiBaseUrl(url: String) {
        networkConfig.baseUrl = url
        securePreferences.edit { putString(SERVER_URL_KEY, url) }
    }

    private fun resetApiBaseUrl() {
        networkConfig.resetBaseUrl()
        securePreferences.edit { remove(SERVER_URL_KEY) }
    }

    @Throws(NullPointerException::class)
    private fun requireUser(): User {
        val loginStateData = loginState.value as? LoginState.LoggedIn
        return loginStateData?.user!!
    }

    @Throws(NullPointerException::class)
    override fun getUserFromLoginState(): User {
        return requireUser()
    }

    @Throws(NullPointerException::class)
    override fun getUser(): Single<User> {
        val user = requireUser()

        return api.getUser(user.email)
    }

    override fun resendVerificationEmail(email: String): Completable {
        return api.resendVerificationEmail(email)
    }

    private fun getDeviceToken(): Single<String> {
        val deviceToken = FirebaseInstanceId.getInstance().token

        return if (deviceToken == null) {
            Single.error(Exception("Failed to fetch Firebase token"))
        } else {
            Single.just(deviceToken)
        }
    }

    override fun login(email: String, password: String, apiBaseUrl: String) {
        loginState.onNext(LoginState.LoggingIn())

        setApiBaseUrl(apiBaseUrl)

        getDeviceToken()
                .doOnSuccess { AuthenticationInterceptor.setCredentials(email, password) }
                .flatMap { api.login(SNS_PLATFORM_APPLICATION_ARN, it) }
                .subscribeBy(
                        onSuccess = {
                            // Store news articles for user's institution
                            cachedLoginArticles = it.articles

                            // Populate the user's ID from the response
                            val user = it.user.copy(id = it.userId)

                            // Fetch the user's institution news feed id
                            // (still LoginState.LoggingIn() at this point)
                            api.getInstitutionNewsFeed(user.institutionId!!)
                                    .map { it.newsFeedId }
                                    .subscribeBy(
                                            onSuccess = { newsFeedId ->
                                                val updatedUser = user.copy(newsFeedId = newsFeedId)

                                                // Login process complete
                                                loginState.onNext(LoginState.LoggedIn(updatedUser))
                                            },
                                            onError = {
                                                loginState.onNext(LoginState.LoggedIn(user))
                                            }
                                    )

                            saveUserCredentials(email, password, apiBaseUrl)
                        },
                        onError = {
                            if (it is HttpException && it.code() == 401) {
                                loginState.onNext(LoginState.LoginUnauthorised())
                            } else {
                                loginState.onNext(LoginState.LoginFailure())
                            }
                        }
                )
    }

    override fun logOut(): Completable {
        cachedLoginArticles = null

        securePreferences.edit {
            remove(USER_PASSWORD_KEY)
            remove(SERVER_URL_KEY)
        }

        AuthenticationInterceptor.setCredentials(null, null)
        resetApiBaseUrl()

        loginState.onNext(LoginState.LoggedOut())

        return api.logOut()
    }

    private fun jsonObjectOf(vararg pairs: Pair<String, Any?>) = JSONObject().apply {
        for ((key, value) in pairs)
            put(key, value)
    }

    override fun signUp(user: SignUpUser): Completable {
        lateinit var jsonObject: JSONObject

        try {
            jsonObject = jsonObjectOf(
                    "email" to user.email,
                    "givenName" to user.givenName,
                    "familyName" to user.familyName,
                    "gender" to user.gender,
                    "nationality" to user.nationality,
                    "yearOfBirth" to user.yearOfBirth,
                    "accountVerified" to user.accountVerified.toString(),
                    "password" to user.password,
                    "institutionId" to user.institutionId.toString(),
                    "hasPersonality" to user.hasPersonality.toString()
            )

        } catch (e: JSONException) {
            e.printStackTrace()
            return Completable.error(e)
        }

        val json = MediaType.parse("application/json; charset=utf-8")
        val requestBody = RequestBody.create(json, jsonObject.toString())

        val request = okhttp3.Request.Builder()
                .url(networkConfig.baseUrl + "api/signUp")
                .addAppHeaders()
                .post(requestBody)
                .build()

        // Create new HTTP client rather than using application's, as no auth is required
        return httpClient.newCall(request).execute()
                .toCompletable()
    }

    private fun updateUser(updatedUser: User): Completable {
        val userSettings = UserSettings(updatedUser.trackingOff, updatedUser.isOptedOutOfDataCollection)

        return api.updateUser(updatedUser.email, userSettings).doOnComplete {
            loginState.onNext(LoginState.LoggedIn(updatedUser))
        }
    }

    override fun setUserTrackingOff(trackingOff: Boolean): Completable {
        val updatedUser = requireUser().copy(trackingOff = trackingOff)
        return updateUser(updatedUser)
    }

    override fun setUserOptedOutOfDataCollection(isOptedOutOfDataCollection: Boolean): Completable {
        val updatedUser = requireUser().copy(isOptedOutOfDataCollection = isOptedOutOfDataCollection)
        return updateUser(updatedUser)
    }

    private fun setUserSelfEfficacyCompleted(): Completable {
        // TODO: Rework – updateUser useless for this
        val updatedUser = requireUser().copy(hasPPSEQuestions = true)
        return updateUser(updatedUser)
    }

    override fun addUserSelfEfficacyAnswers(answers: UserSelfEfficacy): Completable {
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

    override fun addUserPersonality(personality: UserPersonality): Completable {
        val user = requireUser()
        return api.addUserPersonality(user.email, personality)
                .doOnSuccess {
                    setUserPersonalityCompleted()
                }
                .ignoreElement()
    }

    override fun getUserProfilePhotoUrl(): String? = requireUser().profilePhotoURL

    override fun getUserPhoto(): Single<Optional<Photo>> {
        val user = requireUser()

        return api.getPhotos(user.email)
                .map { it.photos }
                .onErrorReturnItem(emptyList())
                .map {
                    Optional(it.firstOrNull())
                }
    }

    override fun setUserPhoto(imageUri: Uri): Completable {
        return uploadNewUserPhoto(requireUser(), imageUri.toFile())
    }

    private fun getFileExtension(imageFile: File): String {
        val dotIndex = imageFile.path.lastIndexOf('.')

        return if (dotIndex > -1) {
            imageFile.path.substring(dotIndex + 1)
        } else {
            "jpg"
        }
    }

    private fun uploadNewUserPhoto(user: User, imageFile: File): Completable {
        val credentialsProvider = CognitoCachingCredentialsProvider(
                appContext, // Context,
                "715927704730",
                "us-east-1:73ae30c9-393c-44cf-a0ac-049cc0838428",
                "arn:aws:iam::715927704730:role/Cognito_isegoriaUnauth_Role",
                "arn:aws:iam::715927704730:role/Cognito_isegoriaAuth_Role",
                Regions.US_EAST_1 // Region
        )

        val s3Client = AmazonS3Client(credentialsProvider)

        val transferUtility = TransferUtility.builder()
                .s3Client(s3Client)
                .context(appContext)
                .build()

        val imageFileExtension = getFileExtension(imageFile)
        val key = "${UUID.randomUUID()}.$imageFileExtension"

        val transfer = transferUtility.upload(networkConfig.s3PicturesBucketName, key, imageFile)

        return transfer.toCompletable().doOnComplete {
            updateDisplayPicturePhoto(user, networkConfig.s3PicturesPath + key).onErrorComplete()
        }
    }

    private fun updateDisplayPicturePhoto(user: User, pictureURL: String): Completable {
        val timestamp = System.currentTimeMillis() / 1000L

        lateinit var jsonObject: JSONObject
        try {
            jsonObject = jsonObjectOf(
                    "url" to pictureURL,
                    "thumbNailUrl" to pictureURL,
                    "title" to "Profile Picture",
                    "description" to "Profile Picture",
                    "date" to timestamp.toString(),
                    "ownerId" to user.email,
                    "sequence" to "0"
            )

        } catch (e: JSONException) {
            e.printStackTrace()
            return Completable.error(e)
        }

        val json = MediaType.parse("application/json; charset=utf-8")
        val requestBody = RequestBody.create(json, jsonObject.toString())

        val request = okhttp3.Request.Builder()
                .url(networkConfig.baseUrl + "photo")
                .addAppHeaders()
                .post(requestBody)
                .build()

        return httpClient.newCall(request).execute().toCompletable()
    }

    override fun getSignUpCountries(): Single<List<Country>> {
        return api.getGeneralInfo()
                .map { it.countries }
                .onErrorReturnItem(emptyList())
    }

    override fun getTasks(): Single<List<Task>> {
        return api.getTasks().onErrorReturnItem(emptyList())
    }

    override fun getRemainingTasks(): Single<List<Task>> {
        val user = requireUser()
        return api.getRemainingTasks(user.getId()).onErrorReturnItem(emptyList())
    }

    override fun getCompletedTasks(): Single<List<Task>> {
        val user = requireUser()
        return api.getCompletedTasks(user.getId()).onErrorReturnItem(emptyList())
    }

    override fun getUserRemainingBadges(userId: Long?): Single<List<Badge>> {
        return api.getRemainingBadges(userId ?: requireUser().id!!)
                .onErrorReturnItem(emptyList())
    }

    override fun getUserCompletedBadges(userId: Long?): Single<List<Badge>> {
        return api.getCompletedBadges(userId ?: requireUser().id!!).onErrorReturnItem(emptyList())
    }

    override fun getContact(email: String): Single<Optional<Contact>> {
        return api.getContact(email)
                .map { Optional(it) }
                .onErrorReturnItem(Optional())
    }

    override fun getNewsArticles(): Single<List<NewsArticle>> {
        return cachedLoginArticles?.let {
            Single.just(it)
        } ?: api.getNewsArticles(requireUser().institutionId!!)
                .onErrorReturnItem(emptyList())
                .doOnSuccess { cachedLoginArticles = null }
    }

    override fun getNewsArticleLikes(articleId: Long): Single<List<Like>> {
        return api.getNewsArticleLikes(articleId)
                .onErrorReturnItem(emptyList())
    }

    override fun likeArticle(articleId: Long): Single<Boolean> {
        val user = requireUser()

        return api.likeArticle(articleId, user.email)
                .map { it.success }
                .onErrorReturnItem(false)
    }

    override fun unlikeArticle(articleId: Long): Single<Boolean> {
        val user = requireUser()
        return api.unlikeArticle(articleId, user.email).toBooleanSingle()
    }

    private fun getElections(): Single<List<Election>> {
        return api.getElections(requireUser().institutionId!!)
                .onErrorReturnItem(emptyList())
    }

    override fun getLatestElection(): Single<Optional<Election>> {
        return getElections()
                .map { Optional(it.firstOrNull()) }
                .onErrorReturnItem(Optional())
    }

    override fun getLatestElectionCandidates(): Single<List<Candidate>> {
        return getLatestElection().flatMap {
            it.value?.let {
                api.getElectionCandidates(it.id)

            } ?: Single.just(emptyList())
        }.onErrorReturnItem(emptyList())
    }

    override fun getCandidate(candidateId: Long): Single<Candidate> {
        return api.getCandidate(candidateId)
    }


    override fun getCandidateLikes(candidateId: Long): Single<List<Like>> {
        return api.getCandidateLikes(candidateId)
    }


    override fun likeCandidate(candidateId: Long): Completable {
        val user = requireUser()

        return api.likeCandidate(candidateId, user.email)
    }

    override fun unlikeCandidate(candidateId: Long): Completable {
        val user = requireUser()

        return api.unlikeCandidate(candidateId, user.email)
    }

    override fun getLatestElectionTickets(): Single<List<CandidateTicket>> {
        return getLatestElection().flatMap {
            it.value?.let {
                api.getTickets(it.id)

            } ?: Single.just(emptyList())
        }.onErrorReturnItem(emptyList())
    }

    override fun getLatestElectionPositions(): Single<List<Position>> {
        return getLatestElection().flatMap {
            it.value?.let {
                api.getElectionPositions(it.id)

            } ?: Single.just(emptyList())
        }.onErrorReturnItem(emptyList())
    }

    override fun getPosition(id: Long): Single<Optional<Position>> {
        return api.getPosition(id)
                .map { Optional(it) }
                .onErrorReturnItem(Optional())
    }

    override fun getPositionCandidates(positionId: Long): Single<List<Candidate>> {
        return api.getPositionCandidates(positionId)
                .onErrorReturnItem(emptyList())
    }

    override fun getTicket(id: Long): Single<Optional<Ticket>> {
        return api.getTicket(id)
                .map { Optional(it) }
                .onErrorReturnItem(Optional())
    }

    override fun getTicketCandidates(ticketId: Long): Single<List<Candidate>> {
        return api.getTicketCandidates(ticketId)
                .onErrorReturnItem(emptyList())
    }

    override fun getVoteLocations(): Single<List<VoteLocation>> {
        val user = requireUser()
        return api.getVoteLocations(user.institutionId!!).onErrorReturnItem(emptyList())
    }

    override fun createUserVoteReminder(electionId: Long, voteLocation: String, date: Long): Completable {
        val user = requireUser()
        val reminder = VoteReminder(user.email, electionId, voteLocation, date)

        return api.addVoteReminder(user.email, reminder)
    }

    override fun getUserVoteReminderExists(): Single<Boolean> {
        val user = requireUser()
        return api.getVoteReminders(user.email)
                .map { it.firstOrNull() == null }
                .onErrorReturnItem(false)
    }

    override fun getLatestUserVoteReminder(): Single<Optional<VoteReminder>> {
        val user = requireUser()
        return api.getVoteReminders(user.email).map { voteReminders ->
            Optional(voteReminders.maxBy { it.date })
        }
    }

    override fun getUserSupportedTicket(ticketId: Long): Single<Boolean> {
        val user = requireUser()
        return api.getUserSupportedTickets(user.email).map {
            it.singleOrNull { ticket ->
                ticket.id == ticketId
            } != null
        }
    }

    override fun supportTicket(ticketId: Long): Completable {
        val user = requireUser()
        return api.supportTicket(ticketId, user.email)
    }

    override fun unsupportTicket(ticketId: Long): Completable {
        val user = requireUser()
        return api.unsupportTicket(ticketId, user.email)
    }

    override fun getEvents(): Single<List<Event>> {
        val user = requireUser()
        return api.getEvents(user.institutionId!!)
                .onErrorReturnItem(emptyList())
    }

    override fun getPhotoAlbums(): Single<List<PhotoAlbum>> {
        val user = requireUser()
        return api.getPhotoAlbums(user.newsFeedId)
                .onErrorReturnItem(emptyList())
    }

    override fun getPhotoLikes(photoId: Long): Single<List<Like>> {
        return api.getPhotoLikes(photoId)
                .onErrorReturnItem(emptyList())
    }

    override fun likePhoto(photoId: Long): Single<Boolean> {
        val user = requireUser()
        return api.likePhoto(photoId, user.email)
                .map { it.success }
                .onErrorReturnItem(false)
    }

    override fun unlikePhoto(photoId: Long): Single<Boolean> {
        val user = requireUser()
        return api.unlikePhoto(photoId, user.email).toBooleanSingle()
    }

    override fun getPhoto(id: Long): Single<Optional<Photo>> {
        return api.getPhoto(id).map { Optional(it) }.onErrorReturnItem(Optional())
    }

    override fun getPhotos(id: Long): Single<PhotosResponse> {
        return api.getPhotos(id)
    }

    override fun getAlbumPhotos(photoAlbumId: Long): Single<List<Photo>> {
        return api.getAlbumPhotos(photoAlbumId)
                .map { it.photos }
                .onErrorReturnItem(emptyList())
    }

    override fun getFriends(): Single<List<Contact>> {
        return api.getFriends().onErrorReturnItem(emptyList())
    }

    override fun getSentFriendRequests(): Single<List<FriendRequest>> {
        val user = requireUser()
        return api.getFriendRequestsSent(user.getId())
                .onErrorReturnItem(emptyList())
    }

    override fun getReceivedFriendRequests(): Single<List<FriendRequest>> {
        val user = requireUser()
        return api.getFriendRequestsReceived(user.getId())
                .onErrorReturnItem(emptyList())
                .map { it.filter { it.accepted == null } }
    }

    override fun addFriend(newFriendEmail: String): Single<Boolean> {
        val user = requireUser()
        return api.addFriend(user.id!!, newFriendEmail).toBooleanSingle()
    }

    override fun removeFriend(friendEmail: String): Completable {
        return api.removeFriend(friendEmail)
    }

    override fun acceptFriendRequest(requestId: Long): Completable {
        return api.acceptFriendRequest(requestId)
    }

    override fun rejectFriendRequest(requestId: Long): Completable {
        return api.rejectFriendRequest(requestId)
    }

    override fun revokeFriendRequest(requestId: Long): Completable {
        return api.revokeFriendRequest(requestId)
    }

    /**
     * Checks if a user is either a friend or has received a pending friend request
     */
    override fun getUserAddedAsFriend(targetUserEmail: String): Single<Boolean> {
        return Single.zip(getFriends(), getSentFriendRequests(), BiFunction<List<Contact>, List<FriendRequest>, Boolean> {
            friends, sentRequests -> friends.any { friend -> friend.email == targetUserEmail }
                || sentRequests.any { sentRequest ->
            sentRequest.requestReceiver?.email == targetUserEmail
                    && sentRequest.accepted == null }
        })
    }

    override fun getFriendStatusAndPendingFriendRequest(targetUserEmail: String): Single<Pair<Boolean, FriendRequest?>> {
        return Single.zip(getFriends(), getSentFriendRequests(), BiFunction<List<Contact>, List<FriendRequest>, Pair<Boolean, FriendRequest?>> {
            friends, sentRequests ->
                 val pendingRequest = sentRequests.firstOrNull() { sentRequest ->
                     sentRequest.requestReceiver?.email == targetUserEmail
                             && sentRequest.accepted == null }

                 when {
                    (friends.any { friend -> friend.email == targetUserEmail }) ->
                        Pair(true, pendingRequest)
                     else ->
                         Pair(false, pendingRequest)
                }
            }
        )
    }

    override fun searchForUsers(query: String): Single<List<User>> {
        return api.searchForUsers(query).onErrorReturnItem(emptyList())
    }

    override fun getInstitution(institutionId: Long): Single<Optional<Institution>> {
        return api.getInstitution(institutionId)
                .map { Optional(it) }
                .onErrorReturnItem(Optional())
    }

    override fun getInstitutionName(institutionId: Long): Single<Optional<String>> {
        return getInstitution(institutionId)
                .map { Optional(it.value?.getName()) }
    }

    override fun getInstitutionServers(): Single<List<InstitutionServer>> {
        return api.getInstitutionURLs()
    }

    override fun getPolls(): Single<List<Poll>> {
        val user = requireUser()
        return api.getPolls(user.institutionId!!)
                .map { it.polls ?: emptyList() }
                .onErrorReturnItem(emptyList())
    }

    override fun answerPoll(pollId: Long, answerId: Long): Completable {
        return api.answerPoll(pollId, answerId)
    }

    override fun getPollResults(pollId: Long): Single<List<PollResult>> {
        return api.getPollResults(pollId)
                .map { it.results }
                .onErrorReturnItem(emptyList())
    }
}