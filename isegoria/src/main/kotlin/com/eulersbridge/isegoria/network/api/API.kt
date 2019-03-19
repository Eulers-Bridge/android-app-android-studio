package com.eulersbridge.isegoria.network.api

import com.eulersbridge.isegoria.network.api.model.*
import com.eulersbridge.isegoria.network.api.response.*
import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.http.*

interface API {

    @GET("api/user/{userEmail}")
    fun getUser(@Path("userEmail") userEmail: String): Single<User>

    @GET("api/candidate/{candidateId}")
    fun getCandidate(@Path("candidateId") candidateId: Long): Single<Candidate>

    @GET("api/candidate/{candidateId}/likes")
    fun getCandidateLikes(@Path("candidateId") candidateId: Long): Single<List<Like>>

    @PUT("api/candidate/{candidateId}/likedBy/{email}/")
    fun likeCandidate(@Path("candidateId") candidateId: Long, @Path("email") email: String): Completable

    @DELETE("api/candidate/{candidateId}/likedBy/{email}/")
    fun unlikeCandidate(@Path("candidateId") candidateId: Long, @Path("email") email: String): Completable

    @GET("api/general-info")
    fun getGeneralInfo(): Single<GeneralInfoResponse>

    @GET("https://www.isegoria.com.au/26af2fdb70869d7a57ebbd65afde108fd92a9367/institutions.json")
    fun getInstitutionURLs(): Single<List<InstitutionServer>>

    @Paginated
    @GET("api/contacts")
    fun getFriends(): Single<List<Contact>>


    @Paginated
    @GET("api/tasks/")
    fun getTasks(): Single<List<Task>>


    @GET("api/login")
    fun login(@Query("topicArn") snsTopicArn: String, @Query("deviceToken") deviceToken: String): Single<LoginResponse>

    @GET("api/logout")
    fun logOut(): Completable


    @GET("api/emailVerification/{userEmail}/resendEmail")
    fun resendVerificationEmail(@Path("userEmail") userEmail: String): Completable

    @POST("api/requestPwdReset/{userEmail}/")
    fun requestPasswordReset(@Path("userEmail") userEmail: String): Completable

    @PUT("api/user/{userEmail}")
    fun updateUser(@Path("userEmail") userEmail: String, @Body user: UserSettings): Completable

    @Paginated
    @GET("api/user/{userId}/contactRequests")
    fun getFriendRequestsSent(@Path("userId") userId: Long): Single<List<FriendRequest>>

    @Paginated
    @GET("api/user/{userId}/contactRequests/rec")
    fun getFriendRequestsReceived(@Path("userId") userId: Long): Single<List<FriendRequest>>

    @PUT("api/user/{userEmail}/PPSEQuestions")
    fun addUserEfficacy(@Path("userEmail") userEmail: String, @Body ppseQuestions: UserSelfEfficacy): Completable

    @PUT("api/user/{userEmail}/personality")
    fun addUserPersonality(@Path("userEmail") userEmail: String, @Body personality: UserPersonality): Single<PersonalityResponse>

    @GET("api/user/{userEmail}/support/")
    fun getUserSupportedTickets(@Path("userEmail") userEmail: String): Single<List<Ticket>>

    @PUT("api/user/{userEmail}/voteReminder")
    fun addVoteReminder(@Path("userEmail") userEmail: String, @Body voteReminder: VoteReminder): Completable

    @Paginated
    @GET("api/user/{userEmail}/voteReminders")
    fun getVoteReminders(@Path("userEmail") userEmail: String): Single<List<VoteReminder>>


    @GET("api/contact/{userEmail}/")
    fun getContact(@Path("userEmail") userEmail: String): Single<Contact>

    @POST("api/user/{userId}/contactRequest/{targetEmail}/")
    fun addFriend(@Path("userId") userId: Long, @Path("targetEmail") targetUserEmail: String): Completable

    @DELETE("api/contact/{targetEmail}")
    fun removeFriend(@Path("targetEmail") targetEmail: String): Completable

    @PUT("api/user/contactRequest/{contactRequestId}/accept")
    fun acceptFriendRequest(@Path("contactRequestId") contactRequestId: Long): Completable

    @PUT("api/user/contactRequest/{contactRequestId}/reject")
    fun rejectFriendRequest(@Path("contactRequestId") contactRequestId: Long): Completable

    @DELETE("api/user/contactRequest/{contactRequestId}")
    fun revokeFriendRequest(@Path("contactRequestId") contactRequestId: Long): Completable

    @GET("api/searchUserProfile/{query}/")
    fun searchForUsers(@Path("query") query: String): Single<List<User>>


    @Paginated
    @GET("api/badges/complete/{userId}")
    fun getCompletedBadges(@Path("userId") userId: Long): Single<List<Badge>>

    @Paginated
    @GET("api/badges/remaining/{userId}")
    fun getRemainingBadges(@Path("userId") userId: Long): Single<List<Badge>>

