package com.eulersbridge.isegoria.network.api

import com.eulersbridge.isegoria.network.Paginated
import com.eulersbridge.isegoria.network.api.models.*
import com.eulersbridge.isegoria.network.api.responses.*
import retrofit2.Call
import retrofit2.http.*

interface API {

    @GET("general-info")
    fun getGeneralInfo(): Call<GeneralInfoResponse>


    // Note: Specifying a full absolute path ignores Retrofit's base API URL
    @GET("https://www.isegoria.com.au/26af2fdb70869d7a57ebbd65afde108fd92a9367/institutions.json")
    fun getInstitutionURLs(): Call<List<ClientInstitution>>

    @Paginated
    @GET("contacts")
    fun getFriends(): Call<List<Contact>>


    @Paginated
    @GET("tasks/")
    fun getTasks(): Call<List<Task>>


    @GET("login")
    fun login(@Query("topicArn") snsTopicArn: String, @Query("deviceToken") deviceToken: String): Call<LoginResponse?>

    @GET("logout")
    fun logout(): Call<Void>


    @GET("emailVerification/{userEmail}/resend")
    fun sendVerificationEmail(@Path("userEmail") userEmail: String): Call<Void>

    @POST("requestPwdReset/{userEmail}/")
    fun requestPasswordReset(@Path("userEmail") userEmail: String): Call<Void>

    @PUT("user/{userEmail}")
    fun updateUserDetails(@Path("userEmail") userEmail: String, @Body user: UserSettings): Call<Void>

    @Paginated
    @GET("user/{userId}/contactRequests")
    fun getFriendRequestsSent(@Path("userId") userId: Long): Call<List<FriendRequest>>

    @Paginated
    @GET("user/{userId}/contactRequests/rec")
    fun getFriendRequestsReceived(@Path("userId") userId: Long): Call<List<FriendRequest>>

    @PUT("user/{userEmail}/PPSEQuestions")
    fun addUserEfficacy(@Path("userEmail") userEmail: String, @Body ppseQuestions: UserSelfEfficacy): Call<Void>

    @PUT("user/{userEmail}/personality")
    fun addUserPersonality(@Path("userEmail") userEmail: String, @Body personality: UserPersonality): Call<PersonalityResponse>

    @GET("user/{userEmail}/support/")
    fun getUserSupportedTickets(@Path("userEmail") userEmail: String): Call<List<Ticket>>

    @PUT("user/{userEmail}/voteReminder")
    fun addVoteReminder(@Path("userEmail") userEmail: String, @Body voteReminder: VoteReminder): Call<Void>

    @Paginated
    @GET("user/{userEmail}/voteReminders")
    fun getVoteReminders(@Path("userEmail") userEmail: String): Call<List<VoteReminder>>


    @GET("contact/{userEmail}/")
    fun getContact(@Path("userEmail") userEmail: String): Call<Contact>

    @POST("user/{userEmail}/contactRequest/{targetUserEmail}")
    fun addFriend(@Path("userEmail") userEmail: String, @Path("targetUserEmail") targetUserEmail: String): Call<Void>

    @PUT("user/contactRequest/{contactRequestId}/accept")
    fun acceptFriendRequest(@Path("contactRequestId") contactRequestId: Long): Call<Void>

    @PUT("user/contactRequest/{contactRequestId}/reject")
    fun rejectFriendRequest(@Path("contactRequestId") contactRequestId: Long): Call<Void>


    @GET("searchUserProfile/{query}/")
    fun searchForUsers(@Path("query") query: String): Call<List<User>?>


    @Paginated
    @GET("badges/complete/{userId}")
    fun getCompletedBadges(@Path("userId") userId: Long): Call<List<Badge>?>

    @Paginated
    @GET("badges/remaining/{userId}")
    fun getRemainingBadges(@Path("userId") userId: Long): Call<List<Badge>?>

    @Paginated
    @GET("tasks/remaining/{userId}?pageSize=20")
    fun getRemainingTasks(@Path("userId") userId: Long): Call<List<Task>?>

    @Paginated
    @GET("tasks/complete/{userId}?pageSize=20")
    fun getCompletedTasks(@Path("userId") userId: Long): Call<List<Task>>


    @GET("institution/{institutionId}")
    fun getInstitution(@Path("institutionId") institutionId: Long): Call<Institution>

    @GET("institution/{institutionId}/newsFeed")
    fun getInstitutionNewsFeed(@Path("institutionId") institutionId: Long): Call<NewsFeedResponse>


