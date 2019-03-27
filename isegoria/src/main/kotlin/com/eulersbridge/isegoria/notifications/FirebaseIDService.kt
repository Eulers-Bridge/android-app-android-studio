package com.eulersbridge.isegoria.notifications

import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.amazonaws.services.sns.AmazonSNSClient
import com.amazonaws.services.sns.model.CreatePlatformEndpointRequest
import com.eulersbridge.isegoria.ENDPOINT_ARN_KEY
import com.eulersbridge.isegoria.SNS_PLATFORM_APPLICATION_ARN
import com.eulersbridge.isegoria.util.extension.edit
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.FirebaseInstanceIdService
import com.securepreferences.SecurePreferences
import dagger.android.AndroidInjection
import javax.inject.Inject



class FirebaseIDService : FirebaseInstanceIdService() {

    @Inject
    internal lateinit var securePreferences: SecurePreferences

    override fun onCreate() {
        AndroidInjection.inject(this)
        super.onCreate()
    }

    override fun onTokenRefresh() {
        super.onTokenRefresh()

        // Get updated InstanceID token.
        val refreshedToken = FirebaseInstanceId.getInstance().token
        // Log.d(javaClass.simpleName, "Refreshed token: " + refreshedToken);

        val request = CreatePlatformEndpointRequest()
        request.apply {
            token = refreshedToken
            platformApplicationArn = SNS_PLATFORM_APPLICATION_ARN
        }

//        snsClient.createPlatformEndpoint(request)?.let { result ->
//            securePreferences.edit {
//                putString(ENDPOINT_ARN_KEY, result.endpointArn)
//            }
//        }
    }

    companion object {


//        private val snsClient by lazy {
//            val client = AmazonSNSClient(BasicAWSCredentials(ACCESS_KEY_ID, SECRET_KEY))
//            client.setRegion(Region.getRegion(Regions.AP_SOUTHEAST_2))
//            client
//        }
    }
}
