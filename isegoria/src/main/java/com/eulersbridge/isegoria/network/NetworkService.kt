package com.eulersbridge.isegoria.network

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Transformations
import android.content.Context
import com.amazonaws.auth.CognitoCachingCredentialsProvider
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3Client
import com.eulersbridge.isegoria.IsegoriaApp
import com.eulersbridge.isegoria.SNS_PLATFORM_APPLICATION_ARN
import com.eulersbridge.isegoria.addAppHeaders
import com.eulersbridge.isegoria.auth.signup.SignUpUser
import com.eulersbridge.isegoria.network.api.API
import com.eulersbridge.isegoria.network.api.models.User
import com.eulersbridge.isegoria.network.api.responses.LoginResponse
import com.eulersbridge.isegoria.onSuccess
import com.eulersbridge.isegoria.util.data.RetrofitLiveData
import com.eulersbridge.isegoria.util.data.SingleLiveData
import com.google.firebase.iid.FirebaseInstanceId
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.util.*
import javax.inject.Singleton

@Singleton
class NetworkService constructor(
    private val app: IsegoriaApp,
    private val appContext: Context,
    private val httpClient: OkHttpClient,
    private val api: API,
    private val networkConfig: NetworkConfig
) {

    private var email: String? = null
    private var password: String? = null

    fun setUserCredentials(email: String?, password: String?) {
        this.email = email
        this.password = password

        if (email.isNullOrBlank() && password.isNullOrBlank())
            networkConfig.resetBaseUrl()
    }

    // Updates the base URL of the API by fetching the API root for the user's institution
    fun updateAPIBaseURL(user: User) {
        user.institutionId?.let { institutionId ->

            api.getInstitution(institutionId).onSuccess {
                it.getName()?.let { institutionName ->

                    api.getInstitutionURLs().onSuccess { urls ->
                        // Find the matching ClientInstitution, and use its `apiRoot`

                        urls.singleOrNull {
                            it.name == institutionName && !it.apiRoot.isNullOrBlank()

                        }?.apiRoot?.let {
                            networkConfig.baseUrl = it + "api/"

                            app.setLoggedInUser(user, password!!)
                        }
                    }
                }
            }
        }
    }

    fun login(email: String, password: String): LiveData<LoginResponse?> {
        this.email = email
        this.password = password

        AuthenticationInterceptor.username = email
        AuthenticationInterceptor.password = password

        val deviceToken = FirebaseInstanceId.getInstance().token

        return if (deviceToken != null) {
            RetrofitLiveData(api.login(SNS_PLATFORM_APPLICATION_ARN, deviceToken))

        } else {
            SingleLiveData(null)
        }
    }

    private fun jsonObjectOf(vararg pairs: Pair<String, Any?>) = JSONObject().apply {
        for ((key, value) in pairs)
            put(key, value)
    }

    fun signUp(user: SignUpUser): LiveData<Boolean> {

        lateinit var jsonObject: JSONObject

        try {
            jsonObject = jsonObjectOf(
                    "email" to user.email,
                    "givenName" to user.givenName,
                    "familyName" to user.familyName,
                    "gender" to user.gender,
                    "nationality" to user.nationality,
                    "yearOfBirth" to user.yearOfBirth,
                    "accountVerified" to user.accountVerified.toString(),
                    "password" to user.password,
                    "institutionId" to user.institutionId.toString(),
                    "hasPersonality" to user.hasPersonality.toString()
            )

        } catch (e: JSONException) {
            e.printStackTrace()
            return SingleLiveData(false)
        }

        val json = MediaType.parse("application/json; charset=utf-8")
        val requestBody = RequestBody.create(json, jsonObject.toString())

        val request = okhttp3.Request.Builder()
            .url(networkConfig.baseUrl + "signUp")
            .addAppHeaders()
            .post(requestBody)
            .build()

        // Create new HTTP client rather than using application's, as no auth is required
        return OkHttpLiveData(OkHttpClient().newCall(request))
    }

    private fun fileExtension(imageFile: File): String {
        val dotIndex = imageFile.path.lastIndexOf('.')

        return if (dotIndex > -1) {
            imageFile.path.substring(dotIndex + 1)
        } else {
            "jpg"
        }
    }

    fun uploadNewUserPhoto(imageFile: File): LiveData<Boolean> {
        val credentialsProvider = CognitoCachingCredentialsProvider(
            appContext, // Context,
            "715927704730",
            "us-east-1:73ae30c9-393c-44cf-a0ac-049cc0838428",
            "arn:aws:iam::715927704730:role/Cognito_isegoriaUnauth_Role",
            "arn:aws:iam::715927704730:role/Cognito_isegoriaAuth_Role",
            Regions.US_EAST_1 // Region
        )

        val s3Client = AmazonS3Client(credentialsProvider)

        val transferUtility = TransferUtility.builder()
            .s3Client(s3Client)
            .context(appContext)
            .build()

        val imageFileExtension = fileExtension(imageFile)
        val key = "${UUID.randomUUID()}.$imageFileExtension"

        val transfer =
            AWSTransferLiveData(transferUtility.upload(networkConfig.s3PicturesBucketName, key, imageFile))

        return Transformations.switchMap(transfer) {
            if (it == TransferState.COMPLETED) {
                updateDisplayPicturePhoto(networkConfig.s3PicturesPath + key)
            } else {
                SingleLiveData(false)
            }
        }
    }

    private fun updateDisplayPicturePhoto(pictureURL: String): LiveData<Boolean> {
        val timestamp = System.currentTimeMillis() / 1000L
        val loggedInUser = app.loggedInUser.value ?: return SingleLiveData(false)

        lateinit var jsonObject: JSONObject
        try {
            jsonObject = jsonObjectOf(
                    "url" to pictureURL,
                    "thumbNailUrl" to pictureURL,
                    "title" to "Profile Picture",
                    "description" to "Profile Picture",
                    "date" to timestamp.toString(),
                    "ownerId" to loggedInUser.email,
                    "sequence" to "0"
            )

        } catch (e: JSONException) {
            e.printStackTrace()
            return SingleLiveData(false)
        }

        val json = MediaType.parse("application/json; charset=utf-8")
        val requestBody = RequestBody.create(json, jsonObject.toString())

        val request = okhttp3.Request.Builder()
            .url(networkConfig.baseUrl + "photo")
            .addAppHeaders()
            .post(requestBody)
            .build()

        return OkHttpLiveData(httpClient.newCall(request))
    }
}
