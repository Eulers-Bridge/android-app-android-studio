package com.eulersbridge.isegoria

import android.annotation.SuppressLint
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
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
import com.eulersbridge.isegoria.inject.DaggerAppComponent
import com.eulersbridge.isegoria.util.extension.notificationChannelIDFromName
import com.eulersbridge.isegoria.util.extension.systemService
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import dagger.android.HasServiceInjector
import dagger.android.support.HasSupportFragmentInjector
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.BehaviorSubject
import java.util.*
import javax.inject.Inject

class IsegoriaApp : MultiDexApplication(), AppRouter, HasActivityInjector, HasSupportFragmentInjector, HasServiceInjector {

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
    lateinit var repository: Repository

    private val compositeDisposable = CompositeDisposable()

    val userVerificationScreenVisible = BehaviorSubject.createDefault(false)!!
    val friendsScreenVisible = BehaviorSubject.createDefault(false)!!

    override fun setUserVerificationScreenVisible(visible: Boolean) {
        userVerificationScreenVisible.onNext(visible)
    }

    override fun setFriendsScreenVisible(visible: Boolean) {
        friendsScreenVisible.onNext(visible)
    }

    override fun onCreate() {
        super.onCreate()

        DaggerAppComponent
            .builder()
            .application(this)
            .build()
            .inject(this)

        createNotificationChannels()

        repository.loginState.subscribe {
            when (it) {
                is LoginState.LoggedIn -> {
                    createAppShortcuts()
                }

                is LoginState.LoggedOut -> {
                    // Remove any notifications that are still visible
                    NotificationManagerCompat.from(this).cancelAll()

                    // Remove all app long-press shortcuts
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N_MR1)
                        systemService<ShortcutManager>().removeAllDynamicShortcuts()
                }
            }
        }.addTo(compositeDisposable)
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
}
