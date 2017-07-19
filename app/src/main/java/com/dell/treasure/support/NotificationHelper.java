package com.dell.treasure.support;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.Color;
import android.support.v7.app.NotificationCompat;

import com.dell.treasure.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by DELL on 2016/12/2.
 */

public class NotificationHelper {
    public static final int NOTICE_ID = 10001;
    public static final int NOTICE_ID1 = 10002;

    public static void sendExpandedNotice(Context context, String title, String content, int smallIcon,
                                          PendingIntent activeIntent,PendingIntent passiveIntent) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setContentTitle(title)
                .setContentText(content)
                .setContentIntent(passiveIntent)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(smallIcon)
                .setAutoCancel(true)
                .setPriority(Notification.PRIORITY_MAX)
                .setDefaults(Notification.DEFAULT_ALL)
                .setCategory(Notification.CATEGORY_MESSAGE)
                .setVisibility(Notification.VISIBILITY_PUBLIC);

        builder.addAction(R.drawable.ic_check,"接收",activeIntent);
        builder.addAction(R.drawable.ic_refuse,"拒绝",passiveIntent);
        Notification notification = builder.build();
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(NOTICE_ID, notification);
    }


    public static void sendDefaultNotice(Context context, String title, String content, int smallIcon, PendingIntent intent) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setContentTitle(title)
                .setContentText(content)
                .setContentIntent(intent)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(smallIcon)
                .setAutoCancel(true)
                .setPriority(Notification.PRIORITY_MAX)
                .setDefaults(Notification.DEFAULT_ALL)
                .setCategory(Notification.CATEGORY_MESSAGE)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setFullScreenIntent(intent,true);
        Notification notification = builder.build();
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(NOTICE_ID, notification);
    }


    public static int getIconColor(){
        return Color.parseColor("#999999");

    }

    private static String getTime() {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm", Locale.SIMPLIFIED_CHINESE);
        return format.format(new Date());
    }

    public static void clearNotification(Context context, int noticeId) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(noticeId);
    }
}
