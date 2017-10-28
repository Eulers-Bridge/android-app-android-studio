package com.eulersbridge.isegoria;

import android.app.Notification;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by Seb on 27/10/2017.
 */

public class NotificationService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String title;
        String text;

        if (remoteMessage.getNotification() != null) {
            title = remoteMessage.getNotification().getTitle();
            text = remoteMessage.getNotification().getBody();

        } else {
            // Data payload instead of notification object
            title = remoteMessage.getData().get("title");
            text = remoteMessage.getData().get("text");
        }

        //TODO: Determine if notification is friend request or vote reminder, use appropriate notification channel

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this,
                "friends")
                .setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(R.drawable.app_icon);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notificationBuilder.setCategory(Notification.CATEGORY_SOCIAL);
            notificationBuilder.setVisibility(Notification.VISIBILITY_PUBLIC);
        }

        int id = (int)System.currentTimeMillis();
        Notification notification = notificationBuilder.build();

        NotificationManagerCompat manager = NotificationManagerCompat.from(getApplicationContext());
        manager.notify(id, notification);
    }
}