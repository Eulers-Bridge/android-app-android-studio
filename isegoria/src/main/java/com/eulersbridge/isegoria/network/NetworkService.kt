package com.eulersbridge.isegoria.network

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Transformations
import com.amazonaws.auth.CognitoCachingCredentialsProvider
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3Client
import com.eulersbridge.isegoria.BuildConfig
import com.eulersbridge.isegoria.IsegoriaApp
import com.eulersbridge.isegoria.SNS_PLATFORM_APPLICATION_ARN
import com.eulersbridge.isegoria.auth.signup.SignUpUser
import com.eulersbridge.isegoria.isNetworkAvailable
import com.eulersbridge.isegoria.network.adapters.LenientLongAdapter
import com.eulersbridge.isegoria.network.adapters.NullPrimitiveAdapter
import com.eulersbridge.isegoria.network.adapters.TimestampAdapter
import com.eulersbridge.isegoria.network.api.API
import com.eulersbridge.isegoria.network.api.models.ClientInstitution
import com.eulersbridge.isegoria.network.api.models.Institution
import com.eulersbridge.isegoria.network.api.models.User
import com.eulersbridge.isegoria.network.api.responses.LoginResponse
import com.eulersbridge.isegoria.util.data.RetrofitLiveData
import com.eulersbridge.isegoria.util.data.SingleLiveData
import com.eulersbridge.isegoria.util.network.SimpleCallback
import com.google.firebase.iid.FirebaseInstanceId
import com.squareup.moshi.Moshi
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.File
import java.util.*

class NetworkService(private val application: IsegoriaApp) {

    companion object {
        private const val SERVER_URL = "http://54.79.70.241:8080/dbInterface/api/"

        private const val S3_PICTURES_BUCKET_NAME = "isegoriauserpics"
        private const val S3_PICTURES_PATH = "https://s3.amazonaws.com/isegoriauserpics/"

        private var apiBaseURL = SERVER_URL

        private val moshiConverterFactory: MoshiConverterFactory

        private var needsSetup = true

        init {
            val moshi = Moshi.Builder()
                .add(LenientLongAdapter())
                .add(NullPrimitiveAdapter())
                .add(TimestampAdapter())
                .build()

            moshiConverterFactory = MoshiConverterFactory.create(moshi)
        }
    }

    private val cacheDirectory = File(application.cacheDir, "network")
    private var httpClient: OkHttpClient? = null

    internal lateinit var api: API

    internal var email: String? = null
    internal var password: String? = null

    private fun setup() {
        if (needsSetup) {
            createHttpClient()
            createAPI()
        }
    }

    private fun createAPI() {
        val retrofit = Retrofit.Builder()
            .client(httpClient!!)
            .baseUrl(apiBaseURL)
            .addConverterFactory(UnwrapConverterFactory())
            .addConverterFactory(moshiConverterFactory)
            .build()

        api = retrofit.create(API::class.java)
    }

    private fun createHttpClient() {
        // Force caching of GET requests for 1 minute, or 5 minutes if no network connection.
        val interceptor = Interceptor { chain ->
            val request = chain.request()

            val response = chain.proceed(request)

            if (request.method() == "GET") {
                val cacheHeaderValue = if (application.isNetworkAvailable())
                    "public, max-age=60" // 60 seconds / 1 minute
                else
                    "public, only-if-cached, max-stale=300" // 300 seconds / 5 minutes

                response.newBuilder()
                    .removeHeader("Pragma")
                    .removeHeader("Cache-Control")
                    .header("Cache-Control", cacheHeaderValue)
                    .build()
            }

            response
        }

        val cacheSize = 40 * 1024 * 1024 // Maximum cache size of 40 MiB
        val cache = Cache(cacheDirectory, cacheSize.toLong())

        val httpClientBuilder = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Accept", "application/json")
                    .addHeader("Content-Type", "application/json")
                    .addHeader("User-Agent", "IsegoriaApp Android")

                chain.proceed(request.build())
            }
            .addInterceptor(interceptor)
            .addNetworkInterceptor(interceptor)
            .cache(cache)

        if (BuildConfig.DEBUG) {
            val logging = HttpLoggingInterceptor()
            logging.level = HttpLoggingInterceptor.Level.BASIC

            //For more detailed debug logging, uncomment the following line:
            //logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            httpClientBuilder.addInterceptor(logging)
        }

        if (!password.isNullOrBlank())
            httpClientBuilder.addInterceptor(AuthenticationInterceptor(email!!, password!!))

