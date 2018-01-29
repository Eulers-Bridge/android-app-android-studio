package com.eulersbridge.isegoria.notifications

import android.app.Notification
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import com.eulersbridge.isegoria.NOTIFICATION_CHANNEL_FRIENDS
import com.eulersbridge.isegoria.R
import com.eulersbridge.isegoria.util.Strings
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class NotificationService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
        super.onMessageReceived(remoteMessage)

        var title: String?
        val text: String?

        if (remoteMessage?.notification != null) {
            title = remoteMessage.notification!!.title
            text = remoteMessage.notification!!.body

        } else {
            // Data payload instead of notification object
            title = remoteMessage!!.data["title"]
            text = remoteMessage.data["text"]

            if (title.isNullOrBlank() && text.isNullOrBlank())
                title = remoteMessage.data["default"]
        }

        if (!title.isNullOrBlank() && !text.isNullOrBlank())
            createNotification(title!!, text!!)

        //TODO: Determine if notification is friend request or vote reminder, use appropriate notification channel
    }

    private fun createNotification(title: String, text: String) {
        val notificationBuilder = NotificationCompat.Builder(this,
                Strings.notificationChannelIDFromName(NOTIFICATION_CHANNEL_FRIENDS))
                .setContentTitle(title)
                .setSmallIcon(R.drawable.notification_icon)

        if (!text.isBlank())
            notificationBuilder.setContentText(text)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notificationBuilder.setCategory(Notification.CATEGORY_SOCIAL)
            notificationBuilder.setVisibility(Notification.VISIBILITY_PUBLIC)
        }

        val id = System.currentTimeMillis().toInt()
        val notification = notificationBuilder.build()

        val manager = NotificationManagerCompat.from(applicationContext)
        manager.notify(id, notification)
    }
}