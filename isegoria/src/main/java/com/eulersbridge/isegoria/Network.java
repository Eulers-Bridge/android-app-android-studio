package com.eulersbridge.isegoria;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.android.volley.Cache;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.ImageRequest;
import com.eulersbridge.isegoria.election.CandidateAllFragment;
import com.eulersbridge.isegoria.election.CandidatePositionFragment;
import com.eulersbridge.isegoria.election.CandidatePositionsFragment;
import com.eulersbridge.isegoria.election.CandidateTicketDetailFragment;
import com.eulersbridge.isegoria.election.CandidateTicketFragment;
import com.eulersbridge.isegoria.election.ElectionOverviewFragment;
import com.eulersbridge.isegoria.feed.EventsDetailFragment;
import com.eulersbridge.isegoria.feed.NewsArticleFragment;
import com.eulersbridge.isegoria.feed.NewsFragment;
import com.eulersbridge.isegoria.feed.PhotoAlbumFragment;
import com.eulersbridge.isegoria.feed.PhotoViewFragment;
import com.eulersbridge.isegoria.feed.PhotosFragment;
import com.eulersbridge.isegoria.login.EmailVerificationFragment;
import com.eulersbridge.isegoria.login.UserSignupFragment;
import com.eulersbridge.isegoria.models.CountryInfo;
import com.eulersbridge.isegoria.models.Event;
import com.eulersbridge.isegoria.models.FriendRequest;
import com.eulersbridge.isegoria.models.PollOption;
import com.eulersbridge.isegoria.models.User;
import com.eulersbridge.isegoria.poll.PollFragment;
import com.eulersbridge.isegoria.poll.PollVoteFragment;
import com.eulersbridge.isegoria.profile.ProfileBadgesFragment;
import com.eulersbridge.isegoria.profile.ProfileFragment;
import com.eulersbridge.isegoria.profile.TaskDetailProgressFragment;
import com.eulersbridge.isegoria.utilities.TimeConverter;
import com.eulersbridge.isegoria.utilities.Utils;
import com.eulersbridge.isegoria.vote.VoteFragment;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class Network {
	private static final String SERVER_URL = "http://54.79.70.241:8080/dbInterface/api";
	private static String PICTURE_URL = "https://s3-ap-southeast-2.amazonaws.com/isegoria/";

    private static final int socketTimeout = 30000; //30 seconds

    private String password;
    private String base64EncodedCredentials;

	private final Isegoria application;
    private boolean reminderSet = false;

    private String voteReminderLocation;
    private long voteReminderDate;

    private int electionId;
    private int userDPId;

    private RequestQueue mRequestQueue;
    private ArrayList<Integer> userTickets = new ArrayList<>();

	public Network(Isegoria application) {
        this.application = application;
	}

	public Network(Isegoria application, String username, String password) {
		this.application = application;
        this.password = password;

        AuthorisedJsonObjectRequest.username = username;
        AuthorisedJsonObjectRequest.password = password;

        AuthorisedJsonArrayRequest.username = username;
        AuthorisedJsonArrayRequest.password = password;

        String formattedCredentials = String.format("%s:%s",username,password);
        base64EncodedCredentials = Base64.encodeToString(formattedCredentials.getBytes(),
                Base64.NO_WRAP);

        Cache cache = new DiskBasedCache(application.getCacheDir(), 1024 * 1024); // 1MB cap
        BasicNetwork network = new BasicNetwork(new HurlStack());

        //For detailed debug network logging, uncomment the following line:
        //VolleyLog.DEBUG = true;

        mRequestQueue = new RequestQueue(cache, network);
        mRequestQueue.start();
	}

	private void setLoggedInUser(User user) {
        application.setLoggedInUser(user);
    }

	private User getLoggedInUser() {
        return application.getLoggedInUser();
    }

    public void setTrackingOff(boolean trackingOff) {
        application.setTrackingOff(trackingOff);
    }

    public void setOptedOutOfDataCollection(boolean optedOutOfDataCollection) {
        application.setOptedOutOfDataCollection(optedOutOfDataCollection);
    }

	public void login() {
        Runnable r = () -> {
            String response = getRequest("/login");
            try {

                if (response.startsWith("HTTP Status 401 - User is disabled")) {
                    application.setVerification();

                } else {
                    JSONObject jObject = new JSONObject(response);
                    String userId = jObject.getString("userId");

                    JSONObject userObject = jObject.getJSONObject("user");

                    User user = new User(userObject);
                    user.setId(userId);
                    user.setPassword(password);

                    setLoggedInUser(user);

                    if (user.isAccountVerified()) {
                        application.setLoggedIn(true);

                        if (user.hasPersonality()) {
                            application.setFeedFragment();
                        } else {
                            application.setPersonality();
                        }

                        getLatestElection();
                        getUserDPId();

                    } else {
                        application.setVerification();
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
                application.getMainActivity().runOnUiThread(application::loginFailed);
            }
        };

        Thread t = new Thread(r);
        t.start();
    }

    void signup(final String firstName, final String lastName, final String gender, final String country, final String yearOfBirth, final String email, final String password, String confirmPassword, final String institution) {
        Runnable r = () -> {
            StringBuilder stringBuffer = new StringBuilder();
            BufferedReader bufferedReader = null;

            try {
                HttpClient httpClient = new DefaultHttpClient();
                HttpParams httpParameters = httpClient.getParams();
                HttpConnectionParams.setTcpNoDelay(httpParameters, true);
                HttpPost httpPost = new HttpPost();

                URI uri = new URI(SERVER_URL + "/signUp");
                httpPost.setURI(uri);
                httpPost.addHeader("Accept", "application/json");
                httpPost.addHeader("Content-type", "application/json");

                JSONObject signup = new JSONObject();
                String json = "";
                try {
                    signup.put("email", email);
                    signup.put("givenName", firstName);
                    signup.put("familyName", lastName);
                    signup.put("gender", gender);
                    signup.put("nationality", country);
                    signup.put("yearOfBirth",yearOfBirth);
                    signup.put("accountVerified", "false");
                    signup.put("password", password);
                    signup.put("institutionId", String.valueOf(institution));

                    json = signup.toString();

                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                StringEntity se = new StringEntity(json);
                httpPost.setEntity(se);

                HttpResponse httpResponse = httpClient.execute(httpPost);
                InputStream inputStream = httpResponse.getEntity().getContent();
                bufferedReader = new BufferedReader(new InputStreamReader(
                        inputStream));

                String readLine = bufferedReader.readLine();
                while (readLine != null) {
                    stringBuffer.append(readLine);
                    stringBuffer.append("\n");
                    readLine = bufferedReader.readLine();
                }

                if(stringBuffer.toString().contains(email)) {
                    application.getMainActivity().runOnUiThread(application::signupSucceeded);
                }
                else {
                    application.getMainActivity().runOnUiThread(application::signupFailed);
                }

            } catch (Exception e) {
                Log.e("Isegoria", "exception", e);
            } finally {
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e) {
                        Log.e("Isegoria", "exception", e);
                    }
                }
            }
       };

       Thread t = new Thread(r);
       t.start();
	}

	public void getGeneralInfo(final UserSignupFragment userSignupFragment) {
		Runnable r = () -> {
            String response = getRequest("/general-info", false);
            try {
                JSONObject jObject = new JSONObject(response);
                JSONArray jArray = jObject.getJSONArray("countrys");

                for (int i=0; i<jArray.length(); i++) {
                    JSONObject currentCountry = jArray.getJSONObject(i);
                    String country = currentCountry.getString("countryName");

                    CountryInfo countryInfo = new CountryInfo(country);
                    userSignupFragment.addCountry(countryInfo);

                    JSONArray institutionsArray = currentCountry.getJSONArray("institutions");
                    for (int j=0; j<institutionsArray.length(); j++) {
                        JSONObject currentInstitution = institutionsArray.getJSONObject(j);

                        String institutionId = currentInstitution.getString("institutionId");
                        String institution = currentInstitution.getString("institutionName");
                        countryInfo.addInstituion(institutionId, institution);
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        };

		Thread t = new Thread(r);
		t.start();
	}


	public void getNewsArticles(final NewsFragment newsFragment) {
        String url = SERVER_URL + "/newsArticles/26";

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(url,
                response -> {
                    try {
                        JSONArray jArray = response.getJSONArray("foundObjects");

                        for (int i=0; i<jArray.length(); i++) {
                            try {
                                JSONObject currentArticle = jArray.getJSONObject(i);
                                int articleId = currentArticle.getInt("articleId");
                                int institutionId = currentArticle.getInt("institutionId");
                                String title = currentArticle.getString("title");
                                String content = currentArticle.getString("content");
                                JSONArray photos = currentArticle.getJSONArray("photos");
                                String picture = photos.getJSONObject(0).getString("url");
                                picture = picture.replace("[", "").replace("]", "").replace("\"", "").replace("\\", "");
                                Bitmap bitmapPicture = null;

                                String likes = currentArticle.optString("likes");
                                long date = currentArticle.getLong("date");
                                date = TimeConverter.convertTimestampTimezone(date);
                                String creatorEmail = currentArticle.optString("creatorEmail");
                                String studentYear = "";
                                String link = null;

                                newsFragment.addNewsArticle(articleId, institutionId, title, content, picture, likes, date, creatorEmail, studentYear, link);
                            } catch (JSONException e1) {
                                e1.printStackTrace();
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> Log.e("Volley", error.toString()));

        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
	}

	public void getNewsArticle(final NewsArticleFragment newsArticleFragment, final int articleId) {
		Runnable r = () -> {
            String response = getRequest("/newsArticle/" + String.valueOf(articleId));
            try {
                JSONObject currentArticle = new JSONObject(response);

                int articleId1 = currentArticle.getInt("articleId");
                final String title = currentArticle.getString("title");
                final String likes = currentArticle.getString("likes");
                final String content = currentArticle.getString("content");
                JSONArray photos = currentArticle.getJSONArray("photos");
                String picture = photos.getJSONObject(0).getString("url");
                String email = currentArticle.getString("creatorEmail");
                picture = picture.replace("[", "").replace("]", "").replace("\"", "").replace("\\", "");

                final boolean inappropriateContent = currentArticle.getBoolean("inappropriateContent");

                String likers = currentArticle.getString("likes");
                long date = currentArticle.getLong("date");
                final long convertedDate = TimeConverter.convertTimestampTimezone(date);
                String link = null;

                final String callbackEmail = email;

                getPicture(picture, new PictureDownloadListener() {
                    @Override
                    public void onDownloadFinished(String url, @Nullable Bitmap bitmap) {
                        newsArticleFragment.populateContent(title, content, likes, convertedDate, bitmap, callbackEmail, inappropriateContent);
                        getUser(newsArticleFragment, callbackEmail);
                    }

                    @Override
                    public void onDownloadFailed(String url, VolleyError error) {
                        newsArticleFragment.populateContent(title, content, likes, convertedDate, null, callbackEmail, inappropriateContent);
                        getUser(newsArticleFragment, callbackEmail);
                    }
                });

            } catch (JSONException e) {
                e.printStackTrace();
            }
        };

		Thread t = new Thread(r);
		t.start();
	}

	private void getUser(final NewsArticleFragment newsArticleFragment, final String userEmail) {
        User loggedInUser = getLoggedInUser();
		if (userEmail.equals(loggedInUser.getEmail())) {
            newsArticleFragment.populateUserContent(loggedInUser);

        } else {
            Runnable r = () -> {
                String response = getRequest(String.format("/contact/%s/", String.valueOf(userEmail)));
                try {
                    JSONObject userObject = new JSONObject(response);

                    User user = new User(userObject);

                    JSONArray photos = userObject.optJSONArray("photos");

                    String photoURL = null;

                    if (photos != null) {
                        int photoIndex = 0;
                        for (int i = 0; i < photos.length(); i++) {
                            JSONObject currentObject = photos.getJSONObject(i);
                            String currentSeq = currentObject.getString("sequence");

                            if (currentSeq.equals("0")) {
                                photoIndex = i;
                                break;
                            }
                        }

                        photoURL = (userObject.getJSONArray("photos").getJSONObject(photoIndex).getString("url"));
                    }

                    newsArticleFragment.populateUserContent(user);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            };

            Thread t = new Thread(r);
            t.start();
        }
	}

	interface UserInfoListener {
        void onFetchSuccess(User user);
        void onFetchFailure(String userEmail, Exception e);
    }

	private void getUser(final String userEmail, final UserInfoListener callback) {
        User loggedInUser = getLoggedInUser();
        if (userEmail.equals(loggedInUser.getEmail())) {
            callback.onFetchSuccess(loggedInUser);

        } else {
            Runnable r = () -> {
                String response = getRequest(String.format("/contact/%s/", String.valueOf(userEmail)));
                try {

                    JSONObject jsonObject = new JSONObject(response);

                    User user = new User(jsonObject);

                    callback.onFetchSuccess(user);

                } catch (JSONException e) {
                    e.printStackTrace();
                    callback.onFetchFailure(userEmail, e);
                }
            };

            Thread t = new Thread(r);
            t.start();
        }
    }

    public void getUserDP(ImageView imageView, LinearLayout backgroundLinearLayout) {
        User loggedInUser = getLoggedInUser();
        this.getFirstPhoto(Integer.valueOf(loggedInUser.getId()), imageView);
        this.getFirstPhotoBlur(0, Integer.valueOf(loggedInUser.getId()), backgroundLinearLayout);
    }

    void getUserDP(int profileId, ImageView imageView, LinearLayout backgroundLinearLayout) {
        this.getFirstPhoto(profileId, imageView);
        this.getFirstPhotoBlur(0, profileId, backgroundLinearLayout);
    }

    void getFriends(final FindAddContactFragment findAddContactFragment) {
        String url = String.format("%s/contacts", SERVER_URL);

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(url, response -> {
            try {
                JSONArray jArray = response.getJSONArray("foundObjects");

                for (int i = 0; i < jArray.length(); i++) {
                    try {
                        JSONObject userObject = jArray.getJSONObject(i);

                        User user = new User(userObject);
                        findAddContactFragment.addFriend(user);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, error -> Log.e("Volley", error.toString()));

        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

	void findContacts(String query, final FindAddContactFragment findAddContactFragment) {
        findAddContactFragment.clearSearchResults();
        String url = String.format("%s/searchUserProfile/%s/", SERVER_URL, String.valueOf(query));

        AuthorisedJsonArrayRequest req = new AuthorisedJsonArrayRequest(url, jsonArray -> {
            try {
                ArrayList<User> usersFound = new ArrayList<>();

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject userObject = jsonArray.getJSONObject(i);
                    User user = new User(userObject);
                    usersFound.add(user);
                }

                findAddContactFragment.addUsers(usersFound);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> Log.e("Volley", error.toString()));

        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
	}

    public void getProfileStats(final ProfileFragment profileFragment) {
        User loggedInUser = getLoggedInUser();
        String url = String.format("%s/contact/%s", SERVER_URL, String.valueOf(loggedInUser.getEmail()));

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(url, response -> {
            try {
                JSONArray foundObjects = response.getJSONArray("foundObjects");

                int numOfContacts = 0;
                int totalTasks = 0;

                if (foundObjects.length() > 0) {
                    JSONObject currentObject = foundObjects.getJSONObject(0);

                    numOfContacts = currentObject.getInt("numOfContacts");
                    totalTasks = currentObject.getInt("totalTasks");
                }

                profileFragment.updateStats(numOfContacts, totalTasks);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }, error -> Log.e("Volley", error.toString()));


        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

    public interface RemainingBadgeCountListener {
        void onFetchSuccess(long remainingBadgeCount);
        void onFetchFailure(Exception e);
    }

    public void getRemainingBadgeCount(final RemainingBadgeCountListener callback) {
        User loggedInUser = getLoggedInUser();
        String url = String.format("%s/badges/remaining/%s", SERVER_URL, String.valueOf(loggedInUser.getId()));

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(url, response -> {
            try {
                JSONArray jArray = response.getJSONArray("foundObjects");

                callback.onFetchSuccess(jArray.length());

            } catch (Exception e) {
                e.printStackTrace();
                callback.onFetchFailure(e);
            }
        }, error -> Log.e("Volley", error.toString()));

        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

    void getDashboardStats(final ContactProfileFragment contactProfileFragment,
                                  int profileUserId) {
        String url = String.format("%s/contacts/%s", SERVER_URL, String.valueOf(profileUserId));

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(url, response -> {
            try {
                JSONArray jArray = response.getJSONArray("foundObjects");
                JSONObject currentObject = jArray.getJSONObject(0);

                int numOfContacts = currentObject.getInt("numOfContacts");
                int numOfCompBadges = currentObject.getInt("numOfCompBadges");
                int numOfCompTasks = currentObject.getInt("numOfCompTasks");
                int totalBadges = currentObject.getInt("totalBadges");
                int totalTasks = currentObject.getInt("totalTasks");

                contactProfileFragment.updateStats(numOfContacts, numOfCompBadges, numOfCompTasks,
                        totalBadges, totalTasks);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }, error -> Log.e("Volley", error.toString()));

        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

    void addFriend(String email, final FindAddContactFragment findAddContactFragment) {
        User loggedInUser = getLoggedInUser();
        String url = String.format("%s/user/%s/contactRequest/%s/", SERVER_URL, String.valueOf(loggedInUser.getId()), String.valueOf(email));

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(Request.Method.POST, url, null,
                response -> {
                    try {
                        findAddContactFragment.showAddedMessage();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }, error -> Log.e("Volley", error.toString()));


        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);

        Log.d("VolleyRequest", req.toString());
    }

    void acceptContact(String contactRequestId, final FindAddContactFragment findAddContactFragment) {
        String url = String.format("%s/user/contactRequest/%s/accept", SERVER_URL, contactRequestId);

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(Request.Method.PUT, url, null,
                response -> {
                    try {
                        findAddContactFragment.showAcceptMessage();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }, error -> Log.e("Volley", error.toString()));


        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);

        Log.d("VolleyRequest", req.toString());
    }

    void denyContact(String requestId, final FindAddContactFragment findAddContactFragment) {
        String url = String.format("%s/user/contactRequest/%s/reject", SERVER_URL, String.valueOf(requestId));

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(Request.Method.PUT, url, null,
                response -> {
                    try {
                        findAddContactFragment.showDenyMessage();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }, error -> Log.e("Volley", error.toString()));


        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);

        Log.d("VolleyRequest", req.toString());
    }

    private void getFriendRequests(final FindAddContactFragment findAddContactFragment, FriendRequest.Type friendRequestType) {
        User loggedInUser = getLoggedInUser();

        String url = String.format("%s/user/%s/contactRequests", SERVER_URL, String.valueOf(loggedInUser.getId()));

        if (friendRequestType == FriendRequest.Type.RECEIVED) {
            url += "/rec";
        }

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(url, response -> {
            try {
                JSONArray foundObjects = response.optJSONArray("foundObjects");

                if (foundObjects != null) {
                    for (int i = 0; i < foundObjects.length(); i++) {
                        JSONObject foundObject = foundObjects.getJSONObject(i);

                        FriendRequest friendRequest = new FriendRequest(foundObject, friendRequestType);

                        if (!friendRequest.isAccepted() && !friendRequest.isRejected()) {
                            findAddContactFragment.addFriendRequest(friendRequest);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, error -> Log.e("Volley", error.toString()));

        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

    void getFriendRequestsSent(final FindAddContactFragment findAddContactFragment) {
	    getFriendRequests(findAddContactFragment, FriendRequest.Type.SENT);
    }

    void getFriendRequestsReceived(final FindAddContactFragment findAddContactFragment) {
        getFriendRequests(findAddContactFragment, FriendRequest.Type.RECEIVED);
    }

    public void addVoteReminder(String location, long date) {
        User loggedInUser = getLoggedInUser();
        String url = String.format("%s/user/%s/voteReminder", SERVER_URL, String.valueOf(loggedInUser.getEmail()));

        HashMap<String, String> params = new HashMap<>();
        params.put("userEmail", String.valueOf(loggedInUser.getEmail()));
        params.put("electionId", String.valueOf(electionId));
        params.put("location", location);
        params.put("date", String.valueOf(date));

        setVoteReminderDate(date);
        setVoteReminderLocation(location);

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(Request.Method.PUT, url, new JSONObject(params),
                response -> {

                }, error -> Log.e("Volley", error.toString()));


        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);

        Log.d("VolleyRequest", req.toString());
    }

    public interface FetchEventsListener {
        void onFetchSuccess(ArrayList<Event> events);
        void onFetchFailure(VolleyError error);
    }

	public void getEvents(final FetchEventsListener callback) {
        String url = String.format("%s/events/26/", SERVER_URL);

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(url,
                jObject -> {
                    try {
                        JSONArray eventsList = jObject.getJSONArray("foundObjects");

                        ArrayList<Event> events = new ArrayList<>();

                        for (int i=0; i < eventsList.length(); i++) {
                            JSONObject eventItem = eventsList.getJSONObject(i);

                            Event event = new Event(eventItem);
                            events.add(event);
                        }

                        callback.onFetchSuccess(events);

                    } catch (Exception e) {
                        e.printStackTrace();
                        callback.onFetchFailure(null);
                    }
                }, error -> {
                    Log.d("Volley", error.toString());
                    callback.onFetchFailure(error);
                });


        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
	}

    public void verifyEmail(final EmailVerificationFragment emailVerificationFragment) {
        String url = String.format("%s/emailVerification/%s/resend", SERVER_URL, String.valueOf(getLoggedInUser().getEmail()));

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(url,
                jObject -> {
                    String test = ":)";
                }, error -> Log.e("Volley", error.toString()));


        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

    private void findContact(final String email, final EventsDetailFragment eventsDetailFragment) {
        String url = SERVER_URL + "/contact/" + String.valueOf(email) + "/";

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(url,
                jObject -> {

                }, error -> {
                    Log.d("Volley", error.toString());
                    eventsDetailFragment.addCandidate(email);
                });


        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

    void findContactPhoto(final String email, final ImageView imageView) {
        String url = String.format("%s/contact/%s/", SERVER_URL, String.valueOf(email));

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(url,
                jObject -> {
                    try {
                        JSONObject photo = jObject.getJSONObject("profilePhoto");

                        String url1 = photo.getString("url");
                        getFirstPhotoImage(url1, imageView);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }, error -> Log.e("Volley", error.toString()));


        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

	public void getPhotoAlbums(final PhotosFragment photosFragment) {
        String url = String.format("%s/photoAlbums/7449", SERVER_URL);

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(url,
                jObject -> {
                    try {
                        JSONArray jArray = jObject.getJSONArray("foundObjects");

                        for (int i=0; i<jArray.length(); i++) {
                            JSONObject currentAlbum = jArray.getJSONObject(i);

                            int nodeId = currentAlbum.getInt("nodeId");
                            String name = currentAlbum.getString("name");
                            String description = currentAlbum.getString("description");
                            String thumbNailUrl = currentAlbum.getString("thumbNailUrl");

                            photosFragment.addPhotoAlbum(nodeId, name, description, thumbNailUrl);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> Log.e("Volley", error.toString()));


        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
	}

	public void getPhotoAlbum(final PhotoAlbumFragment photoAlbumFragment, final String albumId) {
		String url = String.format("%s/photos/%s?pageSize=100", SERVER_URL, String.valueOf(albumId));

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(url,
                jObject -> {
                    try {
                        JSONArray jArray = jObject.getJSONArray("photos");

                        for (int i=0; i<jArray.length(); i++) {
                            JSONObject currentAlbum = jArray.getJSONObject(i);

                            int nodeId = currentAlbum.getInt("nodeId");
                            String title = currentAlbum.getString("title");
                            String description = currentAlbum.getString("description");
                            String thumbNailUrl = currentAlbum.getString("url");

                            photoAlbumFragment.addPhotoThumb(thumbNailUrl, nodeId);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> Log.e("Volley", error.toString()));


        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
	}

    public void getProfileBadgesComplete(final ProfileBadgesFragment profileBadgesFragment, final String targetName, final int targetLevel) {
        String url = String.format("%s/badges/complete/%s", SERVER_URL, String.valueOf(getLoggedInUser().getId()));

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(url, response -> {
                    try {
                        JSONArray jArray = response.getJSONArray("foundObjects");
                        for (int i=0; i<jArray.length(); i++) {
                            JSONObject currentBadge = jArray.getJSONObject(i);
                            int badgeId = currentBadge.getInt("badgeId");
                            String name = currentBadge.getString("name");
                            String description = currentBadge.getString("description");

                            int maxLevel = 0;
                            //Get the max level for each badge
                            for (int j=0; j<jArray.length(); j++) {
                                JSONObject currentBadgeLevel = jArray.getJSONObject(j);
                                String nameLevel = currentBadgeLevel.getString("name");

                                if(name.equals(nameLevel)) {
                                    if (!currentBadgeLevel.isNull("level")) {
                                        if (currentBadgeLevel.getInt("level") > maxLevel) {
                                            maxLevel = currentBadgeLevel.getInt("level");
                                        }
                                    }
                                }
                            }

                            if(currentBadge.isNull("level") && targetName.equals("")) {
                                profileBadgesFragment.addBadgeComplete(badgeId, name, description,
                                        maxLevel);
                            }
                            else if(targetName.equals(name) && !currentBadge.isNull("level")) {
                                int level = currentBadge.getInt("level");
                                if(level == targetLevel) {
                                    profileBadgesFragment.addBadgeComplete(badgeId, name, description,
                                            maxLevel);
                                }
                            }
                        }

                        Network.this.getProfileBadgesRemaining(profileBadgesFragment, targetName, targetLevel);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> Log.e("Volley", error.toString()));


        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

    private void getProfileBadgesRemaining(final ProfileBadgesFragment profileBadgesFragment, final String targetName, final int targetLevel) {
        String url = String.format("%s/badges/remaining/%s", SERVER_URL, String.valueOf(getLoggedInUser().getId()));

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(url, response -> {
            try {
                JSONArray jArray = response.getJSONArray("foundObjects");
                for (int i=0; i<jArray.length(); i++) {
                    JSONObject currentBadge = jArray.getJSONObject(i);
                    int badgeId = currentBadge.getInt("badgeId");
                    String name = currentBadge.getString("name");
                    String description = currentBadge.getString("description");

                    int maxLevel = 0;
                    //Get the max level for each badge
                    for (int j=0; j<jArray.length(); j++) {
                        JSONObject currentBadgeLevel = jArray.getJSONObject(j);
                        String nameLevel = currentBadgeLevel.getString("name");

                        if(name.equals(nameLevel)) {
                            if (!currentBadgeLevel.isNull("level")) {
                                if (currentBadgeLevel.getInt("level") > maxLevel) {
                                    maxLevel = currentBadgeLevel.getInt("level");
                                }
                            }
                        }
                    }

                    if(currentBadge.isNull("level") && targetName.equals("")) {
                        profileBadgesFragment.addBadgeRemaining(badgeId, name, description,
                                maxLevel);
                    }
                    else if(targetName.equals(name) && !currentBadge.isNull("level")) {
                        int level = currentBadge.getInt("level");
                        if(level == targetLevel) {
                            profileBadgesFragment.addBadgeRemaining(badgeId, name, description,
                                    maxLevel);
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> Log.e("Volley", error.toString()));


        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

	public void getPhoto(final PhotoViewFragment photoViewFragment, final int photoId) {
		String url = String.format("%s/photo/%s", SERVER_URL, String.valueOf(photoId));

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(url,
                response -> {
                    try {
                        int nodeId = response.getInt("nodeId");
                        String title = response.getString("title");
                        String description = response.getString("description");
                        long date = response.getLong("date");
                        date = TimeConverter.convertTimestampTimezone(date);
                        String url1 = response.getString("url");
                        boolean inappropriateContent = response.getBoolean("inappropriateContent");
                        int numOfLikes = response.getInt("numOfLikes");

                        photoViewFragment.setData(title, date, inappropriateContent,
                                numOfLikes);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> Log.e("Volley", error.toString()));


        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
	}

    public void getNewsArticleLiked(final NewsArticleFragment newsArticleFragment) {
        String url = SERVER_URL + "/newsArticle/"
                + String.valueOf(newsArticleFragment.getArticleId()) + "/likedBy/" + String.valueOf(getLoggedInUser().getEmail()) + "/";

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(Request.Method.GET, url, null, response -> {
            try {
                boolean success = response.getBoolean("success");

                if(success) {
                    newsArticleFragment.initiallyLiked();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, error -> Log.e("Volley", error.toString()));


        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

	public void getVoteRecords(final VoteFragment voteFragment) {
		Runnable r = () -> {
            String response = getRequest("/voteRecord");
            try {
                JSONObject jObject = new JSONObject(response);
                JSONArray jArray = jObject.getJSONArray("photoAlbums");

                for (int i=0; i<jArray.length(); i++) {
                    JSONObject currentVoteRecord = jArray.getJSONObject(i);

                    int nodeId = currentVoteRecord.getInt("nodeId");
                    String name = currentVoteRecord.getString("name");
                    String description = currentVoteRecord.getString("description");
                    //String responseBitmap = getRequest("photos/" + String.valueOf(nodeId));
                    //JSONObject responseJSON = new JSONObject(responseBitmap);
                    //String pictureURL = responseJSON.getString("url");

                    //Bitmap bitmapPicture;
                    //bitmapPicture = getPicture();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        };

		Thread t = new Thread(r);
		t.start();
	}

	public void getPollQuestions(final PollFragment pollFragment) {
		String url = String.format("%s/polls/26", SERVER_URL);

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(url,
                response -> {

                    try {
                        JSONArray polls = response.getJSONArray("polls");

                        for (int i=0; i < polls.length(); i++) {
                            JSONObject jsonObject = polls.getJSONObject(i);

                            final int nodeId = jsonObject.getInt("nodeId");
                            String creatorEmail = jsonObject.getString("creatorEmail");
                            final String question = jsonObject.getString("question");
                            final String answers = jsonObject.optString("answers");

                            JSONArray pollOptionsJson = jsonObject.getJSONArray("pollOptions");

                            final ArrayList<PollOption> options = new ArrayList<>();

                            for (int j = 0; j < pollOptionsJson.length(); j++) {
                                JSONObject optionObject = pollOptionsJson.getJSONObject(j);
                                PollOption option = new PollOption(optionObject);
                                options.add(option);
                            }

                            if (!String.valueOf(creatorEmail).equals("null")) {
                                getUser(creatorEmail, new UserInfoListener() {
                                    @Override
                                    public void onFetchSuccess(User user) {
                                        pollFragment.addQuestion(nodeId, user, question,
                                                options);
                                    }

                                    @Override
                                    public void onFetchFailure(String userEmail, Exception e) {
                                        pollFragment.addQuestion(nodeId, null, question,
                                                options);
                                    }
                                });

                            } else {
                                pollFragment.addQuestion(nodeId, null, question,
                                        options);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> Log.e("Volley", error.toString()));


        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
	}

    private void getPollResults(final int nodeId, final PollVoteFragment pollVoteFragment) {
        String url = SERVER_URL + "/poll/" + String.valueOf(nodeId) + "/results";

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(url,
                response -> {
                    try {
                        JSONArray jArray = response.getJSONArray("answers");

                        for (int i=0; i<jArray.length(); i++) {
                            JSONObject currentPollAnswers = jArray.getJSONObject(i);
                            int count = currentPollAnswers.getInt("count");

                            pollVoteFragment.setPollResult(i, count);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> Log.e("Volley", error.toString()));


        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

    public void answerEfficacy(float q1, float q2, float q3, float q4) {
        User loggedInUser = getLoggedInUser();
        String url = String.format("%s/user/%s/PPSEQuestions", SERVER_URL, String.valueOf(loggedInUser.getId()));

        HashMap<String, Float> params = new HashMap<>();
        params.put("q1", q1);
        params.put("q2", q2);
        params.put("q3", q3);
        params.put("q4", q4);

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(Request.Method.PUT, url, new JSONObject(params),
                response -> {
                    try {
                        //TODO: Redirect the user to the correct location
                        application.setFeedFragment();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }, error -> Log.e("Volley", error.toString()));


        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

    public void answerPersonality(float extroversion, float agreeableness, float conscientiousness,
                                  float emotionalStability, float openness) {
        User loggedInUser = getLoggedInUser();
        String url = String.format("%s/user/%s/personality", SERVER_URL, String.valueOf(loggedInUser.getId()));

        HashMap<String, Float> params = new HashMap<>();
        params.put("extroversion", extroversion);
        params.put("agreeableness", agreeableness);
        params.put("conscientiousness", conscientiousness);
        params.put("emotionalStability", emotionalStability);
        params.put("openess", openness);

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(Request.Method.PUT, url, new JSONObject(params),
                response -> {
                    try {
                        application.setFeedFragment();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }, error -> Log.e("Volley", error.toString()));


        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);

        Log.d("VolleyRequest", req.toString());
    }

    public void answerPoll(int pollId, int answerIndex, final PollVoteFragment pollVoteFragment) {
        String url = String.format("%s/poll/%s/vote/%s", SERVER_URL, String.valueOf(pollId), String.valueOf(answerIndex));

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(Request.Method.PUT, url, null,
                response -> {
                    try {
                        getPollResults(pollVoteFragment.getNodeId(), pollVoteFragment);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }, error -> Log.e("Volley", error.toString()));


        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);

        Log.d("VolleyRequest", req.toString());
    }

    public void getVoteLocations(final VoteFragment voteFragment) {

        String url = String.format("%s/votingLocations/26", SERVER_URL);
        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(url, response -> {
            try {
               JSONArray jArray = response.getJSONArray("foundObjects");
               for(int i=0; i<jArray.length(); i++) {
                   String ownerId = jArray.getJSONObject(i).getString("ownerId");
                   String votingLocationId = jArray.getJSONObject(i).getString("votingLocationId");
                   String name = jArray.getJSONObject(i).getString("name");
                   String information = jArray.getJSONObject(i).getString("information");

                   voteFragment.addVoteLocations(ownerId, votingLocationId,
                           name, information);
                }

                voteFragment.showAll();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, error -> Log.e("Volley", error.toString()));


        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

    public void getVoteLocation(final VoteFragment voteFragment, final String pos) {

        String url = String.format("%s/votingLocation/%s", SERVER_URL, String.valueOf(pos));
        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(url, response -> {
            try {
                for(int i=0; i<response.length(); i++) {

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, error -> Log.e("Volley", error.toString()));


        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

    private void getLatestElection() {
        String url = String.format("%s/elections/26", SERVER_URL);

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(url, response -> {
            try {
                JSONArray foundObject = response.getJSONArray("foundObjects");
                JSONObject electionObject = foundObject.getJSONObject(0);

                Network.this.electionId = electionObject.getInt("electionId");

                alreadySetVoteReminder();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, error -> Log.e("Volley", error.toString()));


        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

    public void getLatestElection(final ElectionOverviewFragment electionOverviewFragment) {
        String url = String.format("%s/elections/26", SERVER_URL);

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(url, response -> {
            try {
                JSONArray jArray = response.getJSONArray("foundObjects");
                JSONObject electionObject = jArray.getJSONObject(0);

                int electionId = electionObject.getInt("electionId");
                Network.this.electionId = electionId;

                String title = electionObject.getString("title");
                String introduction = electionObject.getString("introduction");
                String date = "";
                String process = electionObject.getString("process");

                electionOverviewFragment.updateEntities(electionId, title, introduction, date, process);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }, error -> Log.e("Volley", error.toString()));

        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

    public void getUserSupportedTickets() {
        User loggedInUser = getLoggedInUser();
        String url = String.format("%s/user/%s/support/", SERVER_URL, String.valueOf(loggedInUser.getId()));

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(url, response -> {
            try {
                JSONArray jArray = response.getJSONArray("foundObjects");
                userTickets.clear();
                for(int i=0; i<jArray.length(); i++) {
                    JSONObject currentObject = jArray.getJSONObject(i);

                    int ticketId = currentObject.getInt("ticketId");
                    userTickets.add(ticketId);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, error -> Log.e("Volley", error.toString()));


        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

    public void getLatestElection(final VoteFragment voteFragment) {
        String url = SERVER_URL + "/elections/26";

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(url, response -> {
            try {
                JSONArray jArray = response.getJSONArray("foundObjects");
                JSONObject electionObject = jArray.getJSONObject(0);

                Network.this.electionId = electionObject.getInt("electionId");

                long startVoting = electionObject.getLong("startVoting");
                long endVoting = electionObject.getLong("endVoting");

                voteFragment.datePicker.setMinDate(startVoting);
                voteFragment.datePicker.setMaxDate(endVoting);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }, error -> Log.e("Volley", error.toString()));


        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

    public void getCandidates(final CandidatePositionsFragment candidatePositionsFragment) {
        String url = SERVER_URL + "/candidates/26";

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(url, response -> {
            try {
                JSONArray jArray = response.getJSONArray("foundObjects");
                JSONObject candidateObject = jArray.getJSONObject(0);


            } catch (Exception e) {
                e.printStackTrace();
            }
        }, error -> Log.e("Volley", error.toString()));


        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

    public void getCandidates(final CandidateAllFragment candidateAllFragment) {
        String url = SERVER_URL + "/candidates/" + String.valueOf(electionId);

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(url, response -> {
            try {
                JSONArray jArray = response.getJSONArray("foundObjects");
                for(int i=0; i<jArray.length(); i++) {
                    JSONObject candidateObject = jArray.getJSONObject(i);

                    int candidateId = candidateObject.getInt("candidateId");
                    int ticketId = candidateObject.getInt("ticketId");
                    int positionId = candidateObject.getInt("positionId");
                    int userId = candidateObject.getInt("userId");
                    String familyName = candidateObject.getString("familyName");
                    String givenName = candidateObject.getString("givenName");
                    String policyStatement = candidateObject.getString("policyStatement");
                    String information = candidateObject.getString("information");

                    candidateAllFragment.addCandidate(userId, ticketId, positionId, candidateId,
                            givenName, familyName);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }, error -> Log.e("Volley", error.toString()));


        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

    public void getCandidatesPosition(final CandidatePositionFragment candidatePositionFragment, final int selectedPositionId) {
        String url = SERVER_URL + "/position/" + String.valueOf(selectedPositionId) + "/candidates";

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(url, response -> {
            try {
                JSONArray jArray = response.getJSONArray("foundObjects");
                for(int i=0; i<jArray.length(); i++) {
                    JSONObject candidateObject = jArray.getJSONObject(i);

                    int candidateId = candidateObject.getInt("candidateId");
                    int ticketId = candidateObject.getInt("ticketId");
                    int positionId = candidateObject.getInt("positionId");
                    int userId = candidateObject.getInt("userId");
                    String familyName = candidateObject.getString("familyName");
                    String givenName = candidateObject.getString("givenName");
                    String policyStatement = candidateObject.getString("policyStatement");
                    String information = candidateObject.getString("information");

                    candidatePositionFragment.addCandidate(userId, ticketId, positionId, candidateId,
                            givenName, familyName);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }, error -> Log.e("Volley", error.toString()));


        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

    public void getTicketLabel(final TextView textView, final int tickedId) {
        String url = SERVER_URL + "/ticket/" + String.valueOf(tickedId);

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(url, response -> {
            try {
                String colour = response.getString("colour");
                String code = response.getString("code");

                textView.setText(code);
                textView.setBackgroundColor(Color.parseColor(colour));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, error -> Log.e("Volley", error.toString()));


        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

    public void getTickets(final CandidateTicketFragment candidateTicketFragment) {
        String url = SERVER_URL + "/tickets/" + String.valueOf(electionId);

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(url, response -> {
            try {
                JSONArray jArray = response.getJSONArray("foundObjects");
                for(int i=0; i<jArray.length(); i++) {
                    JSONObject ticketObject = jArray.getJSONObject(i);

                    int ticketId = ticketObject.getInt("ticketId");
                    String name = ticketObject.getString("name");
                    String numberOfSupporters = ticketObject.getString("numberOfSupporters");
                    String information = ticketObject.getString("information");
                    String logo = ticketObject.getString("logo");
                    String colour;

                    if(ticketObject.isNull("colour")) {
                        colour = "#000000";
                    }
                    else {
                        colour = ticketObject.getString("colour");
                    }

                    candidateTicketFragment.addTicket(ticketId, name, information,
                            numberOfSupporters, colour, logo, jArray.length());
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }, error -> Log.e("Volley", error.toString()));


        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

    public void getTicketDetail(int ticketId, final CandidateTicketDetailFragment candidateTicketDetailFragment) {
        String url = SERVER_URL + "/ticket/" + String.valueOf(ticketId) + "/candidates";

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(url, response -> {
            try {
                JSONArray jArray = response.getJSONArray("foundObjects");

                for(int i=0; i<jArray.length(); i++) {
                    JSONObject currentObject = jArray.getJSONObject(i);

                    int ticketId1 = currentObject.getInt("ticketId");
                    String givenName = currentObject.getString("givenName");
                    String familyName = currentObject.getString("familyName");
                    String name = givenName + " " + familyName;
                    String code = "";
                    String colour = "#000000";
                    String information = "";
                    String logo = "";

                    candidateTicketDetailFragment.updateInformation(ticketId1,
                            Network.this.electionId, name, code, information, colour);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, error -> Log.e("Volley", error.toString()));


        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

    public void getTicketCandidates(final CandidateTicketDetailFragment candidateTicketDetailFragment, final int selectedTickedId) {
        String url = SERVER_URL + "/ticket/" + String.valueOf(selectedTickedId) + "/candidates";

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(url, response -> {
            try {
                JSONArray jArray = response.getJSONArray("foundObjects");
                for(int i=0; i<jArray.length(); i++) {
                    JSONObject candidateObject = jArray.getJSONObject(i);

                    int candidateId = candidateObject.getInt("candidateId");
                    int ticketId = candidateObject.getInt("ticketId");
                    if(selectedTickedId != ticketId) {
                        continue;
                    }

                    int positionId = candidateObject.getInt("positionId");
                    int userId = candidateObject.getInt("userId");
                    String familyName = candidateObject.getString("familyName");
                    String givenName = candidateObject.getString("givenName");
                    String policyStatement = candidateObject.getString("policyStatement");
                    String information = candidateObject.getString("information");

                    candidateTicketDetailFragment.addCandidate(userId, ticketId, positionId, candidateId,
                            givenName, familyName);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }, error -> Log.e("Volley", error.toString()));


        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

    public void getPositionText(final TextView positionTextView, int positionId) {
        String url = SERVER_URL + "/position/" + String.valueOf(positionId);

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(url,
                response -> {
                    try {
                        String name = response.getString("name");
                        String description = response.getString("description");

                        positionTextView.setText(name);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> Log.e("Volley", error.toString()));


        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

    public void getPositions(final CandidatePositionsFragment candidatePositionsFragment) {
        String url = SERVER_URL + "/positions/" + String.valueOf(electionId);

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(url, response -> {
            try {
                JSONArray jArray = response.getJSONArray("foundObjects");

                for(int i=0; i<jArray.length(); i++) {
                    JSONObject positionObject = jArray.getJSONObject(i);

                    int electionId = positionObject.optInt("electionId", 0);
                    int positionId = positionObject.getInt("positionId");
                    String name = positionObject.getString("name");
                    String desc = positionObject.getString("description");

                    candidatePositionsFragment.addPosition(electionId, positionId,
                            name, desc, jArray.length());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, error -> Log.e("Volley", error.toString()));

        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

    void addVoteReminder(long date, String location) {
        User loggedInUser = getLoggedInUser();
        String url = SERVER_URL + "/user/" + String.valueOf(loggedInUser.getEmail()) + "/voteReminder";

        HashMap<String, String> params = new HashMap<>();
        params.put("userEmail", String.valueOf(loggedInUser.getEmail()));
        params.put("location", location);
        params.put("date", String.valueOf(date));
        params.put("electionId", String.valueOf(Network.this.electionId));

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(Request.Method.PUT, url, new JSONObject(params),
                response -> {
                    try {
                        //If we land in here the vote was submitted successfully
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }, error -> Log.e("Volley", error.toString()));


        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);

        Log.d("VolleyRequest", req.toString());
    }

    private void alreadySetVoteReminder() {
        User loggedInUser = getLoggedInUser();
        String url = SERVER_URL + "/user/" + String.valueOf(loggedInUser.getId()) + "/voteReminders/";
        HashMap<String, String> params = new HashMap<>();

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(url, response -> {
            try {
                JSONArray foundObjects = response.getJSONArray("foundObjects");
                for(int i=0; i<foundObjects.length(); i++) {
                    JSONObject voteReminder = foundObjects.getJSONObject(i);

                    int voteElectionId = voteReminder.getInt("electionId");
                    long timestamp = voteReminder.getLong("timestamp");
                    String userEmail = voteReminder.getString("userEmail");
                    String location = voteReminder.getString("location");
                    long date = voteReminder.getLong("date");

                    if(voteElectionId == electionId) {
                        reminderSet = true;
                        voteReminderLocation = location;
                        voteReminderDate = date;
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }, error -> Log.e("Volley", error.toString()));


        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

    public void supportTicket(int ticketId, CandidateTicketDetailFragment candidateTicketDetailFragment) {
        User loggedInUser = getLoggedInUser();
        String url = SERVER_URL + "/ticket/" + String.valueOf(ticketId) + "/support/" + String.valueOf(loggedInUser.getEmail()) + "/";
        HashMap<String, String> params = new HashMap<>();
        userTickets.add(ticketId);

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(Request.Method.PUT, url, new JSONObject(params),
                response -> {
                    try {
                        //If we land in here the vote was submitted successfully
                        String test = "";

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }, error -> Log.e("Volley", error.toString()));

        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);

        Log.d("VolleyRequest", req.toString());
    }

    public void unsupportTicket(int ticketId, CandidateTicketDetailFragment candidateTicketDetailFragment) {
        User loggedInUser = getLoggedInUser();
        String url = SERVER_URL + "/ticket/" + String.valueOf(ticketId) + "/support/" + String.valueOf(loggedInUser.getEmail()) + "/";
        HashMap<String, String> params = new HashMap<>();
        userTickets.remove(Integer.valueOf(ticketId));

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(Request.Method.DELETE, url, new JSONObject(params),
                response -> {
                    try {
                        //If we land in here the vote was submitted successfully
                        String test = "";

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }, error -> Log.e("Volley", error.toString()));

        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);

        Log.d("VolleyRequest", req.toString());
    }

    public void getPhotoLiked(final PhotoViewFragment photoViewFragment) {
        User loggedInUser = getLoggedInUser();
        String url = String.format("%s/photo/%s/likedBy/%s/", SERVER_URL, String.valueOf(photoViewFragment.getPhotoPath()),
                String.valueOf(loggedInUser.getEmail()));

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(Request.Method.GET, url, null, response -> {
            try {
                boolean success = response.getBoolean("success");

                if(success) {
                    photoViewFragment.initiallyLiked();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, error -> Log.e("Volley", error.toString()));

        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

    private void toggleContentLike(boolean like, String contentType, int contentId) {
        User loggedInUser = getLoggedInUser();
        String url = String.format("%s/%s/%s/%s/%s/",
                SERVER_URL,
                contentType,
                String.valueOf(contentId),
                like? "likedBy" : "unlikedBy",
                String.valueOf(loggedInUser.getEmail()));

        int requestMethod = like? Request.Method.PUT : Request.Method.DELETE;

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(requestMethod, url, null,
                response -> {
                    try {
                        //If we land in here the vote was submitted successfully

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }, error -> Log.e("Volley", error.toString()));


        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);

        Log.d("VolleyRequest", req.toString());
    }

    public void likePhoto(int photoId) {
	    toggleContentLike(true, "photo", photoId);
    }

    public void unlikePhoto(int photoId) {
        toggleContentLike(false, "photo", photoId);
    }

    public void likeArticle(int articleId) {
        toggleContentLike(true, "newsArticle", articleId);
    }

    public void unlikeArticle(int articleId) {
        toggleContentLike(false, "newsArticle", articleId);
    }

    public void getTasks(final ProfileFragment profileFragment) {
        String url = SERVER_URL + "/tasks/";

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(url, response -> {
            try {
                JSONArray jArray = response.getJSONArray("foundObjects");
                for(int i=0; i<jArray.length(); i++) {
                    JSONObject taskObject = jArray.getJSONObject(i);

                    long taskId = taskObject.getLong("taskId");
                    String action = taskObject.getString("action");
                    long xpValue = taskObject.getLong("xpValue");

                    profileFragment.addTask(taskId, action, xpValue);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }, error -> Log.e("Volley", error.toString()));


        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

    void getTasks(final ContactProfileFragment contactProfileFragment) {
        String url = SERVER_URL + "/tasks/complete";

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(url, response -> {
            try {
                JSONArray jArray = response.getJSONArray("foundObjects");

                for(int i=0; i<jArray.length(); i++) {
                    JSONObject taskObject = jArray.getJSONObject(i);

                    long taskId = taskObject.getLong("taskId");
                    String action = taskObject.getString("action");
                    long xpValue = taskObject.getLong("xpValue");

                    contactProfileFragment.addTask(taskId, action, xpValue);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }, error -> Log.e("Volley", error.toString()));


        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

    public void getCompletedTasks(final TaskDetailProgressFragment taskDetailProgressFragment) {
        User loggedInUser = getLoggedInUser();
        String url = SERVER_URL + "/tasks/complete/"+String.valueOf(loggedInUser.getId()) + "?pageSize=20";

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(url, response -> {
            try {
                long totalXp = 0;
                JSONArray jArray = response.getJSONArray("foundObjects");
                for(int i=0; i<jArray.length(); i++) {
                    JSONObject taskObject = jArray.getJSONObject(i);

                    long taskId = taskObject.getLong("taskId");
                    String action = taskObject.getString("action");
                    long xpValue = taskObject.getLong("xpValue");

                    totalXp = totalXp + xpValue;
                    taskDetailProgressFragment.addCompletedTask(taskId, action, xpValue);
                }

                taskDetailProgressFragment.setLevel(totalXp);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, error -> Log.e("Volley", error.toString()));

        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

    public void getRemainingTasks(final TaskDetailProgressFragment taskDetailProgressFragment) {
        User loggedInUser = getLoggedInUser();
        String url = SERVER_URL + "/tasks/remaining/"+String.valueOf(loggedInUser.getId()) + "?pageSize=20";

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(url, response -> {
            try {
                long totalXp = 0;
                JSONArray jArray = response.getJSONArray("foundObjects");
                for(int i=0; i<jArray.length(); i++) {
                    JSONObject taskObject = jArray.getJSONObject(i);

                    long taskId = taskObject.getLong("taskId");
                    String action = taskObject.getString("action");
                    long xpValue = taskObject.getLong("xpValue");

                    totalXp = totalXp + xpValue;
                    taskDetailProgressFragment.addRemainingTask(taskId, action, xpValue);
                }

                taskDetailProgressFragment.setLevel(totalXp);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, error -> Log.e("Volley", error.toString()));

        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

	public void likeNewsArticle(final NewsArticleFragment newsArticleFragment) {
        toggleContentLike(true, "newsArticle", newsArticleFragment.getArticleId());
	}

    /**
     * Convenience method for getRequest(params, withAuth) - majority of method usage
     * is with user auth.
     */
    private String getRequest(String params) {
        return getRequest(params, true);
    }

    private String getRequest(String params, boolean withAuth) {
        if (params == null || params.length() == 0) return "";

        StringBuilder stringBuffer = new StringBuilder();
        BufferedReader bufferedReader = null;

        try {
            Log.d("Isegoria", "GET REQUEST WITH PARAMS: "+params);
            URL url = new URL(SERVER_URL + params);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setDoInput(true);
            connection.setDoOutput(false);

            if (withAuth) {
                connection.setRequestProperty("Authorization", "Basic " + base64EncodedCredentials);
            }

            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Content-type", "application/json");

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) return "";

            InputStream inputStream = connection.getInputStream();
            bufferedReader = new BufferedReader(new InputStreamReader(
                    inputStream));

            stringBuffer = new StringBuilder();
            String readLine = bufferedReader.readLine();
            while (readLine != null) {
                stringBuffer.append(readLine);
                stringBuffer.append("\n");
                readLine = bufferedReader.readLine();
            }

        } catch (Exception e) {
            Log.e("Isegoria", "exception", e);

        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    Log.e("Isegoria", "exception", e);
                }
            }
        }

        return stringBuffer.toString();
    }

    public interface PictureDownloadListener {
        void onDownloadFinished(String url, @Nullable Bitmap bitmap);
        void onDownloadFailed(String url, VolleyError error);
    }

	public void getPicture(final String url, @NonNull final PictureDownloadListener callback) {
        ImageRequest req = new ImageRequest(url,
                bitmap -> callback.onDownloadFinished(url, bitmap), 0, 0, ImageView.ScaleType.CENTER_INSIDE, null,
                error -> {
                    Log.d("Volley", error.toString());
                    callback.onDownloadFailed(url, error);
                });

        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

    public void getFirstPhoto(final int positionId, final ImageView imageView) {
        String url = String.format("%s/photos/%s/", SERVER_URL, String.valueOf(positionId));

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(url,
                response -> {
                    try {
                        JSONArray photos = response.getJSONArray("photos");

                        int photoIndex = 0;
                        for (int i = 0; i < photos.length(); i++) {
                            JSONObject currentObject = photos.getJSONObject(i);
                            String currentSeq = currentObject.getString("sequence");

                            if (currentSeq.equals("0")) {
                                photoIndex = i;
                                break;
                            }
                        }

                        String url1 = (response.getJSONArray("photos").getJSONObject(photoIndex).getString("url"));
                        getFirstPhotoImage(url1, imageView);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }, error -> Log.e("Volley", error.toString()));


        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

    public void getUserDPId() {
        User loggedInUser = getLoggedInUser();
        String url = String.format("%s/photos/%s/", SERVER_URL, String.valueOf(loggedInUser.getEmail()));

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(url,
                response -> {
                    try {
                        userDPId = (response.getJSONArray("photos").getJSONObject(0).getInt("nodeId"));
                        String url1 = (response.getJSONArray("photos").getJSONObject(0).getString("url"));

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }, error -> Log.e("Volley", error.toString()));


        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

    private void getFirstPhotoImage(String url, final ImageView view) {
        ImageRequest req = new ImageRequest(url,
                bitmap -> {
                    view.setImageBitmap(bitmap);
                    view.refreshDrawableState();
                }, 0, 0, ImageView.ScaleType.CENTER_INSIDE, null,
                error -> Log.e("Volley", error.toString())) {
        };


        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

    private void getFirstPhotoBlur(int electionId, int positionId, final LinearLayout imageView) {
        String url = SERVER_URL + "/photos/" + String.valueOf(positionId) + "/";

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(url,
                response -> {
                    try {
                        JSONArray photos = response.getJSONArray("photos");

                        int index=0;
                        for(int i=0; i<photos.length(); i++) {
                            JSONObject currentObject = photos.getJSONObject(i);
                            String currentSeq = currentObject.getString("sequence");

                            if(currentSeq.equals("0")) {
                                index = i;
                                break;
                            }
                        }

                        String url1 = (response.getJSONArray("photos").getJSONObject(index).getString("url"));
                        getFirstPhotoImageBlur(url1, imageView);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }, error -> Log.e("Volley", error.toString()));


        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

    private void getFirstPhotoImageBlur(String url, final LinearLayout view) {
        ImageRequest req = new ImageRequest(url,
                bitmap -> {
                    Drawable d = new BitmapDrawable(application.getMainActivity().getResources(),
                            Utils.fastBlur(bitmap, 25));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        view.setBackground(d);
                    } else {
                        view.setBackgroundDrawable(d);
                    }
                }, 0, 0, ImageView.ScaleType.CENTER_INSIDE, null,
                error -> Log.e("Volley", error.toString()));


        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

    public void getPictureVolley(String params, final ImageView imageView) {
        if (params == null) return;

        ImageRequest req = new ImageRequest(params, imageView::setImageBitmap
                , 0, 0, ImageView.ScaleType.CENTER_INSIDE, null, error -> Log.e("Volley", error.toString()));


        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

    public void getPictureVolley2(String params, final ImageView view, final int squareSize, final PhotoViewFragment fragment) {
        ImageRequest req = new ImageRequest(params,
                srcBmp -> {
                    view.setImageBitmap(srcBmp);
                    fragment.setImageBitmap(srcBmp);
                }, 0, 0, ImageView.ScaleType.CENTER_INSIDE, null,
                error -> Log.e("Volley", error.toString()));


        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

    public ArrayList<Integer> getUserTickets() {
        return userTickets;
    }

    public void setUserTickets(ArrayList<Integer> userTickets) {
        this.userTickets = userTickets;
    }

    public boolean isReminderSet() {
        return reminderSet;
    }

    public void setReminderSet(boolean reminderSet) {
        this.reminderSet = reminderSet;
    }

    public long getVoteReminderDate() {
        return voteReminderDate;
    }

    private void setVoteReminderDate(long voteReminderDate) {
        this.voteReminderDate = voteReminderDate;
    }

    public String getVoteReminderLocation() {
        return voteReminderLocation;
    }

    private void setVoteReminderLocation(String voteReminderLocation) {
        this.voteReminderLocation = voteReminderLocation;
    }

    public void s3Upload(final File file) {
        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                application.getApplicationContext(), // Context,
                "715927704730",
                "us-east-1:73ae30c9-393c-44cf-a0ac-049cc0838428",
                "arn:aws:iam::715927704730:role/Cognito_isegoriaUnauth_Role",
                "arn:aws:iam::715927704730:role/Cognito_isegoriaAuth_Role",
                Regions.US_EAST_1 // Region
        );

        AmazonS3Client s3Client = new AmazonS3Client(credentialsProvider);

        TransferUtility transferUtility = TransferUtility.builder()
                .s3Client(s3Client)
                .context(application.getApplicationContext())
                .build();

        long timestamp = System.currentTimeMillis() / 1000L;
        final String filename = String.valueOf(getLoggedInUser().getId()) + "_" + String.valueOf(timestamp) + "_" + file.getName();
        TransferObserver observer = transferUtility.upload("isegoriauserpics", filename, file);
        observer.setTransferListener(new TransferListener() {
            @Override
            public void onStateChanged(int id, TransferState state) {
                if (state == TransferState.COMPLETED) {
                    updateDisplayPicturePhoto("https://s3.amazonaws.com/isegoriauserpics/" + filename);
                }
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) { }

            @Override
            public void onError(int id, Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    private void updateDisplayPicturePhoto(String pictureURL) {
        String url = String.format("%s/photo", SERVER_URL);
        long timestamp = System.currentTimeMillis() / 1000L;

        User loggedInUser = getLoggedInUser();

        HashMap<String, String> params = new HashMap<>();
        params.put("url", pictureURL);
        params.put("thumbNailUrl", pictureURL);
        params.put("title", "Profile Picture");
        params.put("description", "Profile Picture");
        params.put("date", String.valueOf(timestamp));
        params.put("ownerId", String.valueOf(loggedInUser.getId()));
        params.put("sequence", String.valueOf("0"));

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(Request.Method.POST, url, new JSONObject(params),
                response -> {
                    try {
                        String result = response.toString();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }, error -> Log.e("Volley", error.toString()));


        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);

        Log.d("VolleyRequest", req.toString());
    }

    public void updateUserDetails() {
        User loggedInUser = getLoggedInUser();
        String url = String.format("%s/user/%s", SERVER_URL, String.valueOf(loggedInUser.getId()));

        HashMap<String, String> params = new HashMap<>();
        params.put("trackingOff", String.valueOf(loggedInUser.isTrackingOff()));
        params.put("optOutDataCollection", String.valueOf(loggedInUser.isOptedOutOfDataCollection()));

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(Request.Method.PUT, url, new JSONObject(params),
                response -> {
                    try {
                        String responseString = response.toString();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }, error -> Log.e("Volley", error.toString()));

        
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);

        Log.d("VolleyRequest", req.toString());
    }
}
