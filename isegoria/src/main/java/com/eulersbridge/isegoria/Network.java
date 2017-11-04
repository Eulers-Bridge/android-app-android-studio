package com.eulersbridge.isegoria;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;

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
import com.eulersbridge.isegoria.feed.PhotoViewFragment;
import com.eulersbridge.isegoria.models.Badge;
import com.eulersbridge.isegoria.models.Candidate;
import com.eulersbridge.isegoria.models.CandidateTicket;
import com.eulersbridge.isegoria.models.Country;
import com.eulersbridge.isegoria.models.Election;
import com.eulersbridge.isegoria.models.Event;
import com.eulersbridge.isegoria.models.FriendRequest;
import com.eulersbridge.isegoria.models.NewsArticle;
import com.eulersbridge.isegoria.models.Photo;
import com.eulersbridge.isegoria.models.PhotoAlbum;
import com.eulersbridge.isegoria.models.Poll;
import com.eulersbridge.isegoria.models.Position;
import com.eulersbridge.isegoria.models.Task;
import com.eulersbridge.isegoria.models.User;
import com.eulersbridge.isegoria.models.VoteLocation;
import com.eulersbridge.isegoria.utilities.Utils;

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
    private ArrayList<Long> userTickets = new ArrayList<>();

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
                    signup.put("hasPersonality", false);

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

	public interface GeneralInfoListener {
	    void onFetchCountriesSuccess(ArrayList<Country> countries);
	    void onFetchCountriesFailure(Exception e);
    }

	public void getGeneralInfo(@NonNull final GeneralInfoListener callback) {
		Runnable r = () -> {
            String response = getRequest("/general-info", false);
            try {
                JSONObject responseObject = new JSONObject(response);
                JSONArray countriesJson = responseObject.getJSONArray("countrys");

                ArrayList<Country> countries = new ArrayList<>();

                for (int i = 0; i < countriesJson.length(); i++) {
                    JSONObject countryObject = countriesJson.getJSONObject(i);

                    Country country = new Country(countryObject);
                    countries.add(country);
                }

                callback.onFetchCountriesSuccess(countries);

            } catch (JSONException e) {
                e.printStackTrace();
                callback.onFetchCountriesFailure(e);
            }
        };

		Thread t = new Thread(r);
		t.start();
	}

	public interface NewsArticlesListener {
	    void onFetchSuccess(ArrayList<NewsArticle> articles);
        void onFetchFailure(Exception e);
    }

	public void getNewsArticles(final NewsArticlesListener callback) {
        String url = String.format("%s/newsArticles/26", SERVER_URL);

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(url, response -> {
            try {
                JSONArray foundObjects = response.getJSONArray("foundObjects");

                ArrayList<NewsArticle> articles = new ArrayList<>();

                for (int i = 0; i < foundObjects.length(); i++) {
                    JSONObject articleObject = foundObjects.getJSONObject(i);

                    NewsArticle article = new NewsArticle(articleObject);
                    articles.add(article);
                }

                callback.onFetchSuccess(articles);

            } catch (JSONException e) {
                e.printStackTrace();
                callback.onFetchFailure(e);

            }
        },
        error -> {
            Log.e("Volley", error.toString());
            callback.onFetchFailure(error);
        });

        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
	}

	public interface UserInfoListener {
        void onFetchSuccess(User user);
        void onFetchFailure(String userEmail, Exception e);
    }

	public void getUser(final String userEmail, final UserInfoListener callback) {
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

    public interface FriendsListener {
	    void onFetchSuccess(ArrayList<User> friends);
	    void onFetchFailure(Exception e);
    }

    void getFriends(final FriendsListener callback) {
        String url = String.format("%s/contacts", SERVER_URL);

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(url, response -> {
            try {
                ArrayList<User> friends = new ArrayList<>();

                JSONArray foundObjects = response.getJSONArray("foundObjects");

                for (int i = 0; i < foundObjects.length(); i++) {
                    try {
                        JSONObject userObject = foundObjects.getJSONObject(i);

                        User user = new User(userObject);
                        friends.add(user);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                callback.onFetchSuccess(friends);

            } catch (Exception e) {
                e.printStackTrace();
                callback.onFetchFailure(e);
            }
        }, error -> {
            Log.e("Volley", error.toString());
            callback.onFetchFailure(error);
        });

        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

    public interface SearchUsersListener {
	    void onFetchSuccess(ArrayList<User> users);
	    void onFetchFailure(Exception e);
    }

	void searchForUsers(String query, final SearchUsersListener callback) {
        String url = String.format("%s/searchUserProfile/%s/", SERVER_URL, String.valueOf(query));

        AuthorisedJsonArrayRequest req = new AuthorisedJsonArrayRequest(url, jsonArray -> {
            try {
                ArrayList<User> usersFound = new ArrayList<>();

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject userObject = jsonArray.getJSONObject(i);
                    User user = new User(userObject);
                    usersFound.add(user);
                }

                callback.onFetchSuccess(usersFound);

            } catch (JSONException e) {
                e.printStackTrace();
                callback.onFetchFailure(e);
            }
        }, error -> {
            Log.e("Volley", error.toString());
            callback.onFetchFailure(error);
        });

        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
	}

	public interface ProfileStatsListener {
	    void onFetchSuccess(int contactsCount, int totalTasksCount);
	    void onFetchFailure(@Nullable Exception e);
    }

    public void getProfileStats(String userEmail, final ProfileStatsListener callback) {
        String url = String.format("%s/contact/%s/", SERVER_URL, String.valueOf(userEmail));

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(url, response -> {
            try {
                JSONArray foundObjects = response.optJSONArray("foundObjects");

                if (foundObjects != null) {
                    int contactsCount = 0;
                    int totalTasksCount = 0;

                    if (foundObjects.length() > 0) {
                        JSONObject currentObject = foundObjects.getJSONObject(0);

                        contactsCount = currentObject.getInt("numOfContacts");
                        totalTasksCount = currentObject.getInt("totalTasks");
                    }

                    callback.onFetchSuccess(contactsCount, totalTasksCount);

                } else {
                    callback.onFetchFailure(null);
                }

            } catch (Exception e) {
                e.printStackTrace();
                callback.onFetchFailure(e);
            }
        }, error -> {
            Log.e("Volley", error.toString());
            callback.onFetchFailure(error);
        });


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
                JSONArray foundObjects = response.getJSONArray("foundObjects");

                callback.onFetchSuccess(foundObjects.length());

            } catch (Exception e) {
                e.printStackTrace();
                callback.onFetchFailure(e);
            }
        }, error -> {
            Log.e("Volley", error.toString());
            callback.onFetchFailure(error);
        });

        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

    public interface AddFriendListener {
	    void onSuccess(String email);
	    void onFailure(String email, Exception e);
    }

    void addFriend(@NonNull String email, final AddFriendListener callback) {
        User loggedInUser = getLoggedInUser();
        String url = String.format("%s/user/%s/contactRequest/%s/", SERVER_URL, String.valueOf(loggedInUser.getId()), String.valueOf(email));

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(Request.Method.POST, url, null,
            response -> {
                try {
                    callback.onSuccess(email);
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.onFailure(email, e);
                }
            }, error -> {
                Log.e("Volley", error.toString());
                callback.onFailure(email, error);
            }
        );

        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);

        Log.d("VolleyRequest", req.toString());
    }

    public interface AcceptFriendRequestListener {
	    void onSuccess(String contactRequestId);
        void onFailure(String contactRequestId, Exception e);
    }

    void acceptContact(String contactRequestId, final AcceptFriendRequestListener callback) {
        String url = String.format("%s/user/contactRequest/%s/accept", SERVER_URL, contactRequestId);

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(Request.Method.PUT, url, null,
            response -> {
                try {
                    callback.onSuccess(contactRequestId);

                } catch (Exception e) {
                    e.printStackTrace();
                    callback.onFailure(contactRequestId, e);
                }
            }, error -> {
                Log.e("Volley", error.toString());
                callback.onFailure(contactRequestId, error);
            }
        );


        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);

        Log.d("VolleyRequest", req.toString());
    }

    public interface RejectFriendRequestListener {
        void onSuccess(String contactRequestId);
        void onFailure(String contactRequestId, Exception e);
    }

    void rejectContact(String contactRequestId, final RejectFriendRequestListener callback) {
        String url = String.format("%s/user/contactRequest/%s/reject", SERVER_URL, String.valueOf(contactRequestId));

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(Request.Method.PUT, url, null,
            response -> {
                try {
                    callback.onSuccess(contactRequestId);
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.onFailure(contactRequestId, e);
                }
            }, error -> {
                Log.e("Volley", error.toString());
                callback.onFailure(contactRequestId, error);
            }
        );


        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);

        Log.d("VolleyRequest", req.toString());
    }


    public interface FriendRequestsListener {
	    void onFetchSuccess(ArrayList<FriendRequest> friendRequests);
	    void onFetchFailure(Exception e);
    }

    private void getFriendRequests(FriendRequest.Type friendRequestType, final FriendRequestsListener callback) {
        User loggedInUser = getLoggedInUser();

        String url = String.format("%s/user/%s/contactRequests", SERVER_URL, String.valueOf(loggedInUser.getId()));

        if (friendRequestType == FriendRequest.Type.RECEIVED) {
            url += "/rec";
        }

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(url, response -> {
            try {
                JSONArray foundObjects = response.optJSONArray("foundObjects");

                ArrayList<FriendRequest> friendRequests = new ArrayList<>();

                if (foundObjects != null) {
                    for (int i = 0; i < foundObjects.length(); i++) {
                        JSONObject foundObject = foundObjects.getJSONObject(i);

                        FriendRequest friendRequest = new FriendRequest(foundObject, friendRequestType);

                        if (!friendRequest.isAccepted() && !friendRequest.isRejected()) {
                            friendRequests.add(friendRequest);
                        }
                    }
                }

                callback.onFetchSuccess(friendRequests);

            } catch (Exception e) {
                e.printStackTrace();
                callback.onFetchFailure(e);
            }
        }, error -> {
            Log.e("Volley", error.toString());
            callback.onFetchFailure(error);
        });

        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

    void getFriendRequestsSent(final FriendRequestsListener callback) {
	    getFriendRequests(FriendRequest.Type.SENT, callback);
    }

    void getFriendRequestsReceived(final FriendRequestsListener callback) {
        getFriendRequests(FriendRequest.Type.RECEIVED, callback);
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

    public void verifyEmail() {
        String url = String.format("%s/emailVerification/%s/resend", SERVER_URL, String.valueOf(getLoggedInUser().getEmail()));

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(url,
                jObject -> {

                }, error -> Log.e("Volley", error.toString()));


        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

    public interface PhotoAlbumsListener {
	    void onFetchSuccess(ArrayList<PhotoAlbum> albums);
	    void onFetchFailure(Exception e);
    }

	public void getPhotoAlbums(final PhotoAlbumsListener callback) {
        String url = String.format("%s/photoAlbums/7449", SERVER_URL);

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(url,
            jObject -> {
                try {
                    JSONArray foundObjects = jObject.getJSONArray("foundObjects");

                    ArrayList<PhotoAlbum> albums = new ArrayList<>();

                    for (int i = 0; i < foundObjects.length(); i++) {
                        JSONObject albumObject = foundObjects.getJSONObject(i);

                        PhotoAlbum album = new PhotoAlbum(albumObject);
                        albums.add(album);
                    }

                    callback.onFetchSuccess(albums);

                } catch (JSONException e) {
                    e.printStackTrace();
                    callback.onFetchFailure(e);
                }
            }, error -> {
                Log.e("Volley", error.toString());
                callback.onFetchFailure(error);
            }
        );


        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
	}

    public interface PhotosListener {
        void onFetchSuccess(ArrayList<Photo> photos);
        void onFetchFailure(Exception e);
    }

	public void getAlbumPhotos(int albumId, PhotosListener callback) {
		String url = String.format("%s/photos/%s?pageSize=100", SERVER_URL, String.valueOf(albumId));

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(url,
            jsonObject -> {
                try {
                    JSONArray photosArray = jsonObject.getJSONArray("photos");

                    ArrayList<Photo> photos = new ArrayList<>();

                    for (int i = 0; i < photosArray.length(); i++) {
                        JSONObject photoObject = photosArray.getJSONObject(i);

                        Photo photo = new Photo(photoObject);
                        photos.add(photo);
                    }

                    callback.onFetchSuccess(photos);

                } catch (JSONException e) {
                    e.printStackTrace();
                    callback.onFetchFailure(e);
                }
            }, error -> {
                Log.e("Volley", error.toString());
                callback.onFetchFailure(error);
            }
        );

        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
	}

    public interface ProfileBadgesListener {
        void onFetchSuccess(ArrayList<Badge> badges);
        void onFetchFailure(Exception e);
    }

    public void getProfileBadgesComplete(int targetLevel, ProfileBadgesListener callback) {
        String url = String.format("%s/badges/complete/%s", SERVER_URL, String.valueOf(getLoggedInUser().getId()));

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(url, response -> {
                try {
                    JSONArray foundObjects = response.getJSONArray("foundObjects");

                    ArrayList<Badge> badges = new ArrayList<>();

                    for (int i = 0; i < foundObjects.length(); i++) {
                        JSONObject badgeObject = foundObjects.getJSONObject(i);

                        Badge badge = new Badge(badgeObject);

                        if (badge.getLevel() == targetLevel) {
                            badges.add(badge);
                        }
                    }

                    callback.onFetchSuccess(badges);

                } catch (JSONException e) {
                    e.printStackTrace();
                    callback.onFetchFailure(e);
                }
            },
            error -> {
                Log.e("Volley", error.toString());
                callback.onFetchFailure(error);
            }
        );


        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

    public void getProfileBadgesRemaining(int targetLevel, ProfileBadgesListener callback) {
        String url = String.format("%s/badges/remaining/%s", SERVER_URL, String.valueOf(getLoggedInUser().getId()));

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(url, response -> {
            try {
                JSONArray foundObjects = response.getJSONArray("foundObjects");

                ArrayList<Badge> badges = new ArrayList<>();

                for (int i = 0; i < foundObjects.length(); i++) {
                    JSONObject badgeObject = foundObjects.getJSONObject(i);

                    Badge badge = new Badge(badgeObject);
                    if (badge.getLevel() == targetLevel) {
                        badges.add(badge);
                    }
                }

                callback.onFetchSuccess(badges);

            } catch (JSONException e) {
                e.printStackTrace();
                callback.onFetchFailure(e);
            }
        }, error -> {
            Log.e("Volley", error.toString());
            callback.onFetchFailure(error);
        });


        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

    public interface PhotoListener {
	    void onFetchSuccess(Photo photo);
	    void onFetchFailure(long photoId, Exception e);
    }

	public void getPhoto(int photoId, PhotoListener callback) {
		String url = String.format("%s/photo/%s", SERVER_URL, String.valueOf(photoId));

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(url,
            response -> {
                Photo photo = new Photo(response);
                callback.onFetchSuccess(photo);
            }, error -> {
                Log.e("Volley", error.toString());
                callback.onFetchFailure(photoId, error);
            }
        );

        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
	}

	public interface NewsArticleLikedListener {
	    void onFetchSuccess(long articleId, boolean likedByUser);
        void onFetchFailure(long articleId, Exception e);
    }

    public void getNewsArticleLiked(long articleId, NewsArticleLikedListener callback) {
        String url = String.format("%s/newsArticle/%s/likedBy/%s/", SERVER_URL,
                String.valueOf(articleId), String.valueOf(getLoggedInUser().getEmail()));

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(Request.Method.GET, url, null, response -> {
            try {
                boolean success = response.getBoolean("success");

                callback.onFetchSuccess(articleId, success);

            } catch (Exception e) {
                e.printStackTrace();
                callback.onFetchFailure(articleId, e);
            }
        }, error -> {
            Log.e("Volley", error.toString());
            callback.onFetchFailure(articleId, error);
        });


        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

    public interface PollsListener {
	    void onFetchSuccess(ArrayList<Poll> polls);
	    void onFetchFailure(Exception e);
    }

	public void getPollOptions(PollsListener callback) {
		String url = String.format("%s/polls/26", SERVER_URL);

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(url,
            response -> {

                try {
                    JSONArray pollObjects = response.getJSONArray("polls");

                    ArrayList<Poll> polls = new ArrayList<>();

                    for (int i = 0; i < pollObjects.length(); i++) {
                        JSONObject pollObject = pollObjects.getJSONObject(i);

                        Poll poll = new Poll(pollObject);
                        polls.add(poll);
                    }

                    callback.onFetchSuccess(polls);

                } catch (JSONException e) {
                    e.printStackTrace();
                    callback.onFetchFailure(e);
                }
            }, error -> {
                Log.e("Volley", error.toString());
                callback.onFetchFailure(error);
        });


        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
	}

    private void getPollResults(long nodeId, PollResultsListener callback) {
        String url = String.format("%s/poll/%s/results", SERVER_URL, String.valueOf(nodeId));

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(url,
            response -> {
                try {
                    JSONArray answersArray = response.getJSONArray("answers");

                    ArrayList<Integer> results = new ArrayList<>();

                    for (int i = 0; i < answersArray.length(); i++) {
                        JSONObject currentPollAnswers = answersArray.getJSONObject(i);
                        int count = currentPollAnswers.getInt("count");

                        results.add(count);
                    }

                    callback.onFetchSuccess(results);

                } catch (JSONException e) {
                    e.printStackTrace();
                    callback.onFetchFailure(e);
                }
            }, error -> {
                Log.e("Volley", error.toString());
                callback.onFetchFailure(error);
            }
        );


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

    public void addPersonalityForUser(float extroversion, float agreeableness, float conscientiousness,
                                  float emotionalStability, float openness) {
        User loggedInUser = getLoggedInUser();
        String url = String.format("%s/user/%s/personality", SERVER_URL, String.valueOf(loggedInUser.getEmail()));

        HashMap<String, Float> params = new HashMap<>();
        params.put("agreeableness", agreeableness);
        params.put("conscientiousness", conscientiousness);
        params.put("emotionalStability", emotionalStability);
        params.put("extroversion", extroversion);
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

    public interface PollResultsListener {
	    // Results is an array of integers, each a vote count for the poll's options (in the same order)
	    void onFetchSuccess(ArrayList<Integer> results);
        void onFetchFailure(Exception e);
    }

    public void answerPoll(long pollId, int answerIndex, PollResultsListener callback) {
        String url = String.format("%s/poll/%s/vote/%s", SERVER_URL, String.valueOf(pollId), String.valueOf(answerIndex));

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(Request.Method.PUT, url, null,
            response -> {
                try {
                    getPollResults(pollId, callback);

                } catch (Exception e) {
                    e.printStackTrace();
                    callback.onFetchFailure(e);

                }
            }, error -> {
                Log.e("Volley", error.toString());
                callback.onFetchFailure(error);
            }
        );


        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);

        Log.d("VolleyRequest", req.toString());
    }

    public interface VoteLocationsListener {
	    void onFetchSuccess(ArrayList<VoteLocation> voteLocations);
	    void onFetchFailure(Exception e);
    }

    public void getVoteLocations(VoteLocationsListener callback) {

        String url = String.format("%s/votingLocations/26", SERVER_URL);
        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(url, response -> {
            try {
               JSONArray foundObjects = response.getJSONArray("foundObjects");

                ArrayList<VoteLocation> voteLocations = new ArrayList<>();

               for (int i = 0; i < foundObjects.length(); i++) {
                   JSONObject voteLocationObject = foundObjects.getJSONObject(i);
                   VoteLocation voteLocation = new VoteLocation(voteLocationObject);

                   voteLocations.add(voteLocation);
                }

                callback.onFetchSuccess(voteLocations);

            } catch (Exception e) {
                e.printStackTrace();
                callback.onFetchFailure(e);
            }
        }, error -> {
            Log.e("Volley", error.toString());
            callback.onFetchFailure(error);
        });


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

                electionId = electionObject.getInt("electionId");

                alreadySetVoteReminder();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, error -> Log.e("Volley", error.toString()));


        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

    public interface ElectionListener {
        void onFetchSuccess(Election election);
        void onFetchFailure(Exception e);
    }

    public void getLatestElection(ElectionListener callback) {
        String url = String.format("%s/elections/26", SERVER_URL);

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(url, response -> {
            try {
                JSONArray foundObjects = response.getJSONArray("foundObjects");

                if (foundObjects != null && foundObjects.length() >= 1) {
                    JSONObject electionObject = foundObjects.getJSONObject(0);

                    Election election = new Election(electionObject);

                    callback.onFetchSuccess(election);

                } else {
                    callback.onFetchFailure(null);
                }

            } catch (Exception e) {
                e.printStackTrace();
                callback.onFetchFailure(e);
            }
        }, error -> {
            Log.e("Volley", error.toString());
            callback.onFetchFailure(error);
        });

        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

    public void getUserSupportedTickets() {
        User loggedInUser = getLoggedInUser();
        String url = String.format("%s/user/%s/support/", SERVER_URL, String.valueOf(loggedInUser.getId()));

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(url, response -> {
            try {
                JSONArray foundObjects = response.getJSONArray("foundObjects");
                userTickets.clear();
                for(int i = 0; i < foundObjects.length(); i++) {
                    JSONObject currentObject = foundObjects.getJSONObject(i);

                    long ticketId = currentObject.getLong("ticketId");
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

    /*public void getCandidates(final CandidatePositionsFragment candidatePositionsFragment) {
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
    }*/

    public interface CandidatesListener {
        void onFetchSuccess(ArrayList<Candidate> candidates);
        void onFetchFailure(Exception e);
    }

    public void getCandidates(CandidatesListener callback) {
        String url = String.format("%s/candidates/%s", SERVER_URL, String.valueOf(electionId));

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(url, response -> {
            try {
                JSONArray foundObjects = response.getJSONArray("foundObjects");

                ArrayList<Candidate> candidates = new ArrayList<>();

                for (int i = 0; i < foundObjects.length(); i++) {
                    JSONObject candidateObject = foundObjects.getJSONObject(i);

                    Candidate candidate = new Candidate(candidateObject);
                    candidates.add(candidate);
                }

                callback.onFetchSuccess(candidates);

            } catch (Exception e) {
                e.printStackTrace();
                callback.onFetchFailure(e);
            }
        }, error -> {
            Log.e("Volley", error.toString());
            callback.onFetchFailure(error);
        });


        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

    public void getCandidatesPosition(long selectedPositionId, CandidatesListener callback) {
        String url = String.format("%s/position/%s/candidates", SERVER_URL, String.valueOf(selectedPositionId));

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(url, response -> {
            try {
                JSONArray foundObjects = response.getJSONArray("foundObjects");

                ArrayList<Candidate> candidates = new ArrayList<>();

                for(int i = 0; i < foundObjects.length(); i++) {
                    JSONObject candidateObject = foundObjects.getJSONObject(i);

                    Candidate candidate = new Candidate(candidateObject);
                    candidates.add(candidate);
                }

                callback.onFetchSuccess(candidates);

            } catch (Exception e) {
                e.printStackTrace();
                callback.onFetchFailure(e);
            }
        }, error -> {
            Log.e("Volley", error.toString());
            callback.onFetchFailure(error);
        });

        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

    public interface TicketLabelListener {
        void onFetchSuccess(long ticketId, String colour, String code);
        void onFetchFailure(Exception e);
    }

    public void getTicketLabel(long ticketId, TicketLabelListener callback) {
        String url = String.format("%s/ticket/%s", SERVER_URL, String.valueOf(ticketId));

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(url, response -> {
            try {
                String colour = response.getString("colour");
                String code = response.getString("code");

                callback.onFetchSuccess(ticketId, colour, code);

            } catch (Exception e) {
                e.printStackTrace();
                callback.onFetchFailure(e);
            }
        }, error -> {
            Log.e("Volley", error.toString());
            callback.onFetchFailure(error);
        });


        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

    public interface TicketsListener {
        void onFetchSuccess(ArrayList<CandidateTicket> tickets);
        void onFetchFailure(Exception e);
    }

    public void getTickets(TicketsListener callback) {
        String url = String.format("%s/tickets/%s", SERVER_URL, String.valueOf(electionId));

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(url, response -> {
            try {
                JSONArray foundObjects = response.getJSONArray("foundObjects");

                ArrayList<CandidateTicket> tickets = new ArrayList<>();

                for (int i = 0; i < foundObjects.length(); i++) {
                    JSONObject ticketObject = foundObjects.getJSONObject(i);

                    CandidateTicket candidateTicket = new CandidateTicket(ticketObject);
                    tickets.add(candidateTicket);
                }

                callback.onFetchSuccess(tickets);

            } catch (Exception e) {
                e.printStackTrace();
                callback.onFetchFailure(e);
            }
        }, error -> {
            Log.e("Volley", error.toString());
            callback.onFetchFailure(error);
        });


        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

    public void getTicketDetail(long ticketId, TicketsListener callback) {
        String url = String.format("%s/ticket/%s/candidates", SERVER_URL, String.valueOf(ticketId));

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(url, response -> {
            try {
                JSONArray foundObjects = response.getJSONArray("foundObjects");

                ArrayList<CandidateTicket> tickets = new ArrayList<>();

                for(int i = 0; i < foundObjects.length(); i++) {
                    JSONObject currentObject = foundObjects.getJSONObject(i);

                    CandidateTicket ticket = new CandidateTicket(currentObject);

                    /*int ticketId1 = currentObject.getInt("ticketId");
                    String givenName = currentObject.getString("givenName");
                    String familyName = currentObject.getString("familyName");
                    String name = givenName + " " + familyName;
                    String code = "";
                    String colour = "#000000";
                    String information = "";
                    String logo = "";*/

                    tickets.add(ticket);
                }

                callback.onFetchSuccess(tickets);

            } catch (Exception e) {
                e.printStackTrace();
                callback.onFetchFailure(e);
            }
        }, error -> {
            Log.e("Volley", error.toString());
            callback.onFetchFailure(error);
        });


        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

    public void getTicketCandidates(long selectedTickedId, CandidatesListener callback) {
        String url = String.format("%s/ticket/%s/candidates", SERVER_URL, String.valueOf(selectedTickedId));

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(url, response -> {
            try {
                JSONArray foundObjects = response.getJSONArray("foundObjects");

                ArrayList<Candidate> candidates = new ArrayList<>();

                for (int i = 0; i < foundObjects.length(); i++) {
                    JSONObject candidateObject = foundObjects.getJSONObject(i);

                    Candidate candidate = new Candidate(candidateObject);

                    if (selectedTickedId != candidate.getTicketId()) {
                        continue;
                    }

                    candidates.add(candidate);
                }

                callback.onFetchSuccess(candidates);

            } catch (Exception e) {
                e.printStackTrace();
                callback.onFetchFailure(e);
            }
        }, error -> {
            Log.e("Volley", error.toString());
            callback.onFetchFailure(error);
        });


        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

    public interface PositionListener {
        void onFetchSuccess(long positionId, String name);
        void onFetchFailure(Exception e);
    }

    public void getPositionText(long positionId, PositionListener callback) {
        String url = String.format("%s/position/%s", SERVER_URL, String.valueOf(positionId));

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(url,
            response -> {
                try {
                    String name = response.getString("name");

                    callback.onFetchSuccess(positionId, name);

                } catch (JSONException e) {
                    e.printStackTrace();
                    callback.onFetchFailure(e);
                }
            }, error -> {
                Log.e("Volley", error.toString());
                callback.onFetchFailure(error);
            }
        );


        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

    public interface PositionsListener {
        void onFetchSuccess(ArrayList<Position> positions);
        void onFetchFailure(Exception e);
    }

    public void getPositions(PositionsListener callback) {
        String url = String.format("%s/positions/%s", SERVER_URL, String.valueOf(electionId));

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(url, response -> {
            try {
                JSONArray foundObjects = response.getJSONArray("foundObjects");

                ArrayList<Position> positions = new ArrayList<>();

                for (int i = 0; i < foundObjects.length(); i++) {
                    JSONObject positionObject = foundObjects.getJSONObject(i);
                    Position position = new Position(positionObject);
                    positions.add(position);
                }

                callback.onFetchSuccess(positions);

            } catch (Exception e) {
                e.printStackTrace();
                callback.onFetchFailure(e);

            }
        }, error -> {
            Log.e("Volley", error.toString());
            callback.onFetchFailure(error);
        });

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

    public void supportTicket(long ticketId) {
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

    public void unsupportTicket(long ticketId) {
        User loggedInUser = getLoggedInUser();
        String url = SERVER_URL + "/ticket/" + String.valueOf(ticketId) + "/support/" + String.valueOf(loggedInUser.getEmail()) + "/";
        HashMap<String, String> params = new HashMap<>();
        userTickets.remove(Long.valueOf(ticketId));

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

    public interface PhotoLikedListener {
        void onFetchSuccess(long photoId, boolean liked);
        void onFetchFailure(Exception e);
    }

    public void getPhotoLiked(long photoId, PhotoLikedListener callback) {
        User loggedInUser = getLoggedInUser();
        String url = String.format("%s/photo/%s/likedBy/%s/", SERVER_URL, String.valueOf(photoId),
                String.valueOf(loggedInUser.getEmail()));

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(Request.Method.GET, url, null, response -> {
            try {
                boolean success = response.getBoolean("success");

                callback.onFetchSuccess(photoId, success);

            } catch (Exception e) {
                e.printStackTrace();
                callback.onFetchFailure(e);
            }

        }, error -> {
            Log.e("Volley", error.toString());
            callback.onFetchFailure(error);
        });

        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

    private void toggleContentLike(boolean like, String contentType, long contentId) {
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

    public void likeArticle(long articleId) {
        toggleContentLike(true, "newsArticle", articleId);
    }

    public void unlikeArticle(long articleId) {
        toggleContentLike(false, "newsArticle", articleId);
    }

    public interface TasksListener {
        void onFetchSuccess(ArrayList<Task> tasks);
        void onFetchFailure(Exception e);
    }

    public void getTasks(TasksListener callback) {
        String url = String.format("%s/tasks/", SERVER_URL);

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(url, response -> {
            try {
                JSONArray foundObjects = response.getJSONArray("foundObjects");

                ArrayList<Task> tasks = new ArrayList<>();

                for (int i = 0; i < foundObjects.length(); i++) {
                    JSONObject taskObject = foundObjects.getJSONObject(i);

                    Task task = new Task(taskObject);
                    tasks.add(task);
                }

                callback.onFetchSuccess(tasks);

            } catch (Exception e) {
                e.printStackTrace();
                callback.onFetchFailure(e);
            }
        }, error -> {
            Log.e("Volley", error.toString());
            callback.onFetchFailure(error);
        });

        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

    public interface TasksTotalXPListener {
        void onFetchSuccess(ArrayList<Task> tasks, long totalXp);
        void onFetchFailure(Exception e);
    }

    public void getCompletedTasks(TasksTotalXPListener callback) {
        User loggedInUser = getLoggedInUser();
        String url = String.format("%s/tasks/complete/%s?pageSize=20", SERVER_URL, String.valueOf(loggedInUser.getId()));

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(url, response -> {
            try {
                long totalXp = 0;

                JSONArray foundObjects = response.getJSONArray("foundObjects");

                ArrayList<Task> tasks = new ArrayList<>();

                for(int i = 0; i < foundObjects.length(); i++) {
                    JSONObject taskObject = foundObjects.getJSONObject(i);

                    Task task = new Task(taskObject);
                    tasks.add(task);

                    totalXp = totalXp + task.getXpValue();
                }

                callback.onFetchSuccess(tasks, totalXp);

            } catch (Exception e) {
                e.printStackTrace();
                callback.onFetchFailure(e);
            }
        }, error -> {
            Log.e("Volley", error.toString());
            callback.onFetchFailure(error);
        });

        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

    public void getRemainingTasks(TasksTotalXPListener callback) {
        User loggedInUser = getLoggedInUser();
        String url = String.format("%s/tasks/remaining/%s?pageSize=20", SERVER_URL, String.valueOf(loggedInUser.getId()));

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(url, response -> {
            try {
                long totalXp = 0;

                JSONArray foundObjects = response.getJSONArray("foundObjects");

                ArrayList<Task> tasks = new ArrayList<>();

                for (int i = 0; i < foundObjects.length(); i++) {
                    JSONObject taskObject = foundObjects.getJSONObject(i);

                    Task task = new Task(taskObject);
                    tasks.add(task);

                    totalXp = totalXp + task.getXpValue();
                }

                callback.onFetchSuccess(tasks, totalXp);

            } catch (Exception e) {
                e.printStackTrace();
                callback.onFetchFailure(e);
            }
        }, error -> {
            Log.e("Volley", error.toString());
            callback.onFetchFailure(error);
        });

        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

	public void likeNewsArticle(long articleId) {
        toggleContentLike(true, "newsArticle", articleId);
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

    public void getFirstPhoto(long positionId, final ImageView imageView) {
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

    public void getPictureVolley2(String params, final ImageView view, final PhotoViewFragment fragment) {
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

    public ArrayList<Long> getUserTickets() {
        return userTickets;
    }

    public boolean isReminderSet() {
        return reminderSet;
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
