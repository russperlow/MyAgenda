package com.russperlow.myagenda;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static android.content.Context.NOTIFICATION_SERVICE;


// General class that will be used for all item types
public class Item {
    // The type of this list item
    private String type;

    // The details of this item
    private String details;

    // The date this item is due
    private Calendar dueDate;

    // UUID for Notification
    private String uniqueId = "";

    public Item(String type, String details, Calendar dueDate, Activity activity){
        this.type = type;
        this.details = details;
        this.dueDate = dueDate;
        generateUUID();
        scheduleNotification(activity);
    }

    /**
     * Generates a UUID for the notification
     */
    private void generateUUID(){
        uniqueId = UUID.randomUUID().toString();
    }

    /**
     * @return the dueDate as a formatted string
     */
    public String getDueDate(){
        return new SimpleDateFormat("MM/dd/yyyy 'at' hh:mm a").format(dueDate.getTime());
    }

    /**
     * @return the dueDate as a calendar object
     */
    public Calendar getCalendar(){
        return dueDate;
    }

    /**
     * @return the type of this item
     */
    public String getType(){
        return type;
    }

    /**
     * @return the details/description of this item
     */
    public String getDetails(){
        return details;
    }

    /**
     * Schedules 1 or more notifications based on the due date
     * @param activity to link this notification to
     */
    public void scheduleNotification(Activity activity){
        Intent notificationIntent = new Intent(activity, NotificationPublisher.class);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_ID, uniqueId);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_TYPE, type);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_DETAILS, details);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(activity, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        long alarmTime = dueDate.getTimeInMillis() - 3600000;
        AlarmManager alarmManager = (AlarmManager)activity.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);
    }

}