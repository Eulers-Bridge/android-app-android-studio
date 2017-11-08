package com.eulersbridge.isegoria.network;

import com.eulersbridge.isegoria.models.Badge;
import com.eulersbridge.isegoria.models.Candidate;
import com.eulersbridge.isegoria.models.CandidateTicket;
import com.eulersbridge.isegoria.models.Election;
import com.eulersbridge.isegoria.models.Event;
import com.eulersbridge.isegoria.models.FriendRequest;
import com.eulersbridge.isegoria.models.Institution;
import com.eulersbridge.isegoria.models.NewsArticle;
import com.eulersbridge.isegoria.models.Photo;
import com.eulersbridge.isegoria.models.PhotoAlbum;
import com.eulersbridge.isegoria.models.Position;
import com.eulersbridge.isegoria.models.Task;
import com.eulersbridge.isegoria.models.UserPersonality;
import com.eulersbridge.isegoria.models.UserProfile;
import com.eulersbridge.isegoria.models.UserSelfEfficacy;
import com.eulersbridge.isegoria.models.Ticket;
import com.eulersbridge.isegoria.models.UserSettings;
import com.eulersbridge.isegoria.models.VoteLocation;
import com.eulersbridge.isegoria.models.VoteReminder;
import com.google.gson.JsonObject;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by Seb on 04/11/2017.
 */

public interface API {

    @GET("login")
    Call<LoginResponse> attemptLogin(@Query("topicArn") String snsTopicArn, @Query("deviceToken") String deviceToken);


    @GET("general-info")
    Call<GeneralInfoResponse> getGeneralInfo();


    @GET("emailVerification/{userEmail}/resend")
    Call<Void> sendVerificationEmail(@Path("userEmail") String userEmail);

    @PUT("user/{userEmail}")
    Call<Void> updateUserDetails(@Path("userEmail") String userEmail, @Body UserSettings user);

    @Paginated
    @GET("user/{userEmail}/contactRequests")
    Call<List<FriendRequest>> getFriendRequestsSent(@Path("userEmail") String userEmail);

    @Paginated
    @GET("user/{userEmail}/contactRequests/rec")
    Call<List<FriendRequest>> getFriendRequestsReceived(@Path("userEmail") String userEmail);

    @PUT("user/{userEmail}/PPSEQuestions")
    Call<Void> addUserEfficacy(@Path("userEmail") String userEmail, @Body UserSelfEfficacy ppseQuestions);

    @PUT("user/{userEmail}/personality")
    Call<Void> addUserPersonality(@Path("userEmail") String userEmail, @Body UserPersonality personality);

    @GET("user/{userEmail}/support/")
    Call<List<Ticket>> getUserSupportedTickets(@Path("userEmail") String userEmail);

    @PUT("user/{userEmail}/voteReminder")
    Call<Void> addVoteReminder(@Path("userEmail") String userEmail, @Body VoteReminder voteReminder);

    @Paginated
    @GET("user/{userEmail}/voteReminders")
    Call<List<VoteReminder>> getVoteReminders(@Path("userEmail") String userEmail);


    @GET("contact/{userEmail}/")
    Call<UserProfile> getUser(@Path("userEmail") String userEmail);

    @Paginated
    @GET("contacts")
    Call<List<UserProfile>> getFriends();

    @POST("user/{userEmail}/contactRequest/{targetUserEmail}")
    Call<Void> addFriend(@Path("userEmail") String userEmail, @Path("targetUserEmail") String targetUserEmail);

    @PUT("user/contactRequest/{contactRequestId}/accept")
    Call<Void> acceptFriendRequest(@Path("contactRequestId") long contactRequestId);

    @PUT("user/contactRequest/{contactRequestId}/reject")
    Call<Void> rejectFriendRequest(@Path("contactRequestId") long contactRequestId);


    @GET("searchUserProfile/{query}/")
    Call<List<UserProfile>> searchForUsers(@Path("query") String query);


    @Paginated
    @GET("badges/complete/{userId}")
    Call<List<Badge>> getCompletedBadges(@Path("userId") long userId);

    @Paginated
    @GET("badges/remaining/{userId}")
    Call<List<Badge>> getRemainingBadges(@Path("userId") long userId);


    @Paginated
    @GET("tasks/")
    Call<List<Task>> getTasks();

    @Paginated
    @GET("tasks/remaining/{userId}?pageSize=20")
    Call<List<Task>> getRemainingTasks(@Path("userId") long userId);

