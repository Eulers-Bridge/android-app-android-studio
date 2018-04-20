package com.eulersbridge.isegoria.network.api

import com.eulersbridge.isegoria.network.Paginated
import com.eulersbridge.isegoria.network.api.model.*
import com.eulersbridge.isegoria.network.api.response.*
import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.http.*

interface API {

    @GET("general-info")
    fun getGeneralInfo(): Single<GeneralInfoResponse>

    @GET("https://www.isegoria.com.au/26af2fdb70869d7a57ebbd65afde108fd92a9367/institutions.json")
    fun getInstitutionURLs(): Single<List<ClientInstitution>>

    @Paginated
    @GET("contacts")
    fun getFriends(): Single<List<Contact>>


    @Paginated
    @GET("tasks/")
    fun getTasks(): Single<List<Task>>


    @GET("login")
    fun login(@Query("topicArn") snsTopicArn: String, @Query("deviceToken") deviceToken: String): Single<LoginResponse>

    @GET("logout")
    fun logout(): Completable


    @GET("emailVerification/{userEmail}/resend")
    fun resendVerificationEmail(@Path("userEmail") userEmail: String): Completable

    @POST("requestPwdReset/{userEmail}/")
    fun requestPasswordReset(@Path("userEmail") userEmail: String): Completable

    @PUT("user/{userEmail}")
    fun updateUser(@Path("userEmail") userEmail: String, @Body user: UserSettings): Completable

    @Paginated
    @GET("user/{userId}/contactRequests")
    fun getFriendRequestsSent(@Path("userId") userId: Long): Single<List<FriendRequest>>

    @Paginated
    @GET("user/{userId}/contactRequests/rec")
    fun getFriendRequestsReceived(@Path("userId") userId: Long): Single<List<FriendRequest>>

    @PUT("user/{userEmail}/PPSEQuestions")
    fun addUserEfficacy(@Path("userEmail") userEmail: String, @Body ppseQuestions: UserSelfEfficacy): Completable

    @PUT("user/{userEmail}/personality")
    fun addUserPersonality(@Path("userEmail") userEmail: String, @Body personality: UserPersonality): Single<PersonalityResponse>

    @GET("user/{userEmail}/support/")
    fun getUserSupportedTickets(@Path("userEmail") userEmail: String): Single<List<Ticket>>

    @PUT("user/{userEmail}/voteReminder")
    fun addVoteReminder(@Path("userEmail") userEmail: String, @Body voteReminder: VoteReminder): Completable

    @Paginated
    @GET("user/{userEmail}/voteReminders")
    fun getVoteReminders(@Path("userEmail") userEmail: String): Single<List<VoteReminder>>


    @GET("contact/{userEmail}/")
    fun getContact(@Path("userEmail") userEmail: String): Single<Contact>

    @POST("user/{userEmail}/contactRequest/{targetUserEmail}")
    fun addFriend(@Path("userEmail") userEmail: String, @Path("targetUserEmail") targetUserEmail: String): Completable

    @PUT("user/contactRequest/{contactRequestId}/accept")
    fun acceptFriendRequest(@Path("contactRequestId") contactRequestId: Long): Completable

    @PUT("user/contactRequest/{contactRequestId}/reject")
    fun rejectFriendRequest(@Path("contactRequestId") contactRequestId: Long): Completable


    @GET("searchUserProfile/{query}/")
    fun searchForUsers(@Path("query") query: String): Single<List<User>>


    @Paginated
    @GET("badges/complete/{userId}")
    fun getCompletedBadges(@Path("userId") userId: Long): Single<List<Badge>>

    @Paginated
    @GET("badges/remaining/{userId}")
    fun getRemainingBadges(@Path("userId") userId: Long): Single<List<Badge>>

    @Paginated
    @GET("tasks/remaining/{userId}?pageSize=20")
    fun getRemainingTasks(@Path("userId") userId: Long): Single<List<Task>>

    @Paginated
    @GET("tasks/complete/{userId}?pageSize=20")
    fun getCompletedTasks(@Path("userId") userId: Long): Single<List<Task>>


    @GET("institution/{institutionId}")
    fun getInstitution(@Path("institutionId") institutionId: Long): Single<Institution>

    @GET("institution/{institutionId}/newsFeed")
    fun getInstitutionNewsFeed(@Path("institutionId") institutionId: Long): Single<NewsFeedResponse>


