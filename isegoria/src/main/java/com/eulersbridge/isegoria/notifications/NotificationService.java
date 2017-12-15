package com.eulersbridge.isegoria.notifications;

import android.app.Notification;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.text.TextUtils;

import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.common.Constant;
import com.eulersbridge.isegoria.common.Utils;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

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

            if (TextUtils.isEmpty(title) && TextUtils.isEmpty(text)) {
                title = remoteMessage.getData().get("default");
            }
        }

        if (TextUtils.isEmpty(title) && TextUtils.isEmpty(text)) return;

        //TODO: Determine if notification is friend request or vote reminder, use appropriate notification channel

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this,
                Utils.notificationChannelIDFromName(Constant.NOTIFICATION_CHANNEL_FRIENDS))
                .setContentTitle(title)
                .setSmallIcon(R.mipmap.notification_icon);

        if (!TextUtils.isEmpty(text)) {
            notificationBuilder.setContentText(text);
        }

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