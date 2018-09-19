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

    public void onReceive(Context context, Intent intent) {

        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Get the id, title and details from the intent
        int id = intent.getIntExtra(NOTIFICATION_ID, 0);
        String title = intent.getStringExtra(NOTIFICATION_TYPE);
        String details = intent.getStringExtra(NOTIFICATION_DETAILS);

        // Build the basics of the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setSmallIcon(R.drawable.ic_alarm_white_24dp);
        builder.setContentTitle(title);
        builder.setContentText(details);
        builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_alarm_black_24dp));

        // Create the intent and pending intent
        Intent intent1 = new Intent(context, getClass());
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 113, intent1, PendingIntent.FLAG_UPDATE_CURRENT);

        // Finish the builder
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(true);
        builder.setFullScreenIntent(pendingIntent, false);
        builder.setTimeoutAfter(10000);

        // Create the manager and notify
        NotificationManager manager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        manager.notify(id, builder.build());

    }
}