package com.eulersbridge.isegoria;

import android.util.Log;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.CreatePlatformEndpointRequest;
import com.amazonaws.services.sns.model.CreatePlatformEndpointResult;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.securepreferences.SecurePreferences;

/**
 * Created by Seb on 24/10/2017.
 */

public class IsegoriaFirebaseInstanceIDService extends FirebaseInstanceIdService {

    private static final String ACCESS_KEY_ID = "AKIAJNFUHYIZGWPMIZWA";
    private static final String SECRET_KEY = "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";

    public static final String SNSPlatformApplicationArn = "arn:aws:sns:ap-southeast-2:715927704730:app/GCM/android_dev";

    private static AmazonSNSClient snsClient = null;

    private static AmazonSNSClient getSNSClient() {
        if (snsClient == null){
            snsClient = new AmazonSNSClient(new BasicAWSCredentials(ACCESS_KEY_ID, SECRET_KEY));
            snsClient.setRegion(Region.getRegion(Regions.AP_SOUTHEAST_2));
        }
        return snsClient;
    }

    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(getString(R.string.TAG), "Refreshed token: " + refreshedToken);

        CreatePlatformEndpointRequest request = new CreatePlatformEndpointRequest();

        request.setToken(refreshedToken);
        request.setPlatformApplicationArn(SNSPlatformApplicationArn);

        CreatePlatformEndpointResult result = getSNSClient().createPlatformEndpoint(request);
        if (result != null) {
            new SecurePreferences(getApplicationContext())
                    .edit()
                    .putString("endpointArn", result.getEndpointArn())
                    .apply();
        }
    }
}
