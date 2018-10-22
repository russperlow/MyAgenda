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

    // The class this item belongs to
    private String classStr;

    // The type of this list item
    private String type;

    // The details of this item
    private String details;

    // The date this item is due
    private Calendar dueDate;

    // UUID for Notification
    private String uniqueId = "";

    // Collection of when notifications should be sent off
    private boolean[] notificationBools;

    public Item(String classStr, String type, String details, Calendar dueDate, Activity activity, boolean[] notificationBools){
        this.classStr = classStr;
        this.type = type;
        this.details = details;
        this.dueDate = dueDate;
        generateUUID();

        this.notificationBools = new boolean[notificationBools.length];
        for(int i = 0; i < notificationBools.length; i++){
            this.notificationBools[i] = notificationBools[i];
        }

        scheduleNotification(activity);
    }

    public Item(String classStr, String type, String details, Calendar dueDate){
        this.classStr = classStr;
        this.type = type;
        this.details = details;
        this.dueDate = dueDate;
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
     * @return the class of this item
     */
    public String getClassStr(){
        return classStr;
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
     * If an item is edited, we need to update out fields
     *
     * @param newItem the item we are sent with all the new information
     */
    public void onEdit(Item newItem){
        this.classStr = newItem.getClassStr();
        this.type = newItem.getType();
        this.details = newItem.getDetails();
        this.dueDate = newItem.getCalendar();
    }

    /**
     * Schedules 1 or more notifications based on the due date
     * @param activity to link this notification to
     */
    public void scheduleNotification(Activity activity){

        for(int i = 0; i < notificationBools.length; i++) {
            int thisId = NotificationID.nextValue();
            Intent notificationIntent = new Intent(activity, NotificationPublisher.class);
            notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_ID, thisId);
            notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_TYPE, type);
            notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_CLASS, classStr);
            notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_DETAILS, details);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(activity, thisId, notificationIntent, 0);

            long alarmTime = dueDate.getTimeInMillis();

            switch (i){
                case 0:
                    alarmTime -= 3600000; // 1 Hour before
                    break;
                case 1:
                    alarmTime -= 86400000; // 1 Day before
                    break;
                case 2:
                    alarmTime -= 172800000; // 2 Day's before
                    break;
            }

            AlarmManager alarmManager = (AlarmManager) activity.getSystemService(Context.ALARM_SERVICE);
            alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);
        }
    }

}