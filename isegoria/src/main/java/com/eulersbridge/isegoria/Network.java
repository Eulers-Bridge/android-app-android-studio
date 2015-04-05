package com.eulersbridge.isegoria;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Cache;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class Network {
	private static String SERVER_URL = "http://eulersbridge.com:8080/";
	private static String PICTURE_URL = "https://s3-ap-southeast-2.amazonaws.com/isegoria/";
	public long userId;
	private String username;
	private String password;
	private String email;

    private Network network;
	private NewsFragment newsFragment;
	private NewsArticleFragment newsArticleFragment;
	private UserSignupFragment userSignupFragment;
	private EventsDetailFragment eventDetailFragment;
    private ElectionOverviewFragment electionOverviewFragment;
    private EventsDetailFragment eventsDetailFragment;
    private CandidatePositionsFragment candidatePositionsFragment;
	private PhotosFragment photosFragment;
	private PhotoAlbumFragment photoAlbumFragment;
	private PhotoViewFragment photoViewFragment;
    private CandidateAllFragment candidateAllFragment;
    private CandidateTicketFragment candidateTicketFragment;
    private CandidateTicketDetailFragment candidateTicketDetailFragment;
    private CandidatePositionFragment candidatePositionFragment;
    private TaskDetailProgressFragment taskDetailProgressFragment;
    private ProfileBadgesFragment profileBadgesFragment;
    private FindAddContactFragment findAddContactFragment;
	private VoteFragment voteFragment;
	private PollFragment pollFragment;
    private PollVoteFragment pollVoteFragment;
	private Isegoria application;

    private String loginGivenName = "";
    private String loginFamilyName = "";
    private String loginEmail = "";

    private int electionId;

    private RequestQueue mRequestQueue;

	public Network(Isegoria application) {
        this.network = this;
        this.application = application;
	}

	public Network(Isegoria application, String username, String password) {
        this.network = this;
		this.application = application;
		this.username = username;
		this.password = password;

        Cache cache = new DiskBasedCache(application.getCacheDir(), 1024 * 1024); // 1MB cap
        BasicNetwork network = new BasicNetwork(new HurlStack());

        mRequestQueue = new RequestQueue(cache, network);
        mRequestQueue.start();
	}

	public void login() {
		Runnable r = new Runnable() {
			public void run() {
				String response = getRequest("dbInterface/api/login");
				try {
					JSONObject jObject = new JSONObject(response);
					application.setLoggedIn(true);
					application.setFeedFragment();

                    userId = jObject.getLong("userId");

                    JSONObject jUser = jObject.getJSONObject("user");
                    loginGivenName = jUser.getString("givenName");
                    loginFamilyName = jUser.getString("familyName");
                    loginEmail = jUser.getString("email");

				} catch (JSONException e) {
					e.printStackTrace();
					application.getMainActivity().runOnUiThread(new Runnable() {
						public void run() {
							application.loginFailed();
						}
					});
				}
			}
		};

		Thread t = new Thread(r);
		t.start();
	}

	public void signup(final String firstName, final String lastName, final String gender, final String country, final String yearOfBirth, final String email, final String password, String confirmPassword, final String institution) {
       Runnable r = new Runnable() {
    	   public void run() {
    		   StringBuffer stringBuffer = new StringBuffer();
    	        BufferedReader bufferedReader = null;

    	        try {
    	            HttpClient httpClient = new DefaultHttpClient();
                    HttpParams httpParameters = httpClient.getParams();
                    HttpConnectionParams.setTcpNoDelay(httpParameters, true);
    	            HttpPost httpPost = new HttpPost();

    	            URI uri = new URI(SERVER_URL + "dbInterface/api/signUp");
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
    					application.getMainActivity().runOnUiThread(new Runnable() {
    						public void run() {
    							application.signupSucceded();
    						}
    					});
    	            }
    	            else {
    					application.getMainActivity().runOnUiThread(new Runnable() {
    						public void run() {
    							application.signupFailed();
    						}
    					});
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
    	   }
       };

       Thread t = new Thread(r);
       t.start();
	}

	public void getGeneralInfo(final UserSignupFragment userSignupFragment) {
		this.userSignupFragment = userSignupFragment;

		Runnable r = new Runnable() {
			public void run() {
				String response = getRequestNoAuth("dbInterface/api/general-info");
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
			}
		};

		Thread t = new Thread(r);
		t.start();
	}


	public void getNewsArticles(final NewsFragment newsFragment) {
		this.newsFragment = newsFragment;
        String url = SERVER_URL + "dbInterface/api/newsArticles/26";

        JsonObjectRequest req = new JsonObjectRequest(url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray jArray = response.getJSONArray("articles");

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

                                    String likers = null;
                                    long date = currentArticle.getLong("date");
                                    date = TimeConverter.convertTimestampTimezone(date);
                                    String creatorEmail = "";
                                    String studentYear = "";
                                    String link = null;

                                    newsFragment.addNewsArticle(articleId, institutionId, title, content, picture, likers, date, creatorEmail, studentYear, link);
                                } catch (JSONException e1) {
                                    e1.printStackTrace();
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Volley", error.toString());
            }
        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                String credentials = username + ":" + password;
                String base64EncodedCredentials =
                        Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
                headers.put("Authorization", "Basic " + base64EncodedCredentials);
                headers.put("Accept", "application/json");
                headers.put("Content-type", "application/json");
                return headers;
            }
        };

        int socketTimeout = 30000;//30 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
	}

	public void getNewsArticle(final NewsArticleFragment newsArticleFragment, final int articleId) {
		this.newsArticleFragment = newsArticleFragment;

		Runnable r = new Runnable() {
			public void run() {
				String response = getRequest("dbInterface/api/newsArticle/" + String.valueOf(articleId));
				try {
					JSONObject currentArticle = new JSONObject(response);

					int articleId = currentArticle.getInt("articleId");
					int institutionId = currentArticle.getInt("institutionId");
					String title = currentArticle.getString("title");
					String likes = currentArticle.getString("likes");
					String content = currentArticle.getString("content");
                    JSONArray photos = currentArticle.getJSONArray("photos");
                    String picture = photos.getJSONObject(0).getString("url");
					String email = currentArticle.getString("creatorEmail");
					picture = picture.replace("[", "").replace("]", "").replace("\"", "").replace("\\", "");
					Bitmap bitmapPicture = getPicture(picture);

					String likers = currentArticle.getString("likes");
					long date = currentArticle.getLong("date");
					date = TimeConverter.convertTimestampTimezone(date);
					String creatorEmail = "";
					String studentYear = "";
					String link = null;

					newsArticleFragment.populateContent(title, content, likes, date, bitmapPicture, email);
					getUser(newsArticleFragment, email);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		};

		Thread t = new Thread(r);
		t.start();
	}

	public void getUser(final NewsArticleFragment newsArticleFragment, final String userEmail) {
		this.newsArticleFragment = newsArticleFragment;

		Runnable r = new Runnable() {
			public void run() {
				String response = getRequest("dbInterface/api/user/" + userEmail.replace("@", "%40") +"/");
				try {
					JSONObject currentUser = new JSONObject(response);

					String givenName = currentUser.getString("givenName");
					String familyName = currentUser.getString("familyName");
					String gender = currentUser.getString("gender");
					String name = givenName + " " + familyName;
					Bitmap bitmapPicture = null;

					newsArticleFragment.populateUserContent(name, bitmapPicture);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		};

		Thread t = new Thread(r);
		t.start();
	}

    public void getUserDP(ImageView imageView, LinearLayout backgroundLinearLayout) {
        this.getFirstPhoto(0, (int) userId, imageView);
        this.getFirstPhotoBlur(0, (int) userId, backgroundLinearLayout);
    }

    public void getUserDP(int profileId, ImageView imageView, LinearLayout backgroundLinearLayout) {
        this.getFirstPhoto(0, (int) profileId, imageView);
        this.getFirstPhotoBlur(0, (int) profileId, backgroundLinearLayout);
    }

    public NetworkResponse getElections() {
		NetworkResponse networkResponse = null;

		return networkResponse;
	}

	public NetworkResponse getPolls() {
		NetworkResponse networkResponse = null;

		return networkResponse;
	}

    public void findFriends(final FindAddContactFragment findAddContactFragment) {
        this.findAddContactFragment = findAddContactFragment;
        String url = SERVER_URL + "dbInterface/api/contacts/" + String.valueOf("greg.newitt@unimelb.edu.au") + "/";

        JsonArrayRequest req = new JsonArrayRequest(url, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray friends) {
                try {
                    for(int i=0; i<friends.length(); i++) {
                        JSONObject response = friends.getJSONObject(i);

                        String givenName = response.getString("givenName");
                        String familyName = response.getString("familyName");
                        String gender = response.getString("gender");
                        String email = response.getString("email");
                        String nationality = response.getString("nationality");

                        findAddContactFragment.addFriend(givenName, familyName, email, "The University of Melbourne");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Volley", error.toString());
            }
        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                String credentials = username + ":" + password;
                String base64EncodedCredentials =
                        Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
                headers.put("Authorization", "Basic " + base64EncodedCredentials);
                headers.put("Accept", "application/json");
                headers.put("Content-type", "application/json");
                return headers;
            }
        };

        int socketTimeout = 30000;//30 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

	public void findContacts(String query, final FindAddContactFragment findAddContactFragment) {
        this.findAddContactFragment = findAddContactFragment;
        String url = SERVER_URL + "dbInterface/api/contact/" + String.valueOf("greg.newitt@unimelb.edu.au") + "/";

        JsonObjectRequest req = new JsonObjectRequest(url, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    String givenName = response.getString("givenName");
                    String familyName = response.getString("familyName");
                    String gender = response.getString("gender");
                    String email = response.getString("email");
                    String nationality = response.getString("nationality");

                    findAddContactFragment.addUser(givenName, familyName, email, "The University of Melbourne");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Volley", error.toString());
            }
        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                String credentials = username + ":" + password;
                String base64EncodedCredentials =
                        Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
                headers.put("Authorization", "Basic " + base64EncodedCredentials);
                headers.put("Accept", "application/json");
                headers.put("Content-type", "application/json");
                return headers;
            }
        };

        int socketTimeout = 30000;//30 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
	}

    public void addFriend(String email, final FindAddContactFragment findAddContactFragment) {
        this.findAddContactFragment = findAddContactFragment;
        String url = SERVER_URL + "dbInterface/api/user/" + String.valueOf(userId) + "/contact/" + String.valueOf(email) + "/";

        HashMap<String, Integer> params = new HashMap<String, Integer>();

        JsonObjectRequest req = new JsonObjectRequest(Request.Method.PUT, url, new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            findAddContactFragment.showAddedDialog();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Volley", error.toString());
            }
        }) {

        @Override
        public Map<String, String> getHeaders() throws AuthFailureError {
            HashMap<String, String> headers = new HashMap<String, String>();
            String credentials = username + ":" + password;
            String base64EncodedCredentials =
                        Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
            headers.put("Authorization", "Basic " + base64EncodedCredentials);
            headers.put("Accept", "application/json");
            headers.put("Content-type", "application/json; charset=utf-8");

            return headers;
            }
        };

        int socketTimeout = 30000;//30 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);

        Log.d("VolleyRequest", req.toString());
    }

    public void addVoteReminder(String location, long date) {
        this.findAddContactFragment = findAddContactFragment;
        String url = SERVER_URL + "dbInterface/api/user/" + String.valueOf(userId) + "/voteReminder";

        HashMap<String, String> params = new HashMap<String, String>();
        params.put("location", location);
        params.put("date", String.valueOf(date));
        params.put("electionId", String.valueOf(electionId));

        JsonObjectRequest req = new JsonObjectRequest(Request.Method.PUT, url, new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Volley", error.toString());
            }
        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                String credentials = username + ":" + password;
                String base64EncodedCredentials =
                        Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
                headers.put("Authorization", "Basic " + base64EncodedCredentials);
                headers.put("Accept", "application/json");
                headers.put("Content-type", "application/json; charset=utf-8");

                return headers;
            }
        };

        int socketTimeout = 30000;//30 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);

        Log.d("VolleyRequest", req.toString());
    }

	public void getEvents(final EventsFragment eventsFragment) {
        String url = SERVER_URL + "dbInterface/api/events/26/";

        JsonObjectRequest req = new JsonObjectRequest(url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jObject) {
                            try {
                                JSONArray jArray = jObject.getJSONArray("events");

                                for (int i=0; i<jArray.length(); i++) {
                                    JSONObject currentEvent = jArray.getJSONObject(i);
                                    int eventId = currentEvent.getInt("eventId");
                                    int institutionId = currentEvent.getInt("institutionId");
                                    String name = currentEvent.getString("name");
                                    String description = currentEvent.getString("description");
                                    JSONArray photos = currentEvent.getJSONArray("photos");
                                    String picture = photos.getJSONObject(0).getString("url");
                                    picture = picture.replace("[", "").replace("]", "").replace("\"", "").replace("\\", "");
                                    Bitmap bitmapPicture;

                                    String likers = null;
                                    long date = 10000;
                                    if(currentEvent.has("created") && !currentEvent.isNull("created")) {
                                        date = currentEvent.getLong("created");
                                        date = TimeConverter.convertTimestampTimezone(date);
                                    }

                                    String creatorEmail = "";
                                    String studentYear = "";
                                    String link = null;

                                    try {
                                        eventsFragment.addEvent(eventId, name, date, picture);
                                    } catch(Exception e) {

                                    }
                                }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Volley", error.toString());
            }
        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                String credentials = username + ":" + password;
                String base64EncodedCredentials =
                        Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
                headers.put("Authorization", "Basic " + base64EncodedCredentials);
                headers.put("Accept", "application/json");
                headers.put("Content-type", "application/json");
                return headers;
            }
        };

        int socketTimeout = 30000;//30 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
	}

	public void getEventDetails(final EventsDetailFragment eventsDetailFragment, final int eventId) {
		this.eventDetailFragment = eventsDetailFragment;

		Runnable r = new Runnable() {
			public void run() {
				String response = getRequest("dbInterface/api/event/" + String.valueOf(eventId));
				try {
					JSONObject currentEvent = new JSONObject(response);

					int eventId = currentEvent.getInt("eventId");
					int institutionId = currentEvent.getInt("institutionId");
					String name = currentEvent.getString("name");
					String location = currentEvent.getString("location");
					String description = currentEvent.getString("description");
                    JSONArray photos = currentEvent.getJSONArray("photos");
                    String picture = photos.getJSONObject(0).getString("url");
					picture = picture.replace("[", "").replace("]", "").replace("\"", "").replace("\\", "");
					Bitmap bitmapPicture;

					if(picture == "") {
						bitmapPicture = null;
					}
					else {
						bitmapPicture = getPicture(picture);
					}

					String likers = null;
					long date = 10000;
					if(currentEvent.has("created") && !currentEvent.isNull("created")) {
						date = currentEvent.getLong("created");
						date = TimeConverter.convertTimestampTimezone(date);
					}
					String organizerEmail = currentEvent.getString("organizerEmail");
                    findContact(organizerEmail, eventsDetailFragment);
					String studentYear = "";
					String link = null;

					eventsDetailFragment.populateContent(name, description, location, "0", bitmapPicture, date);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		};

		Thread t = new Thread(r);
		t.start();
	}

    public void findContact(final String email, final EventsDetailFragment eventsDetailFragment) {
        this.eventsDetailFragment = eventsDetailFragment;
        String url = SERVER_URL + "dbInterface/api/contact/" + String.valueOf("greg.newitt@unimelb.edu.au") + "/";

        JsonObjectRequest req = new JsonObjectRequest(url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jObject) {
                        try {


                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Volley", error.toString());
                eventsDetailFragment.addCandidate(email);
            }
        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                String credentials = username + ":" + password;
                String base64EncodedCredentials =
                        Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
                headers.put("Authorization", "Basic " + base64EncodedCredentials);
                headers.put("Accept", "application/json");
                headers.put("Content-type", "application/json");
                return headers;
            }
        };

        int socketTimeout = 30000;//30 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

	public void getPhotoAlbums(final PhotosFragment photosFragment) {
		this.photosFragment = photosFragment;
        String url = SERVER_URL + "dbInterface/api/photoAlbums/7449";

        JsonObjectRequest req = new JsonObjectRequest(url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jObject) {
                        try {
                            JSONArray jArray = jObject.getJSONArray("photoAlbums");

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
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Volley", error.toString());
            }
        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                String credentials = username + ":" + password;
                String base64EncodedCredentials =
                        Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
                headers.put("Authorization", "Basic " + base64EncodedCredentials);
                headers.put("Accept", "application/json");
                headers.put("Content-type", "application/json");
                return headers;
            }
        };

        int socketTimeout = 30000;//30 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
	}

	public void getPhotoAlbum(final PhotoAlbumFragment photoAlbumFragment, final String albumId) {
		this.photoAlbumFragment = photoAlbumFragment;
        String url = SERVER_URL + "dbInterface/api/photos/" + String.valueOf(albumId) + "?pageSize=250";

        JsonObjectRequest req = new JsonObjectRequest(url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jObject) {
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
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Volley", error.toString());
            }
        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                String credentials = username + ":" + password;
                String base64EncodedCredentials =
                        Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
                headers.put("Authorization", "Basic " + base64EncodedCredentials);
                headers.put("Accept", "application/json");
                headers.put("Content-type", "application/json");
                return headers;
            }
        };

        int socketTimeout = 30000;//30 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
	}

    public void getProfileBadges(final ProfileBadgesFragment profileBadgesFragment) {
        this.profileBadgesFragment = profileBadgesFragment;
        String url = SERVER_URL + "dbInterface/api/badges/26";

        JsonObjectRequest req = new JsonObjectRequest(url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jObject) {
                        try {
                            JSONArray jArray = jObject.getJSONArray("badges");

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
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Volley", error.toString());
            }
        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                String credentials = username + ":" + password;
                String base64EncodedCredentials =
                        Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
                headers.put("Authorization", "Basic " + base64EncodedCredentials);
                headers.put("Accept", "application/json");
                headers.put("Content-type", "application/json");
                return headers;
            }
        };

        int socketTimeout = 30000;//30 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

	public void getPhoto(final PhotoViewFragment photoViewFragment, final int photoId) {
		this.photoViewFragment = photoViewFragment;
        String url = SERVER_URL + "dbInterface/api/photo/" + String.valueOf(photoId);

        JsonObjectRequest req = new JsonObjectRequest(url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            int nodeId = response.getInt("nodeId");
                            String title = response.getString("title");
                            String description = response.getString("description");
                            long date = response.getLong("date");
                            date = TimeConverter.convertTimestampTimezone(date);
                            String url = response.getString("url");

                            photoViewFragment.setData(title, date);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Volley", error.toString());
            }
        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                String credentials = username + ":" + password;
                String base64EncodedCredentials =
                        Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
                headers.put("Authorization", "Basic " + base64EncodedCredentials);
                headers.put("Accept", "application/json");
                headers.put("Content-type", "application/json");
                return headers;
            }
        };

        int socketTimeout = 30000;//30 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
	}
	
	public void getVoteRecords(final VoteFragment voteFragment) {
		this.voteFragment = voteFragment;

		Runnable r = new Runnable() {
			public void run() {
				String response = getRequest("dbInterface/api/voteRecord");
				try {
					JSONObject jObject = new JSONObject(response);
					JSONArray jArray = jObject.getJSONArray("photoAlbums");
					
					for (int i=0; i<jArray.length(); i++) {
						JSONObject currentVoteRecord = jArray.getJSONObject(i);
						
						int nodeId = currentVoteRecord.getInt("nodeId");
						String name = currentVoteRecord.getString("name");
						String description = currentVoteRecord.getString("description");
						//String responseBitmap = getRequest("dbInterface/api/photos/" + String.valueOf(nodeId));
						//JSONObject responseJSON = new JSONObject(responseBitmap);
						//String pictureURL = responseJSON.getString("url");
						
						//Bitmap bitmapPicture;
						//bitmapPicture = getPicture();
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		};
		
		Thread t = new Thread(r);
		t.start();
	}
	
	public void getPollQuestions(final PollFragment pollFragment) {
		this.pollFragment = pollFragment;

        String url = SERVER_URL + "dbInterface/api/polls/26";

        JsonObjectRequest req = new JsonObjectRequest(url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray jArray = response.getJSONArray("polls");

                            for (int i=0; i<jArray.length(); i++) {
                                JSONObject currentAlbum = jArray.getJSONObject(i);

                                int nodeId = currentAlbum.getInt("nodeId");
                                int creatorID = currentAlbum.getInt("creatorId");
                                String question = currentAlbum.getString("question");
                                String answers = currentAlbum.getString("answers");

                                pollFragment.addQuestion(nodeId, creatorID, question, answers);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Volley", error.toString());
            }
        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                String credentials = username + ":" + password;
                String base64EncodedCredentials =
                        Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
                headers.put("Authorization", "Basic " + base64EncodedCredentials);
                headers.put("Accept", "application/json");
                headers.put("Content-type", "application/json");
                return headers;
            }
        };

        int socketTimeout = 30000;//30 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
	}

    public void getPollResults(final int nodeId, final PollVoteFragment pollVoteFragment) {
        this.pollFragment = pollFragment;
        String url = SERVER_URL + "dbInterface/api/poll/" + String.valueOf(nodeId) + "/results";

        JsonObjectRequest req = new JsonObjectRequest(url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
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
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Volley", error.toString());
            }
        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                String credentials = username + ":" + password;
                String base64EncodedCredentials =
                        Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
                headers.put("Authorization", "Basic " + base64EncodedCredentials);
                headers.put("Accept", "application/json");
                headers.put("Content-type", "application/json");
                return headers;
            }
        };

        int socketTimeout = 30000;//30 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

    public void getPollComments(final int pollId, final PollVoteFragment pollVoteFragment) {
        this.pollVoteFragment = pollVoteFragment;
        String url = SERVER_URL + "dbInterface/api/comments/" + String.valueOf(pollId) + "/";

        JsonArrayRequest req = new JsonArrayRequest(url, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                try {
                    for(int i=0; i<response.length(); i++) {
                        int targetId = response.getJSONObject(i).getInt("targetId");
                        int commentId = response.getJSONObject(i).getInt("commentId");
                        String userName = response.getJSONObject(i).getString("userName");
                        String userEmail = response.getJSONObject(i).getString("userEmail");
                        String content = response.getJSONObject(i).getString("content");

                        pollVoteFragment.addTableComment(userName, content);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Volley", error.toString());
            }
        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                String credentials = username + ":" + password;
                String base64EncodedCredentials =
                        Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
                headers.put("Authorization", "Basic " + base64EncodedCredentials);
                headers.put("Accept", "application/json");
                headers.put("Content-type", "application/json");
                return headers;
            }
        };

        int socketTimeout = 30000;//30 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

    public void postPollComment(final int pollId, final PollFragment pollFragment) {
        this.pollFragment = pollFragment;
        String url = SERVER_URL + "dbInterface/api/comment";

        HashMap<String, String> params = new HashMap<String, String>();
        params.put("targetId", String.valueOf(pollId));
        params.put("userName", (this.loginGivenName + " " + this.loginFamilyName));
        params.put("userEmail", this.loginEmail);
        params.put("content", (this.pollFragment.getCommentsField().getText().toString()));

        JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, url, new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String test = "";
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Volley", error.toString());
            }
        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                String credentials = username + ":" + password;
                String base64EncodedCredentials =
                        Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
                headers.put("Authorization", "Basic " + base64EncodedCredentials);
                headers.put("Accept", "application/json");
                headers.put("Content-type", "application/json; charset=utf-8");
                return headers;
            }
        };

        int socketTimeout = 30000;//30 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);

        Log.d("VolleyRequest", req.toString());
    }

    public void answerPoll(int pollId, int answerIndex, final PollVoteFragment pollVoteFragment) {
        this.pollFragment = pollFragment;
        String url = SERVER_URL + "dbInterface/api/poll/" + String.valueOf(pollId) + "/answer";

        HashMap<String, Integer> params = new HashMap<String, Integer>();
            params.put("answerIndex", answerIndex);
            params.put("answererId", (int) userId);
            params.put("pollId", pollId);

        JsonObjectRequest req = new JsonObjectRequest(Request.Method.PUT, url, new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            getPollResults(pollVoteFragment.getNodeId(), pollVoteFragment);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Volley", error.toString());
            }
        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                String credentials = username + ":" + password;
                String base64EncodedCredentials =
                        Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
                headers.put("Authorization", "Basic " + base64EncodedCredentials);
                headers.put("Accept", "application/json");
                headers.put("Content-type", "application/json; charset=utf-8");
                return headers;
            }
        };

        int socketTimeout = 30000;//30 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);

        Log.d("VolleyRequest", req.toString());
    }

    public void getVoteLocations(final VoteFragment voteFragment) {
        this.pollFragment = pollFragment;

        String url = SERVER_URL + "dbInterface/api/votingLocations/26";
        JsonArrayRequest req = new JsonArrayRequest(url, new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                           for(int i=0; i<response.length(); i++) {
                               String ownerId = response.getJSONObject(i).getString("ownerId");
                               String votingLocationId = response.getJSONObject(i).getString("votingLocationId");
                               String name = response.getJSONObject(i).getString("name");
                               String information = response.getJSONObject(i).getString("information");

                               voteFragment.addVoteLocations(ownerId, votingLocationId,
                                       name, information);
                           }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Volley", error.toString());
            }
        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                String credentials = username + ":" + password;
                String base64EncodedCredentials =
                        Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
                headers.put("Authorization", "Basic " + base64EncodedCredentials);
                headers.put("Accept", "application/json");
                headers.put("Content-type", "application/json");
                return headers;
            }
        };

        int socketTimeout = 30000;//30 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

    public void getVoteLocation(final VoteFragment voteFragment, final String pos) {
        this.pollFragment = pollFragment;

        String url = SERVER_URL + "dbInterface/api/votingLocation/" + pos;
        JsonObjectRequest req = new JsonObjectRequest(url, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    for(int i=0; i<response.length(); i++) {

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Volley", error.toString());
            }
        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                String credentials = username + ":" + password;
                String base64EncodedCredentials =
                        Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
                headers.put("Authorization", "Basic " + base64EncodedCredentials);
                headers.put("Accept", "application/json");
                headers.put("Content-type", "application/json");
                return headers;
            }
        };

        int socketTimeout = 30000;//30 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

    public void getLatestElection(final ElectionOverviewFragment electionOverviewFragment) {
        this.electionOverviewFragment = electionOverviewFragment;
        String url = SERVER_URL + "dbInterface/api/elections/26";

        JsonArrayRequest req = new JsonArrayRequest(url, new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            JSONObject electionObject = response.getJSONObject(0);

                            int electionId = electionObject.getInt("electionId");
                            network.electionId = electionId;

                            String title = electionObject.getString("title");
                            String introduction = electionObject.getString("introduction");
                            String date = "";
                            String process = electionObject.getString("process");

                            electionOverviewFragment.updateEntities(electionId, title, introduction, date, process);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Volley", error.toString());
            }
        }) {

        @Override
        public Map<String, String> getHeaders() throws AuthFailureError {
            HashMap<String, String> headers = new HashMap<String, String>();
            String credentials = username + ":" + password;
            String base64EncodedCredentials = Base64.encodeToString(credentials.getBytes(),
                        Base64.NO_WRAP);
            headers.put("Authorization", "Basic " + base64EncodedCredentials);
            headers.put("Accept", "application/json");
            headers.put("Content-type", "application/json");

            return headers;
        }
    };

        int socketTimeout = 30000;//30 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

    public void getLatestElection(final VoteFragment voteFragment) {
        this.voteFragment = voteFragment;
        String url = SERVER_URL + "dbInterface/api/elections/26";

        JsonArrayRequest req = new JsonArrayRequest(url, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                try {
                    JSONObject electionObject = response.getJSONObject(0);

                    int electionId = electionObject.getInt("electionId");
                    network.electionId = electionId;

                    long startVoting = electionObject.getLong("startVoting");
                    long endVoting = electionObject.getLong("endVoting");

                    voteFragment.datePicker.setMinDate(startVoting);
                    voteFragment.datePicker.setMaxDate(endVoting);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Volley", error.toString());
            }
        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                String credentials = username + ":" + password;
                String base64EncodedCredentials = Base64.encodeToString(credentials.getBytes(),
                        Base64.NO_WRAP);
                headers.put("Authorization", "Basic " + base64EncodedCredentials);
                headers.put("Accept", "application/json");
                headers.put("Content-type", "application/json");

                return headers;
            }
        };

        int socketTimeout = 30000;//30 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

    public void getCandidates(final CandidatePositionsFragment candidatePositionsFragment) {
        this.candidatePositionsFragment = candidatePositionsFragment;
        String url = SERVER_URL + "dbInterface/api/candidates/26";

        JsonArrayRequest req = new JsonArrayRequest(url, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                try {
                    JSONObject candidateObject = response.getJSONObject(0);


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Volley", error.toString());
            }
        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                String credentials = username + ":" + password;
                String base64EncodedCredentials = Base64.encodeToString(credentials.getBytes(),
                        Base64.NO_WRAP);
                headers.put("Authorization", "Basic " + base64EncodedCredentials);
                headers.put("Accept", "application/json");
                headers.put("Content-type", "application/json");

                return headers;
            }
        };

        int socketTimeout = 30000;//30 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

    public void getCandidates(final CandidateAllFragment candidateAllFragment) {
        this.candidateAllFragment = candidateAllFragment;
        String url = SERVER_URL + "dbInterface/api/candidates/" + String.valueOf(electionId);

        JsonArrayRequest req = new JsonArrayRequest(url, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                try {
                    for(int i=0; i<response.length(); i++) {
                        JSONObject candidateObject = response.getJSONObject(i);

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
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Volley", error.toString());
            }
        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                String credentials = username + ":" + password;
                String base64EncodedCredentials = Base64.encodeToString(credentials.getBytes(),
                        Base64.NO_WRAP);
                headers.put("Authorization", "Basic " + base64EncodedCredentials);
                headers.put("Accept", "application/json");
                headers.put("Content-type", "application/json");

                return headers;
            }
        };

        int socketTimeout = 30000;//30 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

    public void getCandidatesPosition(final CandidatePositionFragment candidatePositionFragment, final int selectedPositionId) {
        this.candidatePositionFragment = candidatePositionFragment;
        String url = SERVER_URL + "dbInterface/api/candidates/" + String.valueOf(electionId);

        JsonArrayRequest req = new JsonArrayRequest(url, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                try {
                    for(int i=0; i<response.length(); i++) {
                        JSONObject candidateObject = response.getJSONObject(i);

                        int candidateId = candidateObject.getInt("candidateId");
                        int ticketId = candidateObject.getInt("ticketId");
                        int positionId = candidateObject.getInt("positionId");
                        //if(selectedPositionId != positionId) {
                        //    continue;
                        //}

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
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Volley", error.toString());
            }
        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                String credentials = username + ":" + password;
                String base64EncodedCredentials = Base64.encodeToString(credentials.getBytes(),
                        Base64.NO_WRAP);
                headers.put("Authorization", "Basic " + base64EncodedCredentials);
                headers.put("Accept", "application/json");
                headers.put("Content-type", "application/json");

                return headers;
            }
        };

        int socketTimeout = 30000;//30 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

    public void getTicketLabel(final TextView textView, final int tickedId) {
        String url = SERVER_URL + "dbInterface/api/ticket/" + String.valueOf(tickedId);

        JsonObjectRequest req = new JsonObjectRequest(url, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    String colour = response.getString("colour");
                    String code = response.getString("code");

                    textView.setText(code);
                    textView.setBackgroundColor(Color.parseColor(colour));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Volley", error.toString());
            }
        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                String credentials = username + ":" + password;
                String base64EncodedCredentials = Base64.encodeToString(credentials.getBytes(),
                        Base64.NO_WRAP);
                headers.put("Authorization", "Basic " + base64EncodedCredentials);
                headers.put("Accept", "application/json");
                headers.put("Content-type", "application/json");

                return headers;
            }
        };

        int socketTimeout = 30000;//30 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

    public void getTickets(final CandidateTicketFragment candidateTicketFragment) {
        this.candidateTicketFragment = candidateTicketFragment;
        String url = SERVER_URL + "dbInterface/api/tickets/" + String.valueOf(electionId);

        JsonArrayRequest req = new JsonArrayRequest(url, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                try {
                    for(int i=0; i<response.length(); i++) {
                        JSONObject ticketObject = response.getJSONObject(i);

                        int ticketId = ticketObject.getInt("ticketId");
                        String name = ticketObject.getString("name");
                        String numberOfSupporters = ticketObject.getString("numberOfSupporters");
                        String information = ticketObject.getString("information");
                        String logo = ticketObject.getString("logo");
                        String colour = ticketObject.getString("colour");

                        candidateTicketFragment.addTicket(ticketId, name, information,
                                numberOfSupporters, colour);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Volley", error.toString());
            }
        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                String credentials = username + ":" + password;
                String base64EncodedCredentials = Base64.encodeToString(credentials.getBytes(),
                        Base64.NO_WRAP);
                headers.put("Authorization", "Basic " + base64EncodedCredentials);
                headers.put("Accept", "application/json");
                headers.put("Content-type", "application/json");

                return headers;
            }
        };

        int socketTimeout = 30000;//30 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

    public void getTicketDetail(final CandidateTicketDetailFragment candidateTicketDetailFragment,
                                int ticketId) {
        this.candidateTicketFragment = candidateTicketFragment;
        String url = SERVER_URL + "dbInterface/api/ticket/" + String.valueOf(ticketId);

        JsonObjectRequest req = new JsonObjectRequest(url, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    int ticketId = response.getInt("ticketId");
                    int electionId = response.getInt("electionId");
                    String name = response.getString("name");
                    String code = response.getString("code");
                    String colour = response.getString("colour");
                    String information = response.getString("information");
                    int numberOfSupporters = response.getInt("numberOfSupporters");

                    candidateTicketDetailFragment.updateInformation(ticketId, electionId, name,
                            code, information, numberOfSupporters, colour);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Volley", error.toString());
            }
        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                String credentials = username + ":" + password;
                String base64EncodedCredentials = Base64.encodeToString(credentials.getBytes(),
                        Base64.NO_WRAP);
                headers.put("Authorization", "Basic " + base64EncodedCredentials);
                headers.put("Accept", "application/json");
                headers.put("Content-type", "application/json");

                return headers;
            }
        };

        int socketTimeout = 30000;//30 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

    public void getTicketCandidates(final CandidateTicketDetailFragment candidateTicketDetailFragment, final int selectedTickedId) {
        this.candidateTicketDetailFragment = candidateTicketDetailFragment;
        String url = SERVER_URL + "dbInterface/api/candidates/" + String.valueOf(electionId);

        JsonArrayRequest req = new JsonArrayRequest(url, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                try {
                    for(int i=0; i<response.length(); i++) {
                        JSONObject candidateObject = response.getJSONObject(i);

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
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Volley", error.toString());
            }
        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                String credentials = username + ":" + password;
                String base64EncodedCredentials = Base64.encodeToString(credentials.getBytes(),
                        Base64.NO_WRAP);
                headers.put("Authorization", "Basic " + base64EncodedCredentials);
                headers.put("Accept", "application/json");
                headers.put("Content-type", "application/json");

                return headers;
            }
        };

        int socketTimeout = 30000;//30 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

    public void getPositionText(final TextView positionTextView, int positionId) {
        String url = SERVER_URL + "dbInterface/api/position/" + String.valueOf(positionId);

        JsonObjectRequest req = new JsonObjectRequest(url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String name = response.getString("name");
                            String description = response.getString("description");

                            positionTextView.setText(name);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Volley", error.toString());
            }
        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                String credentials = username + ":" + password;
                String base64EncodedCredentials =
                        Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
                headers.put("Authorization", "Basic " + base64EncodedCredentials);
                headers.put("Accept", "application/json");
                headers.put("Content-type", "application/json");
                return headers;
            }
        };

        int socketTimeout = 30000;//30 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

    public void getPositions(final CandidatePositionsFragment candidatePositionsFragment) {
        this.candidatePositionsFragment = candidatePositionsFragment;
        String url = SERVER_URL + "dbInterface/api/positions/" + String.valueOf(electionId);

        JsonArrayRequest req = new JsonArrayRequest(url, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                try {
                    for(int i=0; i<response.length(); i++) {
                        JSONObject positionObject = response.getJSONObject(i);

                        int electionId = positionObject.getInt("electionId");
                        int positionId = positionObject.getInt("positionId");
                        String name = positionObject.getString("name");
                        String desc = positionObject.getString("description");

                        candidatePositionsFragment.addPosition(electionId, positionId, name, desc);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Volley", error.toString());
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                String credentials = username + ":" + password;
                String base64EncodedCredentials = Base64.encodeToString(credentials.getBytes(),
                        Base64.NO_WRAP);
                headers.put("Authorization", "Basic " + base64EncodedCredentials);
                headers.put("Accept", "application/json");
                headers.put("Content-type", "application/json");

                return headers;
            }
        };

        int socketTimeout = 30000;//30 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

    public void getUserFullName(int userId, final TextView textView, final String params) {
        String url = SERVER_URL + "dbInterface/api/user/" + String.valueOf(userId) + "/";

        JsonObjectRequest req = new JsonObjectRequest(url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String fullName = response.getString("givenName") + " " +
                                    response.getString("familyName");

                            textView.setText(params + fullName);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Volley", error.toString());
            }
        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                String credentials = username + ":" + password;
                String base64EncodedCredentials =
                        Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
                headers.put("Authorization", "Basic " + base64EncodedCredentials);
                headers.put("Accept", "application/json");
                headers.put("Content-type", "application/json");
                return headers;
            }
        };

        int socketTimeout = 30000;//30 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

    public void getTasks(final ProfileFragment profileFragment) {
        String url = SERVER_URL + "dbInterface/api/tasks/";

        JsonArrayRequest req = new JsonArrayRequest(url, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                try {
                    for(int i=0; i<response.length(); i++) {
                        JSONObject taskObject = response.getJSONObject(i);

                        long taskId = taskObject.getLong("taskId");
                        String action = taskObject.getString("action");
                        long xpValue = taskObject.getLong("xpValue");

                        profileFragment.addTask(taskId, action, xpValue);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Volley", error.toString());
            }
        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                String credentials = username + ":" + password;
                String base64EncodedCredentials = Base64.encodeToString(credentials.getBytes(),
                        Base64.NO_WRAP);
                headers.put("Authorization", "Basic " + base64EncodedCredentials);
                headers.put("Accept", "application/json");
                headers.put("Content-type", "application/json");

                return headers;
            }
        };

        int socketTimeout = 30000;//30 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

    public void getTasks(final ContactProfileFragment contactProfileFragment) {
        String url = SERVER_URL + "dbInterface/api/tasks/";

        JsonArrayRequest req = new JsonArrayRequest(url, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                try {
                    for(int i=0; i<response.length(); i++) {
                        JSONObject taskObject = response.getJSONObject(i);

                        long taskId = taskObject.getLong("taskId");
                        String action = taskObject.getString("action");
                        long xpValue = taskObject.getLong("xpValue");

                        contactProfileFragment.addTask(taskId, action, xpValue);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Volley", error.toString());
            }
        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                String credentials = username + ":" + password;
                String base64EncodedCredentials = Base64.encodeToString(credentials.getBytes(),
                        Base64.NO_WRAP);
                headers.put("Authorization", "Basic " + base64EncodedCredentials);
                headers.put("Accept", "application/json");
                headers.put("Content-type", "application/json");

                return headers;
            }
        };

        int socketTimeout = 30000;//30 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

    public void getRemainingTasks(final TaskDetailProgressFragment taskDetailProgressFragment) {
        this.taskDetailProgressFragment = taskDetailProgressFragment;
        String url = SERVER_URL + "dbInterface/api/tasks/";

        JsonArrayRequest req = new JsonArrayRequest(url, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                try {
                    for(int i=0; i<response.length(); i++) {
                        JSONObject taskObject = response.getJSONObject(i);

                        long taskId = taskObject.getLong("taskId");
                        String action = taskObject.getString("action");
                        long xpValue = taskObject.getLong("xpValue");

                        taskDetailProgressFragment.addRemainingTask(taskId, action, xpValue);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Volley", error.toString());
            }
        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                String credentials = username + ":" + password;
                String base64EncodedCredentials = Base64.encodeToString(credentials.getBytes(),
                        Base64.NO_WRAP);
                headers.put("Authorization", "Basic " + base64EncodedCredentials);
                headers.put("Accept", "application/json");
                headers.put("Content-type", "application/json");

                return headers;
            }
        };

        int socketTimeout = 30000;//30 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }
	
	public void likeNewsArticle(final NewsArticleFragment newsArticleFragment) {
		this.newsArticleFragment = newsArticleFragment;

		Runnable r = new Runnable() {
			public void run() {
				try {
					String response = getRequest("dbInterface/api/newsArticle/"+String.valueOf(newsArticleFragment.getArticleId()) + "/likedBy/" + email + "/");

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		
		Thread t = new Thread(r);
		t.start();		
	}
	
	public NetworkResponse getForums() {
		NetworkResponse networkResponse = null;
		
		return networkResponse;
	}
	
	public NetworkResponse getCandidates() {
		NetworkResponse networkResponse = null;
		
		return networkResponse;
	}

	public NetworkResponse getTickets() {
		NetworkResponse networkResponse = null;
		
		return networkResponse;
	}

	public NetworkResponse getPositions() {
		NetworkResponse networkResponse = null;
		
		return networkResponse;
	}
	
	
	
	public String getRequest(String params) {
        StringBuffer stringBuffer = new StringBuffer();
        BufferedReader bufferedReader = null;
        
        try {
            HttpClient httpClient = new DefaultHttpClient();
            HttpParams httpParameters = httpClient.getParams();
            HttpConnectionParams.setTcpNoDelay(httpParameters, true);
            HttpGet httpGet = new HttpGet();

            URI uri = new URI(SERVER_URL + params);
            httpGet.setURI(uri);
            httpGet.addHeader(BasicScheme.authenticate(new UsernamePasswordCredentials(username, password), HTTP.UTF_8, false));
            httpGet.addHeader("Accept", "application/json");
            httpGet.addHeader("Content-type", "application/json");

            HttpResponse httpResponse = httpClient.execute(httpGet);
            InputStream inputStream = httpResponse.getEntity().getContent();
            bufferedReader = new BufferedReader(new InputStreamReader(
                    inputStream));

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
	
	public String getRequestNoAuth(String params) {
        StringBuffer stringBuffer = new StringBuffer();
        BufferedReader bufferedReader = null;
        
        try {
            HttpClient httpClient = new DefaultHttpClient();
            HttpParams httpParameters = httpClient.getParams();
            HttpConnectionParams.setTcpNoDelay(httpParameters, true);
            HttpGet httpGet = new HttpGet();

            URI uri = new URI(SERVER_URL + params);
            httpGet.setURI(uri);
            httpGet.addHeader("Accept", "application/json");
            httpGet.addHeader("Content-type", "application/json");

            HttpResponse httpResponse = httpClient.execute(httpGet);
            InputStream inputStream = httpResponse.getEntity().getContent();
            bufferedReader = new BufferedReader(new InputStreamReader(
                    inputStream));

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
	
	public Bitmap getPicture(String params) {
        StringBuffer stringBuffer = new StringBuffer();
        Bitmap output = null;

        try {
        	HttpUriRequest request = new HttpGet(params);
            HttpClient httpClient = new DefaultHttpClient();
            HttpParams httpParameters = httpClient.getParams();
            HttpConnectionParams.setTcpNoDelay(httpParameters, true);
            //request.addHeader(BasicScheme.authenticate(new UsernamePasswordCredentials(username, password), HTTP.UTF_8, false));
            HttpResponse response = httpClient.execute(request);

            HttpEntity entity = response.getEntity();
            byte[] bytes = EntityUtils.toByteArray(entity);
            output = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } catch (Exception e) {
        	Log.e("Isegoria", "exception", e);
        } finally {

        }
        
        return output;
    }

    public void getFirstPhoto(int electionId, final int positionId, final ImageView imageView) {
        String url = SERVER_URL + "dbInterface/api/photos/" + String.valueOf(positionId) + "/";

        JsonObjectRequest req = new JsonObjectRequest(url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String url = (response.getJSONArray("photos").getJSONObject(0).getString("url"));
                            getFirstPhotoImage(url, imageView);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Volley", error.toString());
            }
        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                String credentials = username + ":" + password;
                String base64EncodedCredentials =
                        Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
                headers.put("Authorization", "Basic " + base64EncodedCredentials);
                headers.put("Accept", "application/json");
                headers.put("Content-type", "application/json");
                return headers;
            }
        };

        int socketTimeout = 30000;//30 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

    public void getFirstPhoto(String email, final ImageView imageView) {
        String url = SERVER_URL + "dbInterface/api/photos/" + String.valueOf(email) + "/";

        JsonObjectRequest req = new JsonObjectRequest(url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String url = (response.getJSONArray("photos").getJSONObject(0).getString("url"));
                            getFirstPhotoImage(url, imageView);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Volley", error.toString());
            }
        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                String credentials = username + ":" + password;
                String base64EncodedCredentials =
                        Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
                headers.put("Authorization", "Basic " + base64EncodedCredentials);
                headers.put("Accept", "application/json");
                headers.put("Content-type", "application/json");
                return headers;
            }
        };

        int socketTimeout = 30000;//30 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

    public void getFirstPhotoImage(String url, final ImageView view) {
        ImageRequest req = new ImageRequest(url,
                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap bitmap) {
                        view.setImageBitmap(bitmap);
                        view.refreshDrawableState();
                    }
                }, 0, 0, null,
                new Response.ErrorListener() {
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Volley", error.toString());
                    }
                }) {
        };

        int socketTimeout = 30000;//30 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

    public void getFirstPhotoBlur(int electionId, int positionId, final LinearLayout imageView) {
        String url = SERVER_URL + "dbInterface/api/photos/" + String.valueOf(positionId) + "/";

        JsonObjectRequest req = new JsonObjectRequest(url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String url = (response.getJSONArray("photos").getJSONObject(0).getString("url"));
                            getFirstPhotoImageBlur(url, imageView);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Volley", error.toString());
            }
        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                String credentials = username + ":" + password;
                String base64EncodedCredentials =
                        Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
                headers.put("Authorization", "Basic " + base64EncodedCredentials);
                headers.put("Accept", "application/json");
                headers.put("Content-type", "application/json");
                return headers;
            }
        };

        int socketTimeout = 30000;//30 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

    public void getFirstPhotoImageBlur(String url, final LinearLayout view) {
        ImageRequest req = new ImageRequest(url,
                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap bitmap) {
                        Drawable d = new BitmapDrawable(application.getMainActivity().getResources(),
                                ProfileFragment.fastBlur(bitmap, 25));
                        view.setBackgroundDrawable(d);
                    }
                }, 0, 0, null,
                new Response.ErrorListener() {
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Volley", error.toString());
                    }
                });

        int socketTimeout = 30000;//30 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

    public void getPictureVolley(String params, final ImageView view) {
        ImageRequest req = new ImageRequest(params,
                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap bitmap) {
                        view.setImageBitmap(bitmap);
                    }
                }, 0, 0, null,
                new Response.ErrorListener() {
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Volley", error.toString());
                    }
                });

        int socketTimeout = 30000;//30 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

    public void getPictureVolley2(String params, final ImageView view, final int squareSize, final PhotoViewFragment fragment) {
        ImageRequest req = new ImageRequest(params,
                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap srcBmp) {
                        view.setImageBitmap(srcBmp);
                        fragment.setImageBitmap(srcBmp);
                    }
                }, 0, 0, null,
                new Response.ErrorListener() {
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Volley", error.toString());
                    }
                });

        int socketTimeout = 30000;//30 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

    public Bitmap scaleCenterCrop(Bitmap source, int newHeight, int newWidth) {
        int sourceWidth = source.getWidth();
        int sourceHeight = source.getHeight();

        float xScale = (float) newWidth / sourceWidth;
        float yScale = (float) newHeight / sourceHeight;
        float scale = Math.max(xScale, yScale);

        float scaledWidth = scale * sourceWidth;
        float scaledHeight = scale * sourceHeight;

        float left = (newWidth - scaledWidth) / 2;
        float top = (newHeight - scaledHeight) / 2;

        RectF targetRect = new RectF(left, top, left + scaledWidth, top + scaledHeight);

        Bitmap dest = Bitmap.createBitmap(newWidth, newHeight, source.getConfig());
        Canvas canvas = new Canvas(dest);
        canvas.drawBitmap(source, null, targetRect, null);

        return dest;
    }

	public static Bitmap decodeSampledBitmapFromBitmap(InputStream is,
	        int reqWidth, int reqHeight) {
	    final BitmapFactory.Options options = new BitmapFactory.Options();
	    options.inJustDecodeBounds = true;
	    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

	    options.inJustDecodeBounds = false;
	    return BitmapFactory.decodeStream(is);
	}
	
	public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
	    // Raw height and width of image
	    final int height = options.outHeight;
	    final int width = options.outWidth;
	    int inSampleSize = 1;
	
	    if (height > reqHeight || width > reqWidth) {
	
	        final int halfHeight = height / 2;
	        final int halfWidth = width / 2;
	
	        // Calculate the largest inSampleSize value that is a power of 2 and keeps both
	        // height and width larger than the requested height and width.
	        while ((halfHeight / inSampleSize) > reqHeight
	                && (halfWidth / inSampleSize) > reqWidth) {
	            inSampleSize *= 2;
	        }
	    }
	
	    return inSampleSize;
	}
}
