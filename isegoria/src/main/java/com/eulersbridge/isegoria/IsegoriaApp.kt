package com.eulersbridge.isegoria

import android.annotation.SuppressLint
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.os.Build
import android.support.v4.app.NotificationManagerCompat
import com.eulersbridge.isegoria.network.NetworkService
import com.eulersbridge.isegoria.network.api.API
import com.eulersbridge.isegoria.network.api.models.NewsArticle
import com.eulersbridge.isegoria.network.api.models.User
import com.eulersbridge.isegoria.personality.PersonalityQuestionsActivity
import com.eulersbridge.isegoria.util.Strings
import com.eulersbridge.isegoria.util.data.SingleLiveData
import com.eulersbridge.isegoria.util.transformation.BlurTransformation
import com.eulersbridge.isegoria.util.transformation.RoundedCornersTransformation
import com.securepreferences.SecurePreferences
import java.util.*

class IsegoriaApp : Application() {

    companion object {
        lateinit var networkService: NetworkService
        private lateinit var securePreferences: SecurePreferences
    }

    val loggedInUser = MutableLiveData<User>()
    var cachedLoginArticles: List<NewsArticle>? = null

    val loginVisible = MutableLiveData<Boolean>()
    val userVerificationVisible = MutableLiveData<Boolean>()
    val friendsVisible = MutableLiveData<Boolean>()

    val api: API by lazy { networkService.api!! }
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

        val screenDensity = resources.displayMetrics.density
        BlurTransformation.screenDensity = screenDensity
        RoundedCornersTransformation.screenDensity = screenDensity
    }

    @SuppressLint("NewApi")
    private fun createNotificationChannels() {
        // Notification channels are only supported on Android O+
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

            //Build a simple map of channel names (Strings) to their importance level (Integer)
            val channels = arrayOf(
                Pair(NOTIFICATION_CHANNEL_FRIENDS, NotificationManager.IMPORTANCE_DEFAULT),
                Pair(NOTIFICATION_CHANNEL_VOTE_REMINDERS, NotificationManager.IMPORTANCE_DEFAULT)
            )

            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            /* Loop through the map, creating notification channels based on the names/importance
            values in the map. `createNotificationChannel` is no-op if the channels have already
            been created from a previous launch. */
            for ((name, importance) in channels) {
                val channelId = Strings.notificationChannelIDFromName(name)

                val notificationChannel =
                    NotificationChannel(channelId, name, importance)
                notificationChannel.setShowBadge(true)
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

            val shortcutManager = getSystemService(ShortcutManager::class.java)
            shortcutManager.dynamicShortcuts = Arrays.asList(election, friends)
        }
    }

    fun updateLoggedInUser(updatedUser: User) {
        loggedInUser.value = updatedUser
    }

    fun setLoggedInUser(user: User, password: String) {
        loggedInUser.value = user

        SecurePreferences(applicationContext)
            .edit()
            .putString(USER_EMAIL_KEY, user.email)
            .putString(USER_PASSWORD_KEY, password)
            .apply()

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
                loginVisible.value = true
                SingleLiveData(false)
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

                    if (user.accountVerified) {
                        networkService.updateAPIBaseURL(user)

                        if (!user.hasPersonality)
                            startActivity(Intent(this, PersonalityQuestionsActivity::class.java))

                    } else {
                        userVerificationVisible.value = true
                    }

                    SingleLiveData(true)

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

        SecurePreferences(applicationContext)
            .edit()
            .remove(USER_PASSWORD_KEY)
            .apply()

        // Remove any notifications that are still visible
        NotificationManagerCompat.from(applicationContext).cancelAll()

        // Remove all app long-press shortcuts
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N_MR1) {
            val shortcutManager = getSystemService(ShortcutManager::class.java)
            shortcutManager.removeAllDynamicShortcuts()
        }

        loginVisible.value = true
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
