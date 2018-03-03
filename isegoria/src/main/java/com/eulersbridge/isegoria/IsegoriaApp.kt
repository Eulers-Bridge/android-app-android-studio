package com.eulersbridge.isegoria

import android.annotation.SuppressLint
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.Transformations
import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.os.Build
import android.support.v4.app.NotificationManagerCompat
import androidx.content.systemService
import com.eulersbridge.isegoria.auth.AuthActivity
import com.eulersbridge.isegoria.network.NetworkService
import com.eulersbridge.isegoria.network.api.API
import com.eulersbridge.isegoria.network.api.models.NewsArticle
import com.eulersbridge.isegoria.network.api.models.User
import com.eulersbridge.isegoria.util.data.SingleLiveData
import com.securepreferences.SecurePreferences
import java.util.*

class IsegoriaApp : Application() {

    companion object {
        lateinit var networkService: NetworkService
        private lateinit var securePreferences: SecurePreferences
    }

    val loggedInUser = MutableLiveData<User>()
    var cachedLoginArticles: List<NewsArticle>? = null

    private val loginVisible = MutableLiveData<Boolean>()
    val userVerificationVisible = MutableLiveData<Boolean>()
    val friendsVisible = MutableLiveData<Boolean>()

    private lateinit var loginObserver: Observer<Boolean>

    val api: API by lazy { networkService.api }
    val savedUserEmail: String? by lazy { securePreferences.getString(USER_EMAIL_KEY, null) }
    val savedUserPassword: String? by lazy { securePreferences.getString(USER_PASSWORD_KEY, null) }

    init {
        loginVisible.value = false
        userVerificationVisible.value = false
        friendsVisible.value = false
    }

    override fun onCreate() {
        super.onCreate()

        createNotificationChannels()

        networkService = NetworkService(this)
        securePreferences = SecurePreferences(this)

        val login = login()

        loginObserver = Observer { loginSuccess ->
            if (loginSuccess != null) {
                if (loginSuccess == false) {
                    showLoginScreen()
                } else {
                    showMainActivity()
                }
            }

            login.removeObserver(loginObserver)
        }

        login.observeForever(loginObserver)
    }

    private fun startActivity(activityClass: Class<*>) {
        val activityIntent = Intent(this, activityClass)

        if (Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.O)
            activityIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

        startActivity(activityIntent)
    }

    private fun showMainActivity() {
        startActivity(MainActivity::class.java)
    }

    private fun showLoginScreen() {
        if (loginVisible.value == null || loginVisible.value == false) {
            loginVisible.value = true
            startActivity(AuthActivity::class.java)
        }
    }

    fun hideLoginScreen() {
        if (loginVisible.value == null || loginVisible.value == true)
            loginVisible.value = false
    }

    @SuppressLint("NewApi")
    private fun createNotificationChannels() {
        // Notification channels are only supported on Android O+
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            //Simple map of channel names (Strings) to their importance level (Integer)
            mapOf(
                NOTIFICATION_CHANNEL_FRIENDS to NotificationManager.IMPORTANCE_DEFAULT,
                NOTIFICATION_CHANNEL_VOTE_REMINDERS to NotificationManager.IMPORTANCE_DEFAULT

            ).forEach{ name, importance ->
                val channelId = notificationChannelIDFromName(name)

                val notificationChannel =
                    NotificationChannel(channelId, name, importance)
                notificationChannel.setShowBadge(true)

                /*`createNotificationChannel` is no-op if the channels have already
                been created from a previous launch. */
                notificationManager.createNotificationChannel(notificationChannel)
            }
        }
    }

    @SuppressLint("NewApi")
    private fun setupAppShortcuts() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {

            val election = ShortcutInfo.Builder(this, SHORTCUT_ACTION_ELECTION)
                .setShortLabel(getString(R.string.shortcut_view_latest_election_label_short))
                .setLongLabel(getString(R.string.shortcut_view_latest_election_label_long))
                .setIcon(Icon.createWithResource(this, R.drawable.electionblue))
                .setRank(1)
                .setIntent(
                    Intent(this, MainActivity::class.java)
                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        .setAction(SHORTCUT_ACTION_ELECTION)
                )
                .build()

            val friends = ShortcutInfo.Builder(this, SHORTCUT_ACTION_FRIENDS)
                .setShortLabel(getString(R.string.shortcut_add_friend_label_short))
                .setLongLabel(getString(R.string.shortcut_add_friend_label_long))
                .setIcon(Icon.createWithResource(this, R.drawable.friends))
                .setRank(2)
                .setIntent(
                    Intent(this, MainActivity::class.java)
                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        .setAction(SHORTCUT_ACTION_FRIENDS)
                )
                .build()

            systemService<ShortcutManager>().dynamicShortcuts = Arrays.asList(election, friends)
        }
    }

    fun updateLoggedInUser(updatedUser: User) {
        loggedInUser.value = updatedUser
    }

    fun setLoggedInUser(user: User, password: String) {
        loggedInUser.value = user

        SecurePreferences(applicationContext).edit {
            putString(USER_EMAIL_KEY, user.email)
            putString(USER_PASSWORD_KEY, password)
        }

        setupAppShortcuts()
    }

    fun login(): LiveData<Boolean> {
        if (loggedInUser.value == null) {
            val email = savedUserEmail
            val password = savedUserPassword

            val haveStoredCredentials = email != null && password != null

            return if (haveStoredCredentials) {
                login(email!!, password!!)

            } else {
                return SingleLiveData(false)
            }
        }

        return SingleLiveData(false)
    }

    fun login(email: String, password: String): LiveData<Boolean> {
        val login = networkService.login(email, password)

        return Transformations.switchMap(login) { response ->
            if (response == null) {
                SingleLiveData(false)

            } else {
                val user = response.user
                cachedLoginArticles = response.articles

                if (user != null) {
                    user.id = response.userId

                    hideLoginScreen()

                    if (user.accountVerified) {
                        networkService.updateAPIBaseURL(user)

                    } else {
                        hideLoginScreen()
                        userVerificationVisible.value = true
                    }

                    return@switchMap SingleLiveData(true)

                } else {
                    loginVisible.value = true
                }

                SingleLiveData(false)
            }
        }
    }

    @SuppressLint("NewApi")
    fun logOut() {
        loggedInUser.value = null

        networkService.email = null
        networkService.password = null

        SecurePreferences(applicationContext).edit {
            remove(USER_PASSWORD_KEY)
        }

        // Remove any notifications that are still visible
        NotificationManagerCompat.from(applicationContext).cancelAll()

        // Remove all app long-press shortcuts
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N_MR1)
            systemService<ShortcutManager>().removeAllDynamicShortcuts()

        showLoginScreen()
    }

    fun setTrackingOff(trackingOff: Boolean) {
        loggedInUser.value?.let {
            loggedInUser.value = it.copy(trackingOff = trackingOff)
        }
    }

    fun setOptedOutOfDataCollection(isOptedOutOfDataCollection: Boolean) {
        loggedInUser.value?.let {
            loggedInUser.value = it.copy(isOptedOutOfDataCollection = isOptedOutOfDataCollection)
        }
    }

    fun onUserSelfEfficacyCompleted() {
        loggedInUser.value?.let {
            loggedInUser.value = it.copy(hasPersonality = true)
        }
    }
}