    @Paginated
    @GET("tasks/complete/{userId}?pageSize=20")
    Call<List<Task>> getCompletedTasks(@Path("userId") long userId);


    @GET("institution/{institutionId}")
    Call<Institution> getInstitution(@Path("institutionId") long institutionId);

    @GET("institution/{institutionId}/newsFeed")
    Call<JsonObject> getInstitutionNewsFeed(@Path("institutionId") long institutionId);


    @Paginated
    @GET("newsArticles/{institutionId}")
    Call<List<NewsArticle>> getNewsArticles(@Path("institutionId") long institutionId);

    @GET("newsArticle/{articleId}/likedBy/{userEmail}")
    Call<LikedResponse> getNewsArticleLiked(@Path("articleId") long articleId, @Path("userEmail") String userEmail);

    @PUT("newsArticle/{articleId}/likedBy/{userEmail}")
    Call<Void> likeArticle(@Path("articleId") long photoId, @Path("userEmail") String userEmail);

    @DELETE("newsArticle/{articleId}/unlikedBy/{userEmail}")
    Call<Void> unlikeArticle(@Path("articleId") long photoId, @Path("userEmail") String userEmail);


    @Paginated
    @GET("photoAlbums/{newsFeedId}")
    Call<List<PhotoAlbum>> getPhotoAlbums(@Path("newsFeedId") long newsFeedId);

    @GET("photos/{albumId}")
    Call<PhotosResponse> getAlbumPhotos(@Path("albumId") long albumId);

    @GET("photos/{photoId}")
    Call<Photo> getPhoto(@Path("photoId") long photoId);

    @GET("photos/{photoId}")
    Call<PhotosResponse> getPhotos(@Path("photoId") long photoId);

    @GET("photos/{userEmail}")
    Call<PhotosResponse> getPhotos(@Path("userEmail") String userEmail);

    @GET("photo/{photoId}/likedBy/{userEmail}")
    Call<LikedResponse> getPhotoLiked(@Path("photoId") long photoId, @Path("userEmail") String userEmail);

    @PUT("photo/{photoId}/likedBy/{userEmail}")
    Call<Void> likePhoto(@Path("photoId") long photoId, @Path("userEmail") String userEmail);

    @DELETE("photo/{photoId}/unlikedBy/{userEmail}")
    Call<Void> unlikePhoto(@Path("photoId") long photoId, @Path("userEmail") String userEmail);


    @Paginated
    @GET("events/{institutionId}")
    Call<List<Event>> getEvents(@Path("institutionId") long institutionId);


    @GET("ticket/{ticketId}")
    Call<Ticket> getTicket(@Path("ticketId") long ticketId);

    @Paginated
    @GET("tickets/{electionId}")
    Call<List<CandidateTicket>> getTickets(@Path("electionId") long electionId);

    @Paginated
    @GET("ticket/{ticketId}/candidates")
    Call<List<Candidate>> getTicketCandidates(@Path("ticketId") long ticketId);

    @PUT("ticket/{ticketId}/support/{userEmail}")
    Call<Void> supportTicket(@Path("ticketId") long ticketId, @Path("userEmail") String userEmail);

    @DELETE("ticket/{ticketId}/support/{userEmail}")
    Call<Void> unsupportTicket(@Path("ticketId") long ticketId, @Path("userEmail") String userEmail);


    @GET("position/{positionId}")
    Call<Position> getPosition(@Path("positionId") long positionId);

    @Paginated
    @GET("positions/{electionId}")
    Call<List<Position>> getElectionPositions(@Path("electionId") long electionId);

    @GET("position/{selectionPositionId}/candidates")
    Call<List<Candidate>> getPositionCandidates(@Path("selectionPositionId") long selectionPositionId);


    @Paginated
    @GET("elections/{institutionId}")
    Call<List<Election>> getElections(@Path("institutionId") long institutionId);

    @GET("candidates/{electionId}")
    Call<List<Candidate>> getElectionCandidates(@Path("electionId") long electionId);


    @GET("polls/{ownerId}")
    Call<PollsResponse> getPolls(@Path("ownerId") long ownerId);

    @PUT("poll/{pollId}/vote/{optionId}")
    Call<Void> answerPoll(@Path("pollId") long pollId, @Path("optionId") long optionId);

    @GET("poll/{pollId}/results")
    Call<PollResultsResponse> getPollResults(@Path("pollId") long pollId);


    @Paginated
    @GET("votingLocations/{institutionId}")
    Call<List<VoteLocation>> getVoteLocations(@Path("institutionId") long institutionId);
}