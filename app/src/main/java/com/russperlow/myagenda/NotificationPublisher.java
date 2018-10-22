package com.russperlow.myagenda;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;

import static android.content.Context.NOTIFICATION_SERVICE;

public class NotificationPublisher extends BroadcastReceiver {

    public static String NOTIFICATION_ID = "notification-id";
    public static String NOTIFICATION = "notification";
    public static String NOTIFICATION_TYPE = "TYPE";
    public static String NOTIFICATION_DETAILS = "DETAILS";
    public static String NOTIFICATION_CLASS = "CLASS";
    public static int FIRST_NOTIFICATION_TIME = 172800000; // Two days (first to go off)
    public static int SECOND_NOTIFICATION_TIME = 86400000; // One day (middle)
    public static int THIRD_NOTIFICATION_TIME = 3600000; // One hour (last)

    public void onReceive(Context context, Intent intent) {

        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Get the id, title and details from the intent
        int id = intent.getIntExtra(NOTIFICATION_ID, 0);
        String title = intent.getStringExtra(NOTIFICATION_TYPE);
        String details = intent.getStringExtra(NOTIFICATION_DETAILS);
        String classStr = intent.getStringExtra(NOTIFICATION_CLASS);

        // Build the basics of the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setSmallIcon(R.drawable.ic_alarm_white_24dp);
        builder.setContentTitle(classStr + " - " + title);
        builder.setContentText(details);
        builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_alarm_black_24dp));
        builder.setTimeoutAfter(5000);

        // Create the intent and pending intent
        Intent intent1 = new Intent(context, getClass());
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 113, intent1, PendingIntent.FLAG_UPDATE_CURRENT);

        // Finish the builder
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(true);
        builder.setFullScreenIntent(pendingIntent, false);

        // Create the manager and notify
        NotificationManager manager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        builder.setLights(0xFFFF00FF, 1000, 1000);
        manager.notify(id, builder.build());
    }
}