    @Paginated
    @GET("api/tasks/remaining/{userId}?pageSize=20")
    fun getRemainingTasks(@Path("userId") userId: Long): Single<List<Task>>

    @Paginated
    @GET("api/tasks/complete/{userId}?pageSize=20")
    fun getCompletedTasks(@Path("userId") userId: Long): Single<List<Task>>


    @GET("api/institution/{institutionId}")
    fun getInstitution(@Path("institutionId") institutionId: Long): Single<Institution>

    @GET("api/institution/{institutionId}/newsFeed")
    fun getInstitutionNewsFeed(@Path("institutionId") institutionId: Long): Single<NewsFeedResponse>


    @Paginated
    @GET("api/newsArticles/{institutionId}")
    fun getNewsArticles(@Path("institutionId") institutionId: Long): Single<List<NewsArticle>>

    @GET("api/newsArticle/{articleId}/likes")
    fun getNewsArticleLikes(@Path("articleId") articleId: Long): Single<List<Like>>

    @PUT("api/newsArticle/{articleId}/likedBy/{userEmail}/")
    fun likeArticle(@Path("articleId") articleId: Long, @Path("userEmail") userEmail: String): Single<LikeResponse>

    @DELETE("api/newsArticle/{articleId}/likedBy/{userEmail}/")
    fun unlikeArticle(@Path("articleId") articleId: Long, @Path("userEmail") userEmail: String): Completable


    @Paginated
    @GET("api/photoAlbums/{newsFeedId}")
    fun getPhotoAlbums(@Path("newsFeedId") newsFeedId: Long): Single<List<PhotoAlbum>>

    @GET("api/photos/{albumId}")
    fun getAlbumPhotos(@Path("albumId") albumId: Long): Single<PhotosResponse>

    @GET("api/photos/{photoId}")
    fun getPhoto(@Path("photoId") photoId: Long): Single<Photo>

    @GET("api/photos/{photoId}")
    fun getPhotos(@Path("photoId") photoId: Long): Single<PhotosResponse>

    @GET("api/photos/{userEmail}")
    fun getPhotos(@Path("userEmail") userEmail: String): Single<PhotosResponse>

    @GET("api/photo/{photoId}/likes")
    fun getPhotoLikes(@Path("photoId") photoId: Long): Single<List<Like>>

    @PUT("api/photo/{photoId}/likedBy/{userEmail}/")
    fun likePhoto(@Path("photoId") photoId: Long, @Path("userEmail") userEmail: String): Single<LikeResponse>

    @DELETE("api/photo/{photoId}/likedBy/{userEmail}/")
    fun unlikePhoto(@Path("photoId") photoId: Long, @Path("userEmail") userEmail: String): Completable


    @Paginated
    @GET("api/events/{institutionId}")
    fun getEvents(@Path("institutionId") institutionId: Long): Single<List<Event>>


    @GET("api/ticket/{ticketId}")
    fun getTicket(@Path("ticketId") ticketId: Long): Single<Ticket>

    @Paginated
    @GET("api/tickets/{electionId}")
    fun getTickets(@Path("electionId") electionId: Long): Single<List<CandidateTicket>>

    @Paginated
    @GET("api/ticket/{ticketId}/candidates")
    fun getTicketCandidates(@Path("ticketId") ticketId: Long): Single<List<Candidate>>

    @PUT("api/ticket/{ticketId}/support/{userEmail}")
    fun supportTicket(@Path("ticketId") ticketId: Long, @Path("userEmail") userEmail: String): Completable

    @DELETE("api/ticket/{ticketId}/support/{userEmail}")
    fun unsupportTicket(@Path("ticketId") ticketId: Long, @Path("userEmail") userEmail: String): Completable


    @GET("api/position/{positionId}")
    fun getPosition(@Path("positionId") positionId: Long): Single<Position>

    @Paginated
    @GET("api/positions/{electionId}")
    fun getElectionPositions(@Path("electionId") electionId: Long): Single<List<Position>>

    @Paginated
    @GET("api/position/{selectionPositionId}/candidates")
    fun getPositionCandidates(@Path("selectionPositionId") selectionPositionId: Long): Single<List<Candidate>>


    @Paginated
    @GET("api/elections/{institutionId}")
    fun getElections(@Path("institutionId") institutionId: Long): Single<List<Election>>

    @GET("api/candidates/{electionId}")
    fun getElectionCandidates(@Path("electionId") electionId: Long): Single<List<Candidate>>


    @GET("api/polls/{ownerId}")
    fun getPolls(@Path("ownerId") ownerId: Long): Single<PollsResponse>

    @PUT("api/poll/{pollId}/vote/{optionId}")
    fun answerPoll(@Path("pollId") pollId: Long, @Path("optionId") optionId: Long): Completable

    @GET("api/poll/{pollId}/results")
    fun getPollResults(@Path("pollId") pollId: Long): Single<PollResultsResponse>


    @Paginated
    @GET("api/votingLocations/{institutionId}")
    fun getVoteLocations(@Path("institutionId") institutionId: Long): Single<List<VoteLocation>>
}