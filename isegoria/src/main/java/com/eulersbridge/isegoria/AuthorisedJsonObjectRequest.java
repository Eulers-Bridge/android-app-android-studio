package com.eulersbridge.isegoria;

import android.util.Base64;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * JsonObjectRequest subclass to add username and password authorization headers.
 */
class AuthorisedJsonObjectRequest extends JsonObjectRequest {
    static String username;
    static String password;

    AuthorisedJsonObjectRequest(int method, String url, JSONObject jsonRequest, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        super(method, url, jsonRequest, listener, errorListener);
    }

    AuthorisedJsonObjectRequest(String url, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        super(url, null, listener, errorListener);
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        HashMap<String, String> headers = new HashMap<>();
        String credentials = username + ":" + password;
        String base64EncodedCredentials =
                Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
        headers.put("Authorization", "Basic " + base64EncodedCredentials);
        headers.put("Accept", "application/json");
        headers.put("Content-type", "application/json; charset=utf-8");
        return headers;
    }
}