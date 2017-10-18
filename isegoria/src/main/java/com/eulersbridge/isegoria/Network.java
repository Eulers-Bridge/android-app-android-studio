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

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transfermanager.TransferManager;
import com.amazonaws.mobileconnectors.s3.transfermanager.Upload;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Network {
	private static final String SERVER_URL = "http://54.79.70.241:8080/dbInterface/api";
	private static String PICTURE_URL = "https://s3-ap-southeast-2.amazonaws.com/isegoria/";

    private static final int socketTimeout = 30000; //30 seconds

	private long userId;
	private String username;
	private String password;
	private String email;
    private boolean hasPersonality;
    private boolean loginAccountVerified;

    private final Network network;
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
    private CandidatePositionFragment candidatePositionFragment;
    private TaskDetailProgressFragment taskDetailProgressFragment;
    private ProfileBadgesFragment profileBadgesFragment;
    private EmailVerificationFragment emailVerificationFragment;
    private CandidateTicketDetailFragment candidateTicketDetailFragment;
    private FindAddContactFragment findAddContactFragment;
	private VoteFragment voteFragment;
	private PollFragment pollFragment;
    private PollVoteFragment pollVoteFragment;
	private final Isegoria application;
    private boolean reminderSet = false;
    private boolean trackingOff;
    private boolean optOutDataCollection;

    private String loginGivenName = "";
    private String loginFamilyName = "";
    private String loginEmail = "";
    private String loginGender = "";
    private String loginNationality = "";
    private String loginYearOfBirth = "";

    private String voteReminderLocation;
    private long voteReminderDate;

    private int electionId;
    private int userDPId;

    private RequestQueue mRequestQueue;
    private ArrayList<Integer> userTickets = new ArrayList<>();

	public Network(Isegoria application) {
        this.network = this;
        this.application = application;
	}

	public Network(Isegoria application, String username, String password) {
        this.network = this;
		this.application = application;
		this.username = username;
		this.password = password;

        AuthorisedJsonObjectRequest.username = username;
        AuthorisedJsonObjectRequest.password = password;

        Cache cache = new DiskBasedCache(application.getCacheDir(), 1024 * 1024); // 1MB cap
        BasicNetwork network = new BasicNetwork(new HurlStack());

        mRequestQueue = new RequestQueue(cache, network);
        mRequestQueue.start();
	}

	void login() {
		Runnable r = new Runnable() {
			public void run() {
				String response = getRequest("/login");
				try {
                    if(response.startsWith("HTTP Status 401 - User is disabled")) {
                        application.setVerification();
                    }
                    else {
                        JSONObject jObject = new JSONObject(response);
                        userId = jObject.getLong("userId");

                        JSONObject jUser = jObject.getJSONObject("user");
                        loginGivenName = jUser.getString("givenName");
                        loginFamilyName = jUser.getString("familyName");
                        loginEmail = jUser.getString("email");
                        loginAccountVerified = jUser.getBoolean("accountVerified");
                        hasPersonality = jUser.getBoolean("hasPersonality");
                        trackingOff = jUser.getBoolean("trackingOff");
                        optOutDataCollection = jUser.getBoolean("optOutDataCollection");

                        loginGender = jUser.getString("gender");
                        loginNationality = jUser.getString("nationality");
                        loginYearOfBirth = jUser.getString("yearOfBirth");

                        if (loginAccountVerified) {
                            application.setLoggedIn(true);
                            if(hasPersonality) {
                                application.setFeedFragment();
                            }
                            else {
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

	void signup(final String firstName, final String lastName, final String gender, final String country, final String yearOfBirth, final String email, final String password, String confirmPassword, final String institution) {
       Runnable r = new Runnable() {
    	   public void run() {
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
    					application.getMainActivity().runOnUiThread(new Runnable() {
    						public void run() {
    							application.signupSucceeded();
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

	void getGeneralInfo(final UserSignupFragment userSignupFragment) {
		this.userSignupFragment = userSignupFragment;

		Runnable r = new Runnable() {
			public void run() {
				String response = getRequestNoAuth("general-info");
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


	void getNewsArticles(final NewsFragment newsFragment) {
		this.newsFragment = newsFragment;
        String url = SERVER_URL + "/newsArticles/26";

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
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
        });
        
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
	}

	void getNewsArticle(final NewsArticleFragment newsArticleFragment, final int articleId) {
		this.newsArticleFragment = newsArticleFragment;

		Runnable r = new Runnable() {
			public void run() {
				String response = getRequest("newsArticle/" + String.valueOf(articleId));
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
                    boolean inappropriateContent = currentArticle.getBoolean("inappropriateContent");

					String likers = currentArticle.getString("likes");
					long date = currentArticle.getLong("date");
					date = TimeConverter.convertTimestampTimezone(date);
					String link = null;

					newsArticleFragment.populateContent(title, content, likes, date, bitmapPicture, email, inappropriateContent);
					getUser(newsArticleFragment, email);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		};

		Thread t = new Thread(r);
		t.start();
	}

	private void getUser(final NewsArticleFragment newsArticleFragment, final String userEmail) {
		this.newsArticleFragment = newsArticleFragment;

		Runnable r = new Runnable() {
			public void run() {
				String response = getRequest("user/" + userEmail.replace("@", "%40") +"/");
				try {
					JSONObject currentUser = new JSONObject(response);

					String givenName = currentUser.getString("givenName");
					String familyName = currentUser.getString("familyName");
					String gender = currentUser.getString("gender");
					String name = givenName + " " + familyName;

                    JSONArray photos = currentUser.getJSONArray("photos");

                    int index=0;
                    for(int i=0; i<photos.length(); i++) {
                        JSONObject currentObject = photos.getJSONObject(i);
                        String currentSeq = currentObject.getString("sequence");

                        if(currentSeq.equals("0")) {
                            index = i;
                            break;
                        }
                    }

                    String photoURL = (currentUser.getJSONArray("photos").getJSONObject(index).getString("url"));

					newsArticleFragment.populateUserContent(name, photoURL);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		};

		Thread t = new Thread(r);
		t.start();
	}

    void getUserDP(ImageView imageView, LinearLayout backgroundLinearLayout) {
        this.getFirstPhoto(0, (int) userId, imageView);
        this.getFirstPhotoBlur(0, (int) userId, backgroundLinearLayout);
    }

    void getUserDP(int profileId, ImageView imageView, LinearLayout backgroundLinearLayout) {
        this.getFirstPhoto(0, profileId, imageView);
        this.getFirstPhotoBlur(0, profileId, backgroundLinearLayout);
    }

    void findFriends(final FindAddContactFragment findAddContactFragment) {
        this.findAddContactFragment = findAddContactFragment;
        String url = SERVER_URL + "/contactRequests/" + String.valueOf(this.userId) + "/";

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray jArray = response.getJSONArray("foundObjects");
                    for(int i=0; i<jArray.length(); i++) {
                        try {
                            JSONObject resp = jArray.getJSONObject(i);

                            int userId = resp.getInt("userId");
                            boolean acceptedBoolean = resp.getBoolean("accepted");
                            if(acceptedBoolean) {
                                String contactDetails = resp.getString("contactDetails");
                                network.findContactFriend(String.valueOf(userId), findAddContactFragment);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
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
        });

        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);

        url = SERVER_URL + "/user/" + String.valueOf(this.userId) + "/contactRequests";
        req = new AuthorisedJsonObjectRequest(url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray friends = response.getJSONArray("foundObjects");
                    for(int i=0; i<friends.length(); i++) {
                        try {
                            JSONObject resp = friends.getJSONObject(i);

                            int userId = resp.getInt("userId");
                            boolean acceptedBoolean = resp.getBoolean("accepted");
                            String contactDetails = resp.getString("contactDetails");

                            if(acceptedBoolean) {
                                network.findContactFriend(String.valueOf(userId), findAddContactFragment);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
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
        });

        policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

	void findContacts(String query, final FindAddContactFragment findAddContactFragment) {
        this.findAddContactFragment = findAddContactFragment;
        this.findAddContactFragment.clearSearchResults();
        String url = SERVER_URL + "/contact/" + String.valueOf(query) + "/";

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    String givenName = response.getString("givenName");
                    String familyName = response.getString("familyName");
                    String gender = response.getString("gender");
                    String email = response.getString("email");
                    String nationality = response.getString("nationality");

                    JSONObject profilePhoto = response.getJSONObject("profilePhoto");
                    String profilePhotoURL = profilePhoto.getString("url");

                    findAddContactFragment.addUser(givenName, familyName, email, "The University of Melbourne", profilePhotoURL);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Volley", error.toString());
            }
        });

        
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
	}

    private void findContactPending(final int contactId, String query, final FindAddContactFragment findAddContactFragment) {
        this.findAddContactFragment = findAddContactFragment;
        String url = SERVER_URL + "/user/" + String.valueOf(query) + "/";

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    String givenName = response.getString("givenName");
                    String familyName = response.getString("familyName");
                    String gender = response.getString("gender");
                    String email = response.getString("email");
                    String nationality = response.getString("nationality");

                    JSONObject profilePhoto = response.getJSONArray("photos").getJSONObject(0);
                    String profilePhotoURL = profilePhoto.getString("url");

                    findAddContactFragment.addPendingFriend(contactId, givenName, familyName, email, "The University of Melbourne", profilePhotoURL);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Volley", error.toString());
            }
        });

        
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

    void getDashboardStats(final ProfileFragment profileFragment) {
        String url = String.format("%s/contacts/%s/", SERVER_URL, loginEmail);

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray foundObjects = response.getJSONArray("foundObjects");

                    int numOfContacts = 0;
                    int numOfCompBadges = 0;
                    int numOfCompTasks = 0;
                    int totalBadges = 0;
                    int totalTasks = 0;
                    int experience = 0;

                    if (foundObjects.length() > 0) {
                        JSONObject currentObject = foundObjects.getJSONObject(0);

                        numOfContacts = currentObject.getInt("numOfContacts");
                        numOfCompBadges = currentObject.getInt("numOfCompBadges");
                        numOfCompTasks = currentObject.getInt("numOfCompTasks");
                        totalBadges = currentObject.getInt("totalBadges");
                        totalTasks = currentObject.getInt("totalTasks");
                        experience = currentObject.getInt("experience");
                    }

                    profileFragment.updateStats(numOfContacts, numOfCompBadges, numOfCompTasks,
                            totalBadges, totalTasks, experience);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Volley", error.toString());
            }
        });

        
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

    void getDashboardStats(final ContactProfileFragment contactProfileFragment,
                                  int profileUserId) {
        String url = SERVER_URL + "/contacts/" + String.valueOf(profileUserId);

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
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
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Volley", error.toString());
            }
        });
        
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

    private void findContactFriend(final String query, final FindAddContactFragment findAddContactFragment) {
        this.findAddContactFragment = findAddContactFragment;
        String url = SERVER_URL + "/user/" + String.valueOf(query) + "/";

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    int userId = Integer.parseInt(query);
                    String givenName = response.getString("givenName");
                    String familyName = response.getString("familyName");
                    String gender = response.getString("gender");
                    String email = response.getString("email");
                    String nationality = response.getString("nationality");

                    JSONObject profilePhoto = response.getJSONArray("photos").getJSONObject(0);
                    String profilePhotoURL = profilePhoto.getString("url");

                    findAddContactFragment.addFriend(userId, givenName, familyName, email, "The University of Melbourne", profilePhotoURL);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Volley", error.toString());
            }
        });

        
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

    void addFriend(String email, final FindAddContactFragment findAddContactFragment) {
        this.findAddContactFragment = findAddContactFragment;
        String url = SERVER_URL + "/user/" + String.valueOf(userId) + "/contact/" + String.valueOf(email) + "/";

        HashMap<String, Integer> params = new HashMap<>();

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(Request.Method.PUT, url, new JSONObject(params),
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
        });

        
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);

        Log.d("VolleyRequest", req.toString());
    }

    void acceptContact(String email, final FindAddContactFragment findAddContactFragment) {
        this.findAddContactFragment = findAddContactFragment;
        String url = SERVER_URL + "/contact/" + String.valueOf(email) + "/";

        HashMap<String, Integer> params = new HashMap<>();

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(Request.Method.PUT, url, new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            findAddContactFragment.showAcceptDialog();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Volley", error.toString());
            }
        });

        
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);

        Log.d("VolleyRequest", req.toString());
    }

    void denyContact(String email, final FindAddContactFragment findAddContactFragment) {
        this.findAddContactFragment = findAddContactFragment;
        String url = SERVER_URL + "/contact/" + String.valueOf(email) + "/";

        HashMap<String, Integer> params = new HashMap<>();

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(Request.Method.DELETE, url, new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            findAddContactFragment.showDenyDialog();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Volley", error.toString());
            }
        });

        
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);

        Log.d("VolleyRequest", req.toString());
    }

    void findPendingContacts(final FindAddContactFragment findAddContactFragment) {
        this.findAddContactFragment = findAddContactFragment;
        String url = SERVER_URL + "/contactRequests/" + String.valueOf(this.userId) + "/";

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray jArray = response.getJSONArray("foundObjects");
                    for(int i=0; i<jArray.length(); i++) {
                        JSONObject currentObject = jArray.getJSONObject(i);

                        int nodeId = currentObject.getInt("nodeId");
                        int userId = currentObject.getInt("userId");
                        //boolean acceptedContact = response.getBoolean("accepted");
                       // boolean rejectedContact = response.getBoolean("rejected");
                        if(currentObject.isNull("accepted") && currentObject.isNull("rejected")) {
                            String contactDetails = currentObject.getString("contactDetails");
                            network.findContactPending(nodeId, String.valueOf(userId), findAddContactFragment);
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
        });

        
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

    public void addVoteReminder(String location, long date) {
        String url = SERVER_URL + "/user/" + String.valueOf(userId) + "/voteReminder";

        HashMap<String, String> params = new HashMap<>();
        params.put("location", location);
        params.put("date", String.valueOf(date));
        params.put("electionId", String.valueOf(electionId));

        setVoteReminderDate(date);
        setVoteReminderLocation(location);

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(Request.Method.PUT, url, new JSONObject(params),
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
        });

        
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);

        Log.d("VolleyRequest", req.toString());
    }

	void getEvents(final EventsFragment eventsFragment) {
        String url = SERVER_URL + "/events/26/";

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jObject) {
                            try {
                                JSONArray jArray = jObject.getJSONArray("foundObjects");

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
                                    } catch(Exception ignored) {

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
        });

        
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
	}

    void verifyEmail(final EmailVerificationFragment emailVerificationFragment) {
        this.emailVerificationFragment = emailVerificationFragment;
        String url = SERVER_URL + "/emailVerification/" + String.valueOf(username) + "/resend";

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jObject) {
                        String test = ":)";
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Volley", error.toString());
            }
        });

        
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

	void getEventDetails(final EventsDetailFragment eventsDetailFragment, final int eventId) {
		this.eventDetailFragment = eventsDetailFragment;

		Runnable r = new Runnable() {
			public void run() {
				String response = getRequest("event/" + String.valueOf(eventId));
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

    private void findContact(final String email, final EventsDetailFragment eventsDetailFragment) {
        this.eventsDetailFragment = eventsDetailFragment;
        String url = SERVER_URL + "/contact/" + String.valueOf(email) + "/";

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(url, null,
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
        });

        
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

    void findContactPhoto(final String email, final ImageView imageView) {
        String url = SERVER_URL + "/contact/" + String.valueOf(email) + "/";

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jObject) {
                        try {
                            JSONObject photo = jObject.getJSONObject("profilePhoto");

                            String url = photo.getString("url");
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
        });

        
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

	void getPhotoAlbums(final PhotosFragment photosFragment) {
		this.photosFragment = photosFragment;
        String url = SERVER_URL + "/photoAlbums/7449";

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jObject) {
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
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Volley", error.toString());
            }
        });

        
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
	}

	void getPhotoAlbum(final PhotoAlbumFragment photoAlbumFragment, final String albumId) {
		this.photoAlbumFragment = photoAlbumFragment;
        String url = SERVER_URL + "/photos/" + String.valueOf(albumId) + "?pageSize=100";

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(url, null,
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
        });

        
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
	}

    void getProfileBadgesComplete(final ProfileBadgesFragment profileBadgesFragment, final String targetName, final int targetLevel) {
        this.profileBadgesFragment = profileBadgesFragment;
        String url = SERVER_URL + "/badges/complete/" + String.valueOf(this.userId);

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
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

                            network.getProfileBadgesRemaining(profileBadgesFragment, targetName, targetLevel);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Volley", error.toString());
            }
        });

        
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

    private void getProfileBadgesRemaining(final ProfileBadgesFragment profileBadgesFragment, final String targetName, final int targetLevel) {
        this.profileBadgesFragment = profileBadgesFragment;
        String url = SERVER_URL + "/badges/remaining/" + String.valueOf(this.userId);

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
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
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Volley", error.toString());
            }
        });

        
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

	void getPhoto(final PhotoViewFragment photoViewFragment, final int photoId) {
		this.photoViewFragment = photoViewFragment;
        String url = SERVER_URL + "/photo/" + String.valueOf(photoId);

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(url, null,
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
                            boolean inappropriateContent = response.getBoolean("inappropriateContent");
                            int numOfLikes = response.getInt("numOfLikes");

                            photoViewFragment.setData(title, date, inappropriateContent,
                                    numOfLikes);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Volley", error.toString());
            }
        });

        
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
	}

    void getNewsArticleLiked(final NewsArticleFragment newsArticleFragment) {
        this.newsArticleFragment = newsArticleFragment;
        String url = SERVER_URL + "/newsArticle/"
                + String.valueOf(newsArticleFragment.getArticleId()) + "/likedBy/" + String.valueOf(loginEmail) + "/";

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    boolean success = response.getBoolean("success");

                    if(success) {
                        newsArticleFragment.initiallyLiked();
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
        });

        
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

	public void getVoteRecords(final VoteFragment voteFragment) {
		this.voteFragment = voteFragment;

		Runnable r = new Runnable() {
			public void run() {
				String response = getRequest("voteRecord");
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
			}
		};

		Thread t = new Thread(r);
		t.start();
	}

	void getPollQuestions(final PollFragment pollFragment) {
		this.pollFragment = pollFragment;

        String url = SERVER_URL + "/polls/26";

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            JSONArray jArray = response.getJSONArray("polls");

                            for (int i=0; i<jArray.length(); i++) {
                                JSONObject currentAlbum = jArray.getJSONObject(i);

                                int nodeId = currentAlbum.getInt("nodeId");
                                //int creatorID = currentAlbum.getInt("creatorId");
                                String creatorEmail = currentAlbum.getString("creatorEmail");
                                String question = currentAlbum.getString("question");
                                String answers = currentAlbum.getString("answers");
                                int numOfComments = currentAlbum.getInt("numOfComments");
                                int numOfAnswers = currentAlbum.getInt("numOfAnswers");

                                pollFragment.addQuestion(nodeId, creatorEmail, question,
                                        answers, numOfComments, numOfAnswers);
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
        });

        
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
	}

    private void getPollResults(final int nodeId, final PollVoteFragment pollVoteFragment) {
        String url = SERVER_URL + "/poll/" + String.valueOf(nodeId) + "/results";

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(url, null,
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
        });

        
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

    void getPollComments(final int pollId, final PollVoteFragment pollVoteFragment) {
        this.pollVoteFragment = pollVoteFragment;
        String url = SERVER_URL + "/comments/" + String.valueOf(pollId) + "/";

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray jArray = response.getJSONArray("foundObjects");
                    for(int i=0; i<jArray.length(); i++) {
                        int targetId = jArray.getJSONObject(i).getInt("targetId");
                        int commentId = jArray.getJSONObject(i).getInt("commentId");
                        String userName = jArray.getJSONObject(i).getString("userName");
                        String userEmail = jArray.getJSONObject(i).getString("userEmail");
                        String content = jArray.getJSONObject(i).getString("content");

                        pollVoteFragment.addTableComment(userName, content, userEmail);
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
        });

        
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

    void postPollComment(final int pollId, String comment, final PollFragment pollFragment) {
        this.pollFragment = pollFragment;
        String url = SERVER_URL + "/comment";

        HashMap<String, String> params = new HashMap<>();
        params.put("targetId", String.valueOf(pollId));
        params.put("userName", (this.loginGivenName + " " + this.loginFamilyName));
        params.put("userEmail", this.loginEmail);
        params.put("content", comment);

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(Request.Method.POST, url, new JSONObject(params),
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
        });

        
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);

        Log.d("VolleyRequest", req.toString());
    }

    void answerPersonality(float extroversion, float agreeableness, float conscientiousness,
                                  float emotionalStability, float openess) {
        String url = SERVER_URL + "/user/" + String.valueOf(userId) + "/personality";

        HashMap<String, Float> params = new HashMap<>();
        params.put("extroversion", extroversion);
        params.put("agreeableness", agreeableness);
        params.put("conscientiousness", conscientiousness);
        params.put("emotionalStability", emotionalStability);
        params.put("openess", openess);

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(Request.Method.PUT, url, new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            application.setFeedFragment();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Volley", error.toString());
            }
        });

        
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);

        Log.d("VolleyRequest", req.toString());
    }

    void answerPoll(int pollId, int answerIndex, final PollVoteFragment pollVoteFragment) {
        String url = SERVER_URL + "/poll/" + String.valueOf(pollId) + "/answer";

        HashMap<String, Integer> params = new HashMap<>();
            params.put("answerIndex", answerIndex);
            params.put("answererId", (int) userId);
            params.put("pollId", pollId);

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(Request.Method.PUT, url, new JSONObject(params),
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
        });

        
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);

        Log.d("VolleyRequest", req.toString());
    }

    void getVoteLocations(final VoteFragment voteFragment) {

        String url = SERVER_URL + "/votingLocations/26";
        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
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
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Volley", error.toString());
            }
        });

        
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

    public void getVoteLocation(final VoteFragment voteFragment, final String pos) {

        String url = SERVER_URL + "/votingLocation/" + pos;
        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(url, null, new Response.Listener<JSONObject>() {
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
        });

        
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

    private void getLatestElection() {
        String url = SERVER_URL + "/elections/26";

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray foundObject = response.getJSONArray("foundObjects");
                    JSONObject electionObject = foundObject.getJSONObject(0);

                    network.electionId = electionObject.getInt("electionId");

                    alreadySetVoteReminder();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Volley", error.toString());
            }
        });

        
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

    void getLatestElection(final ElectionOverviewFragment electionOverviewFragment) {
        this.electionOverviewFragment = electionOverviewFragment;
        String url = SERVER_URL + "/elections/26";

        JsonObjectRequest req = new JsonObjectRequest(url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray jArray = response.getJSONArray("foundObjects");
                            JSONObject electionObject = jArray.getJSONObject(0);

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
            HashMap<String, String> headers = new HashMap<>();
            String credentials = username + ":" + password;
            String base64EncodedCredentials = Base64.encodeToString(credentials.getBytes(),
                        Base64.NO_WRAP);
            headers.put("Authorization", "Basic " + base64EncodedCredentials);
            headers.put("Accept", "application/json");
            headers.put("Content-type", "application/json");

            return headers;
        }
    };
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

    void getUserSupportedTickets() {
        String url = SERVER_URL + "/user/" + String.valueOf(this.userId) + "/support/";

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
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
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Volley", error.toString());
            }
        });

        
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

    void getLatestElection(final VoteFragment voteFragment) {
        this.voteFragment = voteFragment;
        String url = SERVER_URL + "/elections/26";

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray jArray = response.getJSONArray("foundObjects");
                    JSONObject electionObject = jArray.getJSONObject(0);

                    network.electionId = electionObject.getInt("electionId");

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
        });

        
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

    public void getCandidates(final CandidatePositionsFragment candidatePositionsFragment) {
        this.candidatePositionsFragment = candidatePositionsFragment;
        String url = SERVER_URL + "/candidates/26";

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray jArray = response.getJSONArray("foundObjects");
                    JSONObject candidateObject = jArray.getJSONObject(0);


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Volley", error.toString());
            }
        });

        
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

    void getCandidates(final CandidateAllFragment candidateAllFragment) {
        this.candidateAllFragment = candidateAllFragment;
        String url = SERVER_URL + "/candidates/" + String.valueOf(electionId);

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
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
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Volley", error.toString());
            }
        });

        
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

    void getCandidatesPosition(final CandidatePositionFragment candidatePositionFragment, final int selectedPositionId) {
        this.candidatePositionFragment = candidatePositionFragment;
        String url = SERVER_URL + "/position/" + String.valueOf(selectedPositionId) + "/candidates";

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
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
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Volley", error.toString());
            }
        });

        
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

    void getTicketLabel(final TextView textView, final int tickedId) {
        String url = SERVER_URL + "/ticket/" + String.valueOf(tickedId);

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(url, null, new Response.Listener<JSONObject>() {
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
        });

        
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

    void getTickets(final CandidateTicketFragment candidateTicketFragment) {
        this.candidateTicketFragment = candidateTicketFragment;
        String url = SERVER_URL + "/tickets/" + String.valueOf(electionId);

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
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
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Volley", error.toString());
            }
        });

        
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

    void getTicketDetail(int ticketId, final CandidateTicketDetailFragment candidateTicketDetailFragment) {
        this.candidateTicketDetailFragment = candidateTicketDetailFragment;
        String url = SERVER_URL + "/ticket/" + String.valueOf(ticketId) + "/candidates";

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray jArray = response.getJSONArray("foundObjects");

                    for(int i=0; i<jArray.length(); i++) {
                        JSONObject currentObject = jArray.getJSONObject(i);

                        int ticketId = currentObject.getInt("ticketId");
                        String givenName = currentObject.getString("givenName");
                        String familyName = currentObject.getString("familyName");
                        String name = givenName + " " + familyName;
                        String code = "";
                        String colour = "#000000";
                        String information = "";
                        String logo = "";

                        candidateTicketDetailFragment.updateInformation(ticketId,
                                network.electionId, name, code, information, colour);
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
        });

        
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

    void getTicketCandidates(final CandidateTicketDetailFragment candidateTicketDetailFragment, final int selectedTickedId) {
        this.candidateTicketDetailFragment = candidateTicketDetailFragment;
        String url = SERVER_URL + "/ticket/" + String.valueOf(selectedTickedId) + "/candidates";

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
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
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Volley", error.toString());
            }
        });

        
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

    void getPositionText(final TextView positionTextView, int positionId) {
        String url = SERVER_URL + "/position/" + String.valueOf(positionId);

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(url, null,
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
        });

        
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

    void getPositions(final CandidatePositionsFragment candidatePositionsFragment) {
        this.candidatePositionsFragment = candidatePositionsFragment;
        String url = SERVER_URL + "/positions/" + String.valueOf(electionId);

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray jArray = response.getJSONArray("foundObjects");
                    for(int i=0; i<jArray.length(); i++) {
                        JSONObject positionObject = jArray.getJSONObject(i);

                        int electionId = positionObject.getInt("electionId");
                        int positionId = positionObject.getInt("positionId");
                        String name = positionObject.getString("name");
                        String desc = positionObject.getString("description");

                        candidatePositionsFragment.addPosition(electionId, positionId,
                                name, desc, jArray.length());
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
        });

        
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

    void addVoteReminder(long date, String location) {
        String url = SERVER_URL + "/user/" + String.valueOf(loginEmail) + "/voteReminder";

        HashMap<String, String> params = new HashMap<>();
        params.put("location", location);
        params.put("date", String.valueOf(date));
        params.put("electionId", String.valueOf(network.electionId));

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(Request.Method.PUT, url, new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            //If we land in here the vote was submitted successfully
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Volley", error.toString());
            }
        });

        
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);

        Log.d("VolleyRequest", req.toString());
    }

    private void alreadySetVoteReminder() {
        String url = SERVER_URL + "/user/" + String.valueOf(this.userId) + "/voteReminders/";
        HashMap<String, String> params = new HashMap<>();

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
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
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Volley", error.toString());
            }
        });

        
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

    void supportTicket(int ticketId, CandidateTicketDetailFragment candidateTicketDetailFragment) {
        this.candidateTicketDetailFragment = candidateTicketDetailFragment;

        String url = SERVER_URL + "/ticket/" + String.valueOf(ticketId) + "/support/" + String.valueOf(loginEmail) + "/";
        HashMap<String, String> params = new HashMap<>();
        userTickets.add(ticketId);

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(Request.Method.PUT, url, new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            //If we land in here the vote was submitted successfully
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
        });

        
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);

        Log.d("VolleyRequest", req.toString());
    }

    void unsupportTicket(int ticketId, CandidateTicketDetailFragment candidateTicketDetailFragment) {
        this.candidateTicketDetailFragment = candidateTicketDetailFragment;

        String url = SERVER_URL + "/ticket/" + String.valueOf(ticketId) + "/support/" + String.valueOf(loginEmail) + "/";
        HashMap<String, String> params = new HashMap<>();
        userTickets.remove(Integer.valueOf(ticketId));

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(Request.Method.DELETE, url, new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            //If we land in here the vote was submitted successfully
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
        });

        
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);

        Log.d("VolleyRequest", req.toString());
    }

    void getPhotoLiked(final PhotoViewFragment photoViewFragment) {
        this.photoViewFragment = photoViewFragment;
        String url = SERVER_URL + "/photo/"
                + String.valueOf(photoViewFragment.getPhotoPath())
                + "/likedBy/"
                + String.valueOf(loginEmail) + "/";

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    boolean success = response.getBoolean("success");

                    if(success) {
                        photoViewFragment.initiallyLiked();
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
        });

        
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

    void likePhoto(int photoId, PhotoViewFragment photoViewFragment) {

        String url = SERVER_URL + "/photo/" + String.valueOf(photoId)
                + "/likedBy/"
                + String.valueOf(loginEmail) + "/";
        HashMap<String, String> params = new HashMap<>();

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(Request.Method.PUT, url, new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            //If we land in here the vote was submitted successfully
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
        });

        
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);

        Log.d("VolleyRequest", req.toString());
    }

    void unlikePhoto(int photoId, PhotoViewFragment photoViewFragment) {

        String url = SERVER_URL + "/photo/" + String.valueOf(photoId) + "/likedBy/" + String.valueOf(loginEmail) + "/";
        HashMap<String, String> params = new HashMap<>();

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(Request.Method.DELETE, url, new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            //If we land in here the vote was submitted successfully
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
        });

        
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);

        Log.d("VolleyRequest", req.toString());
    }

    void likeArticle(int articleId, NewsArticleFragment newsArticleFragment) {
        this.newsArticleFragment = newsArticleFragment;

        String url = SERVER_URL + "/newsArticle/" + String.valueOf(articleId) + "/likedBy/" + String.valueOf(loginEmail) + "/";
        HashMap<String, String> params = new HashMap<>();

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(Request.Method.PUT, url, new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            //If we land in here the vote was submitted successfully
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
        });

        
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);

        Log.d("VolleyRequest", req.toString());
    }

    void unlikeArticle(int articleId, NewsArticleFragment newsArticleFragment) {
        this.newsArticleFragment = newsArticleFragment;

        String url = SERVER_URL + "/newsArticle/" + String.valueOf(articleId) + "/likedBy/" + String.valueOf(loginEmail) + "/";
        HashMap<String, String> params = new HashMap<>();

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(Request.Method.DELETE, url, new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            //If we land in here the vote was submitted successfully
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
        });

        
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);

        Log.d("VolleyRequest", req.toString());
    }

    void getUserFullName(String userEmail, final TextView textView, final String params) {
        String url = String.format("%s/user/%s/", SERVER_URL, userEmail);

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(url, null,
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
        });

        
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

    void getTasks(final ProfileFragment profileFragment) {
        String url = SERVER_URL + "/tasks/";

        JsonObjectRequest req = new JsonObjectRequest(url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
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
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Volley", error.toString());
            }
        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                String credentials = username + ":" + password;
                String base64EncodedCredentials = Base64.encodeToString(credentials.getBytes(),
                        Base64.NO_WRAP);
                headers.put("Authorization", "Basic " + base64EncodedCredentials);
                headers.put("Accept", "application/json");
                headers.put("Content-type", "application/json");

                return headers;
            }
        };

        
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

    void getTasks(final ContactProfileFragment contactProfileFragment) {
        String url = SERVER_URL + "/tasks/complete";

        JsonObjectRequest req = new JsonObjectRequest(url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
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
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Volley", error.toString());
            }
        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                String credentials = username + ":" + password;
                String base64EncodedCredentials = Base64.encodeToString(credentials.getBytes(),
                        Base64.NO_WRAP);
                headers.put("Authorization", "Basic " + base64EncodedCredentials);
                headers.put("Accept", "application/json");
                headers.put("Content-type", "application/json");

                return headers;
            }
        };

        
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

    void getCompletedTasks(final TaskDetailProgressFragment taskDetailProgressFragment) {
        this.taskDetailProgressFragment = taskDetailProgressFragment;
        String url = SERVER_URL + "/tasks/complete/"+String.valueOf(userId) + "?pageSize=20";

        JsonObjectRequest req = new JsonObjectRequest(url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
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
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Volley", error.toString());
            }
        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                String credentials = username + ":" + password;
                String base64EncodedCredentials = Base64.encodeToString(credentials.getBytes(),
                        Base64.NO_WRAP);
                headers.put("Authorization", "Basic " + base64EncodedCredentials);
                headers.put("Accept", "application/json");
                headers.put("Content-type", "application/json");

                return headers;
            }
        };

        
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

    void getRemainingTasks(final TaskDetailProgressFragment taskDetailProgressFragment) {
        this.taskDetailProgressFragment = taskDetailProgressFragment;
        String url = SERVER_URL + "/tasks/remaining/"+String.valueOf(userId) + "?pageSize=20";

        JsonObjectRequest req = new JsonObjectRequest(url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
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
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Volley", error.toString());
            }
        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                String credentials = username + ":" + password;
                String base64EncodedCredentials = Base64.encodeToString(credentials.getBytes(),
                        Base64.NO_WRAP);
                headers.put("Authorization", "Basic " + base64EncodedCredentials);
                headers.put("Accept", "application/json");
                headers.put("Content-type", "application/json");

                return headers;
            }
        };

        
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

	public void likeNewsArticle(final NewsArticleFragment newsArticleFragment) {
		this.newsArticleFragment = newsArticleFragment;

		Runnable r = new Runnable() {
			public void run() {
				try {
					String response = getRequest("newsArticle/"+String.valueOf(newsArticleFragment.getArticleId()) + "/likedBy/" + email + "/");

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};

		Thread t = new Thread(r);
		t.start();
	}

    String getLoginGivenName() {
        return loginGivenName;
    }

    public void setLoginGivenName(String loginGivenName) {
        this.loginGivenName = loginGivenName;
    }

    String getLoginFamilyName() {
        return loginFamilyName;
    }

    public void setLoginFamilyName(String loginFamilyName) {
        this.loginFamilyName = loginFamilyName;
    }

    String getLoginEmail() {
        return loginEmail;
    }

    public void setLoginEmail(String loginEmail) {
        this.loginEmail = loginEmail;
    }

    private String getRequest(String params) {
        StringBuilder stringBuffer = new StringBuilder();
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

	private String getRequestNoAuth(String params) {
        StringBuilder stringBuffer = new StringBuilder();
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

	private Bitmap getPicture(String params) {
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
        }

        return output;
    }

    public void getFirstPhoto(int electionId, final int positionId, final ImageView imageView) {
        String url = String.format("%s/photos/%d/", SERVER_URL, positionId);

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
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

                            String url = (response.getJSONArray("photos").getJSONObject(index).getString("url"));
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
        });

        
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

    void getFirstPhotoFromUser(final String userEmail, final ImageView imageView) {
        String url = String.format("%s/photos/%s/", SERVER_URL, userEmail);

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
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

                            String url = (response.getJSONArray("photos").getJSONObject(index).getString("url"));
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
        });

        
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

    void getUserDPId() {
        String url = String.format("%s/photos/%s/", SERVER_URL, loginEmail);

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            userDPId = (response.getJSONArray("photos").getJSONObject(0).getInt("nodeId"));
                            String url = (response.getJSONArray("photos").getJSONObject(0).getString("url"));

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Volley", error.toString());
            }
        });

        
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

    public void getFirstPhoto(String email, final ImageView imageView) {
        String url = SERVER_URL + "/photos/" + String.valueOf(email) + "/";

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(url, null,
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
        });

        
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

    private void getFirstPhotoImage(String url, final ImageView view) {
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

        
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

    private void getFirstPhotoBlur(int electionId, int positionId, final LinearLayout imageView) {
        String url = SERVER_URL + "/photos/" + String.valueOf(positionId) + "/";

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
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

                            String url = (response.getJSONArray("photos").getJSONObject(index).getString("url"));
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
        });

        
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

    private void getFirstPhotoImageBlur(String url, final LinearLayout view) {
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

        
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

    void getPictureVolley(String params, final ImageView view) {
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

        
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

    void getPictureVolley2(String params, final ImageView view, final int squareSize, final PhotoViewFragment fragment) {
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

        
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);
    }

    ArrayList<Integer> getUserTickets() {
        return userTickets;
    }

    public void setUserTickets(ArrayList<Integer> userTickets) {
        this.userTickets = userTickets;
    }

    boolean isReminderSet() {
        return reminderSet;
    }

    public void setReminderSet(boolean reminderSet) {
        this.reminderSet = reminderSet;
    }

    long getVoteReminderDate() {
        return voteReminderDate;
    }

    private void setVoteReminderDate(long voteReminderDate) {
        this.voteReminderDate = voteReminderDate;
    }

    String getVoteReminderLocation() {
        return voteReminderLocation;
    }

    private void setVoteReminderLocation(String voteReminderLocation) {
        this.voteReminderLocation = voteReminderLocation;
    }

    boolean isOptOutDataCollection() {
        return optOutDataCollection;
    }

    void setOptOutDataCollection(boolean optOutDataCollection) {
        this.optOutDataCollection = optOutDataCollection;
    }

    boolean isTrackingOff() {
        return trackingOff;
    }

    void setTrackingOff(boolean trackingOff) {
        this.trackingOff = trackingOff;
    }

    void s3Upload(final File file) {
        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                application.getApplicationContext(), // Context,
                "715927704730",
                "us-east-1:73ae30c9-393c-44cf-a0ac-049cc0838428",
                "arn:aws:iam::715927704730:role/Cognito_isegoriaUnauth_Role",
                "arn:aws:iam::715927704730:role/Cognito_isegoriaAuth_Role",
                Regions.US_EAST_1 // Region
        );

        AmazonS3Client s3Client = new AmazonS3Client(credentialsProvider);

        TransferUtility transferUtility = new TransferUtility(s3Client, application.getApplicationContext());

        long timestamp = System.currentTimeMillis() / 1000L;
        final String filename = String.valueOf(userId) + "_" + String.valueOf(timestamp) + "_" + file.getName();
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
        String url = SERVER_URL + "/photo";
        long timestamp = System.currentTimeMillis() / 1000L;

        HashMap<String, String> params = new HashMap<>();
        params.put("url", pictureURL);
        params.put("thumbNailUrl", pictureURL);
        params.put("title", "Profile Picture");
        params.put("description", "Profile Picture");
        params.put("date", String.valueOf(timestamp));
        params.put("ownerId", String.valueOf(userId));
        params.put("sequence", String.valueOf("0"));

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(Request.Method.POST, url, new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String result = response.toString();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Volley", error.toString());
            }
        });

        
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);

        Log.d("VolleyRequest", req.toString());
    }

    void updateUserDetails() {
        String url = SERVER_URL + "/user/" + String.valueOf(userId);

        HashMap<String, String> params = new HashMap<>();
        params.put("trackingOff", String.valueOf(this.isTrackingOff()));
        params.put("optOutDataCollection", String.valueOf(this.optOutDataCollection));

        AuthorisedJsonObjectRequest req = new AuthorisedJsonObjectRequest(Request.Method.PUT, url, new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String responseString = response.toString();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Volley", error.toString());
            }
        });

        
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(policy);
        mRequestQueue.add(req);

        Log.d("VolleyRequest", req.toString());
    }
}