        httpClient = httpClientBuilder.build()
    }

    // Updates the base URL of the API by fetching the API root for the user's institution
    fun updateAPIBaseURL(user: User) {
        user.institutionId?.let { institutionId ->

            api.getInstitution(institutionId).enqueue(object : SimpleCallback<Institution>() {
                override fun handleResponse(response: retrofit2.Response<Institution>) {
                    response.body()?.getName()?.let { institutionName ->

                        api.getInstitutionURLs()
                            .enqueue(object : SimpleCallback<List<ClientInstitution>>() {
                                override fun handleResponse(response: retrofit2.Response<List<ClientInstitution>>) {
                                    val institution = response.body()?.singleOrNull {
                                        it.name == institutionName && !it.apiRoot.isNullOrBlank()
                                    }

                                    institution?.let {
                                        apiBaseURL = it.apiRoot!! + "api/"

                                        // Recreate the API with the new base URL
                                        //createAPI(institution.apiRoot);
                                        createAPI()

                                        application.setLoggedInUser(user, password!!)
                                    }
                                }
                            })
                    }
                }
            })
        }
    }

    fun login(email: String, password: String): LiveData<LoginResponse?> {
        this.email = email
        this.password = password
        needsSetup = true

        setup()

        val deviceToken = FirebaseInstanceId.getInstance().token

        return if (deviceToken != null) {
            val snsPlatformArn = SNS_PLATFORM_APPLICATION_ARN
            RetrofitLiveData(api.login(snsPlatformArn, deviceToken))

        } else {
            SingleLiveData(null)
        }
    }

    fun signUp(user: SignUpUser): LiveData<Boolean> {
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("email", email!!)
            .addFormDataPart("givenName", user.givenName)
            .addFormDataPart("familyName", user.familyName)
            .addFormDataPart("gender", user.gender)
            .addFormDataPart("nationality", user.nationality)
            .addFormDataPart("yearOfBirth", user.yearOfBirth)
            .addFormDataPart("accountVerified", user.accountVerified.toString())
            .addFormDataPart("password", password!!)
            .addFormDataPart("institutionId", user.institutionId.toString())
            .addFormDataPart("hasPersonality", user.hasPersonality.toString())
            .build()

        val request = okhttp3.Request.Builder()
            .url(SERVER_URL + "signUp")
            .method("POST", requestBody)
            .addHeader("Accept", "application/json")
            .addHeader("Content-type", "application/json")
            .addHeader("User-Agent", "IsegoriaApp Android")
            .post(requestBody)
            .build()

        // Create new HTTP client rather than using application's, as no auth is required
        val responseBody = OkHttpLiveData(OkHttpClient().newCall(request))

        return Transformations.switchMap(responseBody) {
            SingleLiveData(it != null && it.contains(email!!))
        }
    }

    fun uploadNewUserPhoto(imageFile: File): LiveData<Boolean> {
        val credentialsProvider = CognitoCachingCredentialsProvider(
            application.applicationContext, // Context,
            "715927704730",
            "us-east-1:73ae30c9-393c-44cf-a0ac-049cc0838428",
            "arn:aws:iam::715927704730:role/Cognito_isegoriaUnauth_Role",
            "arn:aws:iam::715927704730:role/Cognito_isegoriaAuth_Role",
            Regions.US_EAST_1 // Region
        )

        val s3Client = AmazonS3Client(credentialsProvider)

        val transferUtility = TransferUtility.builder()
            .s3Client(s3Client)
            .context(application.applicationContext)
            .build()

        val dotIndex = imageFile.path.lastIndexOf('.')

        val imageFileExtension = if (dotIndex > -1) {
            imageFile.path.substring(dotIndex + 1)
        } else {
            "jpg"
        }

        val key = "${UUID.randomUUID()}.$imageFileExtension"

        val transfer =
            AWSTransferLiveData(transferUtility.upload(S3_PICTURES_BUCKET_NAME, key, imageFile))

        return Transformations.switchMap(transfer) {
            if (it == TransferState.COMPLETED) {
                updateDisplayPicturePhoto(S3_PICTURES_PATH + key)
            } else {
                SingleLiveData(false)
            }
        }
    }

    private fun updateDisplayPicturePhoto(pictureURL: String): LiveData<Boolean> {
        setup()

        val timestamp = System.currentTimeMillis() / 1000L
        val loggedInUser = application.loggedInUser.value ?: return SingleLiveData(false)

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("url", pictureURL)
            .addFormDataPart("thumbNailUrl", pictureURL)
            .addFormDataPart("title", "Profile Picture")
            .addFormDataPart("description", "Profile Picture")
            .addFormDataPart("date", timestamp.toString())
            .addFormDataPart("ownerId", loggedInUser.email)
            .addFormDataPart("sequence", "0")
            .build()

        val request = okhttp3.Request.Builder()
            .url(apiBaseURL + "photo")
            .method("PUT", requestBody)
            .addHeader("Accept", "application/json")
            .addHeader("Content-type", "application/json")
            .addHeader("User-Agent", "IsegoriaApp Android")
            .post(requestBody)
            .build()

        val updateRequest = OkHttpLiveData(httpClient!!.newCall(request))
        return Transformations.switchMap(updateRequest) { bodyString ->
            SingleLiveData(bodyString != null)
        }
    }
}
