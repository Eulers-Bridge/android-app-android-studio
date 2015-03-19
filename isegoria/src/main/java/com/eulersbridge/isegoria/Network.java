package com.eulersbridge.isegoria;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;
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
	private long userId;
	private String username;
	private String password;
	private String email;

	private NewsFragment newsFragment;
	private NewsArticleFragment newsArticleFragment;
	private UserSignupFragment userSignupFragment;
	private EventsDetailFragment eventDetailFragment;
	private PhotosFragment photosFragment;
	private PhotoAlbumFragment photoAlbumFragment;
	private PhotoViewFragment photoViewFragment;
	private VoteFragment voteFragment;
	private PollFragment pollFragment;
	private Isegoria application;

    private RequestQueue mRequestQueue;

	public Network(Isegoria application) {
        this.application = application;
	}

	public Network(Isegoria application, String username, String password) {
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

	public NetworkResponse logout() {
		NetworkResponse networkResponse = null;

		return networkResponse;
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
                                JSONObject currentArticle = jArray.getJSONObject(i);
                                int articleId = currentArticle.getInt("articleId");
                                int institutionId = currentArticle.getInt("institutionId");
                                String title = currentArticle.getString("title");
                                String content = currentArticle.getString("content");
                                String picture = currentArticle.getString("picture");
                                picture = picture.replace("[", "").replace("]", "").replace("\"", "").replace("\\", "");
                                Bitmap bitmapPicture = null;

                                String likers = null;
                                long date = currentArticle.getLong("date");
                                date = TimeConverter.convertTimestampTimezone(date);
                                String creatorEmail = "";
                                String studentYear = "";
                                String link = null;

                                newsFragment.addNewsArticle(articleId, institutionId, title, content, picture, likers, date, creatorEmail, studentYear, link);
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
					String picture = currentArticle.getString("picture");
					String email = currentArticle.getString("creatorEmail");
					picture = picture.replace("[", "").replace("]", "").replace("\"", "").replace("\\", "");
					Bitmap bitmapPicture = getPicture(picture);

					String likers = currentArticle.getString("likes");
					long date = currentArticle.getLong("date");
					date = TimeConverter.convertTimestampTimezone(date);
					String creatorEmail = "";
					String studentYear = "";
					String link = null;

					newsArticleFragment.populateContent(title, content, likes, date, bitmapPicture);
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
					JSONObject currentArticle = new JSONObject(response);

					String givenName = currentArticle.getString("givenName");
					String familyName = currentArticle.getString("familyName");
					String gender = currentArticle.getString("gender");
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

	public NetworkResponse getElections() {
		NetworkResponse networkResponse = null;

		return networkResponse;
	}

	public NetworkResponse getPolls() {
		NetworkResponse networkResponse = null;

		return networkResponse;
	}

	public NetworkResponse getUsers() {
		NetworkResponse networkResponse = null;

		return networkResponse;
	}

	public NetworkResponse getCountrys() {
		NetworkResponse networkResponse = null;

		return networkResponse;
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
                                    String picture = currentEvent.getString("picture");
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
					String picture = currentEvent.getString("picture");
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
					String creatorEmail = "";
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
        String url = SERVER_URL + "dbInterface/api/photos/" + String.valueOf(albumId);

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
                                String thumbNailUrl = currentAlbum.getString("thumbNailUrl");

                                photoAlbumFragment.addPhotoThumb(thumbNailUrl, String.valueOf(nodeId));
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

	public void getPhoto(final PhotoViewFragment photoViewFragment, final String photoId) {
		this.photoViewFragment = photoViewFragment;

		Runnable r = new Runnable() {
			public void run() {
				String response = getRequest("dbInterface/api/photo/" + String.valueOf(photoId));
				try {
					JSONObject jObject = new JSONObject(response);

					int nodeId = jObject.getInt("nodeId");
					String title = jObject.getString("title");
					String description = jObject.getString("description");
					String url = jObject.getString("url");
						
					Bitmap bitmapPicture = getPicture(url);
					photoViewFragment.addPhoto(title, bitmapPicture);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		};
		
		Thread t = new Thread(r);
		t.start();
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
						JSONObject currentAlbum = jArray.getJSONObject(i);
						
						int nodeId = currentAlbum.getInt("nodeId");
						String name = currentAlbum.getString("name");
						String description = currentAlbum.getString("description");
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
