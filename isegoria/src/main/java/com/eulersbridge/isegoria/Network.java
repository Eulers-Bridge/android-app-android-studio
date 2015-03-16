package com.eulersbridge.isegoria;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

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
	
	public Network(Isegoria application) {
		this.application = application;
	}
	
	public Network(Isegoria application, String username, String password) {
		this.application = application;
		this.username = username;
		this.password = password;
	}
	
	public void login() {
		Runnable r = new Runnable() {
			public void run() {
				String response = getRequest("dbInterface/api/login");
				try {
					JSONObject jObject = new JSONObject(response);
					application.setLoggedIn(true);
					application.setFeedFragment();
					
					jObject.getLong("userId");
					
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

		Runnable r = new Runnable() {
			public void run() {
				String response = getRequest("dbInterface/api/newsArticles/26");
				try {
					JSONObject jObject = new JSONObject(response);
					JSONArray jArray = jObject.getJSONArray("articles");
					
					for (int i=0; i<jArray.length(); i++) {
						JSONObject currentArticle = jArray.getJSONObject(i);
						int articleId = currentArticle.getInt("articleId");
						int institutionId = currentArticle.getInt("institutionId");
						String title = currentArticle.getString("title");
						String content = currentArticle.getString("content");
						String picture = currentArticle.getString("picture");
						picture = picture.replace("[", "").replace("]", "").replace("\"", "").replace("\\", "");
						Bitmap bitmapPicture = getPicture(picture);
						
						String likers = null;
						long date = currentArticle.getLong("date");
						date = TimeConverter.convertTimestampTimezone(date);
						String creatorEmail = "";
						String studentYear = "";
						String link = null;
						
						newsFragment.addNewsArticle(articleId, institutionId, title, content, bitmapPicture, likers, date, creatorEmail, studentYear, link);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		};
		
		Thread t = new Thread(r);
		t.start();
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
		Runnable r = new Runnable() {
			public void run() {
				String response = getRequest("dbInterface/api/events/26/");
				try {
					JSONObject jObject = new JSONObject(response);
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
						
						try {
						
							eventsFragment.addEvent(eventId, name, date, bitmapPicture);
						
						} catch(Exception e) {
							
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

		Runnable r = new Runnable() {
			public void run() {
				String response = getRequest("dbInterface/api/photoAlbums/7449");
				try {
					JSONObject jObject = new JSONObject(response);
					JSONArray jArray = jObject.getJSONArray("photoAlbums");
					
					for (int i=0; i<jArray.length(); i++) {
						JSONObject currentAlbum = jArray.getJSONObject(i);
						
						int nodeId = currentAlbum.getInt("nodeId");
						String name = currentAlbum.getString("name");
						String description = currentAlbum.getString("description");
						String thumbNailUrl = currentAlbum.getString("thumbNailUrl");
						
						Bitmap bitmapPicture = getPicture(thumbNailUrl);
						photosFragment.addPhotoAlbum(nodeId, name, description, bitmapPicture);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		};
		
		Thread t = new Thread(r);
		t.start();
	}
	
	public void getPhotoAlbum(final PhotoAlbumFragment photoAlbumFragment, final String albumId) {
		this.photoAlbumFragment = photoAlbumFragment;

		Runnable r = new Runnable() {
			public void run() {
				String response = getRequest("dbInterface/api/photos/" + String.valueOf(albumId));
				try {
					JSONObject jObject = new JSONObject(response);
					JSONArray jArray = jObject.getJSONArray("photos");
					
					for (int i=0; i<jArray.length(); i++) {
						JSONObject currentAlbum = jArray.getJSONObject(i);
						
						int nodeId = currentAlbum.getInt("nodeId");
						String title = currentAlbum.getString("title");
						String description = currentAlbum.getString("description");
						String thumbNailUrl = currentAlbum.getString("thumbNailUrl");
						
						Bitmap bitmapPicture = getPicture(thumbNailUrl);
						photoAlbumFragment.addPhotoThumb(bitmapPicture, String.valueOf(nodeId));
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		};
		
		Thread t = new Thread(r);
		t.start();
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

		Runnable r = new Runnable() {
			public void run() {
				String response = getRequest("dbInterface/api/polls/7449/");
				try {
					JSONObject jObject = new JSONObject(response);
					JSONArray jArray = jObject.getJSONArray("polls");
					
					for (int i=0; i<jArray.length(); i++) {
						JSONObject currentAlbum = jArray.getJSONObject(i);
						
						int nodeId = currentAlbum.getInt("nodeId");
						String question = currentAlbum.getString("question");
						String answers = currentAlbum.getString("answers");
						//String responseBitmap = getRequest("dbInterface/api/photos/" + String.valueOf(nodeId));
						//JSONObject responseJSON = new JSONObject(responseBitmap);
						//String pictureURL = responseJSON.getString("url");
						
						//Bitmap bitmapPicture;
						//bitmapPicture = getPicture();

						pollFragment.addQuestion(question, answers);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		};
		
		Thread t = new Thread(r);
		t.start();		
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