    @Paginated
    @GET("newsArticles/{institutionId}")
    fun getNewsArticles(@Path("institutionId") institutionId: Long): Single<List<NewsArticle>>

    @GET("newsArticle/{articleId}/likes")
    fun getNewsArticleLikes(@Path("articleId") articleId: Long): Single<List<Like>>

    @PUT("newsArticle/{articleId}/likedBy/{userEmail}/")
    fun likeArticle(@Path("articleId") articleId: Long, @Path("userEmail") userEmail: String): Single<LikeResponse>

    @DELETE("newsArticle/{articleId}/likedBy/{userEmail}/")
    fun unlikeArticle(@Path("articleId") articleId: Long, @Path("userEmail") userEmail: String): Completable


    @Paginated
    @GET("photoAlbums/{newsFeedId}")
    fun getPhotoAlbums(@Path("newsFeedId") newsFeedId: Long): Single<List<PhotoAlbum>>

    @GET("photos/{albumId}")
    fun getAlbumPhotos(@Path("albumId") albumId: Long): Single<PhotosResponse>

    @GET("photos/{photoId}")
    fun getPhoto(@Path("photoId") photoId: Long): Single<Photo>

    @GET("photos/{photoId}")
    fun getPhotos(@Path("photoId") photoId: Long): Single<PhotosResponse>

    @GET("photos/{userEmail}")
    fun getPhotos(@Path("userEmail") userEmail: String): Single<PhotosResponse>

    @GET("photo/{photoId}/likes")
    fun getPhotoLikes(@Path("photoId") photoId: Long): Single<List<Like>>

    @PUT("photo/{photoId}/likedBy/{userEmail}/")
    fun likePhoto(@Path("photoId") photoId: Long, @Path("userEmail") userEmail: String): Single<LikeResponse>

    @DELETE("photo/{photoId}/likedBy/{userEmail}/")
    fun unlikePhoto(@Path("photoId") photoId: Long, @Path("userEmail") userEmail: String): Completable


    @Paginated
    @GET("events/{institutionId}")
    fun getEvents(@Path("institutionId") institutionId: Long): Single<List<Event>>


    @GET("ticket/{ticketId}")
    fun getTicket(@Path("ticketId") ticketId: Long): Single<Ticket>

    @Paginated
    @GET("tickets/{electionId}")
    fun getTickets(@Path("electionId") electionId: Long): Single<List<CandidateTicket>>

    @Paginated
    @GET("ticket/{ticketId}/candidates")
    fun getTicketCandidates(@Path("ticketId") ticketId: Long): Single<List<Candidate>>

    @PUT("ticket/{ticketId}/support/{userEmail}")
    fun supportTicket(@Path("ticketId") ticketId: Long, @Path("userEmail") userEmail: String): Completable

    @DELETE("ticket/{ticketId}/support/{userEmail}")
    fun unsupportTicket(@Path("ticketId") ticketId: Long, @Path("userEmail") userEmail: String): Completable


    @GET("position/{positionId}")
    fun getPosition(@Path("positionId") positionId: Long): Single<Position>

    @Paginated
    @GET("positions/{electionId}")
    fun getElectionPositions(@Path("electionId") electionId: Long): Single<List<Position>>

    @Paginated
    @GET("position/{selectionPositionId}/candidates")
    fun getPositionCandidates(@Path("selectionPositionId") selectionPositionId: Long): Single<List<Candidate>>


    @Paginated
    @GET("elections/{institutionId}")
    fun getElections(@Path("institutionId") institutionId: Long): Single<List<Election>>

    @GET("candidates/{electionId}")
    fun getElectionCandidates(@Path("electionId") electionId: Long): Single<List<Candidate>>


    @GET("polls/{ownerId}")
    fun getPolls(@Path("ownerId") ownerId: Long): Single<PollsResponse>

    @PUT("poll/{pollId}/vote/{optionId}")
    fun answerPoll(@Path("pollId") pollId: Long, @Path("optionId") optionId: Long): Completable

    @GET("poll/{pollId}/results")
    fun getPollResults(@Path("pollId") pollId: Long): Single<PollResultsResponse>


    @Paginated
    @GET("votingLocations/{institutionId}")
    fun getVoteLocations(@Path("institutionId") institutionId: Long): Single<List<VoteLocation>>
}