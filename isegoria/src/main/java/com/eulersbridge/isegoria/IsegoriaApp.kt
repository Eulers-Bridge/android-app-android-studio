package com.eulersbridge.isegoria

import android.annotation.SuppressLint
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.os.Build
import android.support.multidex.MultiDexApplication
import android.support.v4.app.Fragment
import android.support.v4.app.NotificationManagerCompat
import androidx.core.content.systemService
import com.eulersbridge.isegoria.auth.AuthActivity
import com.eulersbridge.isegoria.inject.DaggerAppComponent
import com.eulersbridge.isegoria.network.NetworkService
import com.eulersbridge.isegoria.network.api.models.NewsArticle
import com.eulersbridge.isegoria.network.api.models.User
import com.securepreferences.SecurePreferences
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import dagger.android.HasServiceInjector
import dagger.android.support.HasSupportFragmentInjector
import io.reactivex.Single
import io.reactivex.rxkotlin.subscribeBy
import java.util.*
import javax.inject.Inject
import kotlin.reflect.KClass

class IsegoriaApp : MultiDexApplication(), HasActivityInjector, HasSupportFragmentInjector, HasServiceInjector {

    @Inject
    lateinit var activityInjector: DispatchingAndroidInjector<Activity>

    @Inject
    lateinit var fragmentInjector: DispatchingAndroidInjector<Fragment>

    @Inject
    lateinit var serviceInjector: DispatchingAndroidInjector<Service>

    override fun activityInjector() = activityInjector
    override fun supportFragmentInjector() = fragmentInjector
    override fun serviceInjector() = serviceInjector

    @Inject
    lateinit var networkService: NetworkService

    @Inject
    lateinit var securePreferences: SecurePreferences

    val loggedInUser = MutableLiveData<User>()
    var cachedLoginArticles: List<NewsArticle>? = null

    private val loginVisible = MutableLiveData<Boolean>()
    val userVerificationVisible = MutableLiveData<Boolean>()
    val friendsVisible = MutableLiveData<Boolean>()

    val savedUserEmail: String? by lazy { securePreferences.getString(USER_EMAIL_KEY, null) }
    val savedUserPassword: String? by lazy { securePreferences.getString(USER_PASSWORD_KEY, null) }

    init {
        loginVisible.value = false
        userVerificationVisible.value = false
        friendsVisible.value = false
    }

    override fun onCreate() {
        super.onCreate()

        DaggerAppComponent
            .builder()
            .application(this)
            .build()
            .inject(this)

        createNotificationChannels()

        val login = login()
        createLoginObserver(login)
    }

    private fun createLoginObserver(loginSuccess: Single<Boolean>) {
        loginSuccess.subscribeBy(
                onSuccess = {
                    if (it) {
                        showMainActivity()
                    } else {
                        showLoginScreen()
                    }
                },
                onError = {
                    it.printStackTrace()
                    showLoginScreen()
                }
        )
    }

    private fun startActivity(activityClass: KClass<*>) {
        val activityIntent = Intent(this, activityClass.java)
        activityIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(activityIntent)
    }

    private fun showMainActivity() {
        startActivity(MainActivity::class)
    }

    private fun showLoginScreen() {
        if (loginVisible.value == null || loginVisible.value == false) {
            loginVisible.value = true
            startActivity(AuthActivity::class)
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
                systemService<NotificationManager>(Context.NOTIFICATION_SERVICE)

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
                notificationManager?.createNotificationChannel(notificationChannel)
            }
        }
    }

    @SuppressLint("NewApi")
    private fun createAppShortcuts() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {

            val election = ShortcutInfo.Builder(this, SHORTCUT_ACTION_ELECTION)
                .setShortLabel(getString(R.string.shortcut_view_latest_election_label_short))
                .setLongLabel(getString(R.string.shortcut_view_latest_election_label_long))
                .setIcon(Icon.createWithResource(this, R.drawable.election_blue))
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
        loggedInUser.postValue(user)

        securePreferences.edit {
            putString(USER_EMAIL_KEY, user.email)
            putString(USER_PASSWORD_KEY, password)
        }

        createAppShortcuts()
    }

    fun login(): Single<Boolean> {
        if (loggedInUser.value == null) {
            val email = savedUserEmail
            val password = savedUserPassword

            val haveStoredCredentials = email != null && password != null

            return if (haveStoredCredentials) {
                login(email!!, password!!)

            } else {
                return Single.just(false)
            }
        }

        return Single.just(false)
    }

    fun login(email: String, password: String): Single<Boolean> {
        val loginSuccess = networkService.login(email, password)
                .map { response ->
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

                        return@map true

                    } else {
                        loginVisible.value = true
                    }

                    false
                }

        createLoginObserver(loginSuccess)

        return loginSuccess
    }

    @SuppressLint("NewApi")
    fun logOut() {
        loggedInUser.value = null

        networkService.setUserCredentials(null, null)

        securePreferences.edit {
            remove(USER_PASSWORD_KEY)
        }

        // Remove any notifications that are still visible
        NotificationManagerCompat.from(this).cancelAll()

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
