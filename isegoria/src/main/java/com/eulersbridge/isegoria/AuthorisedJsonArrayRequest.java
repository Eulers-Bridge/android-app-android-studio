package com.eulersbridge.isegoria;

import android.util.Base64;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;

import java.util.HashMap;
import java.util.Map;

/**
 * JsonObjectRequest subclass to add username and password authorization headers.
 */
class AuthorisedJsonArrayRequest extends JsonArrayRequest {
    static String username;
    static String password;

    AuthorisedJsonArrayRequest(int method, String url, JSONArray jsonRequest, Response.Listener<JSONArray> listener, Response.ErrorListener errorListener) {
        super(method, url, jsonRequest, listener, errorListener);
    }

    AuthorisedJsonArrayRequest(String url, Response.Listener<JSONArray> listener, Response.ErrorListener errorListener) {
        super(url, listener, errorListener);
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