    @Paginated
    @GET("newsArticles/{institutionId}")
    fun getNewsArticles(@Path("institutionId") institutionId: Long): Call<List<NewsArticle>>

    @GET("newsArticle/{articleId}/likes")
    fun getNewsArticleLikes(@Path("articleId") articleId: Long): Call<List<Like>>

    @PUT("newsArticle/{articleId}/likedBy/{userEmail}/")
    fun likeArticle(@Path("articleId") photoId: Long, @Path("userEmail") userEmail: String): Call<LikeResponse>

    @DELETE("newsArticle/{articleId}/likedBy/{userEmail}/")
    fun unlikeArticle(@Path("articleId") photoId: Long, @Path("userEmail") userEmail: String): Call<Void>


    @Paginated
    @GET("photoAlbums/{newsFeedId}")
    fun getPhotoAlbums(@Path("newsFeedId") newsFeedId: Long): Call<List<PhotoAlbum>>

    @GET("photos/{albumId}")
    fun getAlbumPhotos(@Path("albumId") albumId: Long): Call<PhotosResponse>

    @GET("photos/{photoId}")
    fun getPhoto(@Path("photoId") photoId: Long): Call<Photo>

    @GET("photos/{photoId}")
    fun getPhotos(@Path("photoId") photoId: Long): Call<PhotosResponse>

    @GET("photos/{userEmail}")
    fun getPhotos(@Path("userEmail") userEmail: String): Call<PhotosResponse>

    @GET("photo/{photoId}/likes")
    fun getPhotoLikes(@Path("photoId") photoId: Long): Call<List<Like>?>

    @PUT("photo/{photoId}/likedBy/{userEmail}/")
    fun likePhoto(@Path("photoId") photoId: Long, @Path("userEmail") userEmail: String): Call<LikeResponse>

    @DELETE("photo/{photoId}/likedBy/{userEmail}/")
    fun unlikePhoto(@Path("photoId") photoId: Long, @Path("userEmail") userEmail: String): Call<Void>


    @Paginated
    @GET("events/{institutionId}")
    fun getEvents(@Path("institutionId") institutionId: Long): Call<List<Event>?>


    @GET("ticket/{ticketId}")
    fun getTicket(@Path("ticketId") ticketId: Long): Call<Ticket>

    @Paginated
    @GET("tickets/{electionId}")
    fun getTickets(@Path("electionId") electionId: Long): Call<List<CandidateTicket>>

    @Paginated
    @GET("ticket/{ticketId}/candidates")
    fun getTicketCandidates(@Path("ticketId") ticketId: Long): Call<List<Candidate>>

    @PUT("ticket/{ticketId}/support/{userEmail}")
    fun supportTicket(@Path("ticketId") ticketId: Long, @Path("userEmail") userEmail: String): Call<Void>

    @DELETE("ticket/{ticketId}/support/{userEmail}")
    fun unsupportTicket(@Path("ticketId") ticketId: Long, @Path("userEmail") userEmail: String): Call<Void>


    @GET("position/{positionId}")
    fun getPosition(@Path("positionId") positionId: Long): Call<Position>

    @Paginated
    @GET("positions/{electionId}")
    fun getElectionPositions(@Path("electionId") electionId: Long): Call<List<Position>>

    @Paginated
    @GET("position/{selectionPositionId}/candidates")
    fun getPositionCandidates(@Path("selectionPositionId") selectionPositionId: Long): Call<List<Candidate>>


    @Paginated
    @GET("elections/{institutionId}")
    fun getElections(@Path("institutionId") institutionId: Long): Call<List<Election>>

    @GET("candidates/{electionId}")
    fun getElectionCandidates(@Path("electionId") electionId: Long): Call<List<Candidate>>


    @GET("polls/{ownerId}")
    fun getPolls(@Path("ownerId") ownerId: Long): Call<PollsResponse>

    @PUT("poll/{pollId}/vote/{optionId}")
    fun answerPoll(@Path("pollId") pollId: Long, @Path("optionId") optionId: Long): Call<Void>

    @GET("poll/{pollId}/results")
    fun getPollResults(@Path("pollId") pollId: Long): Call<PollResultsResponse>


    @Paginated
    @GET("votingLocations/{institutionId}")
    fun getVoteLocations(@Path("institutionId") institutionId: Long): Call<List<VoteLocation>?>
}