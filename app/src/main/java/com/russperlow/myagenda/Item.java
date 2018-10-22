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
import android.util.Log;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

    // The ids of all notifications
    private int[] notificationIds;

    /**
     * Constructor for creating a perminent
     * @param classStr
     * @param type
     * @param details
     * @param dueDate
     * @param activity
     * @param notificationBools
     */
    public Item(String classStr, String type, String details, Calendar dueDate, Activity activity, boolean[] notificationBools){
        this.classStr = classStr;
        this.type = type;
        this.details = details;
        this.dueDate = dueDate;
        generateUUID();

        this.notificationBools = new boolean[notificationBools.length];
        this.notificationIds = new int[notificationBools.length];

        for(int i = 0; i < notificationBools.length; i++){
            this.notificationBools[i] = notificationBools[i];
            this.notificationIds[i] = -1;
        }

        scheduleAllNotifications(activity);
    }

//    public Item(String classStr, String type, String details, Calendar dueDate, Activity activity, int[] notificationIds){
//        this.classStr = classStr;
//        this.type = type;
//        this.details = details;
//        this.dueDate = dueDate;
//        this.notificationIds = notificationIds;
//
//        notificationBools = new boolean[3];
//        notificationBools[0] = notificationBools[1] = notificationBools[2] = true;
//
//        editAllNotifications(activity);
//    }

    public Item(String classStr, String type, String details, Calendar dueDate, int[] notificationIds){
        this.classStr = classStr;
        this.type = type;
        this.details = details;
        this.dueDate = dueDate;
        this.notificationIds = notificationIds;

        notificationBools = new boolean[3];
        notificationBools[0] = notificationBools[1] = notificationBools[2] = true;
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
     * @return the notification ids of this item
     */
    public int[] getNotificationIds() {
        return notificationIds;
    }

    public boolean hasPassed(){
        return dueDate.compareTo(Calendar.getInstance()) < 0;
    }

    /**
     * If an item is edited, we need to update out fields
     *
     * @param classStr the class string
     * @param type the type this item is
     * @param details the details of this item
     * @param dueDate the calendar this is due at
     * @param updateNotifications whether or not we should update notifications
     * @param activity used if we need to update notifications
     */
    public void onEdit(String classStr, String type, String details, Calendar dueDate, boolean updateNotifications, Activity activity){
        this.classStr = classStr;
        this.type = type;
        this.details = details;
        this.dueDate = dueDate;

        // If a notification setting or the due date was changed, we update notifications
        if(updateNotifications){
            Log.i("ITEM", "On Edit ~ Update");
            editAllNotifications(activity);
        }
    }

    /**
     * Schedules all notifications by default
     *
     * @param activity To get the system service from
     */
    private void scheduleAllNotifications(Activity activity){
        for(int i = 0; i < notificationBools.length; i++){
            // Create and set an id for all notifications, true or false, in case changes in settings are made
            int thisId = NotificationID.nextValue();
            notificationIds[i] = thisId;

            // If this notification is set to true, we make a notification for it
            if(notificationBools[i]){
                createNotification(activity, thisId, getAlarmTime(i));
            }
        }
        Log.i("ITEM", "Schedule all notifications");
    }

    /**
     * Will edit all notifications if a notification setting or the due date was changed
     *
     * @param activity Activity to get the Alarm Service from
     */
    private void editAllNotifications(Activity activity){
        for(int i = 0; i < notificationIds.length; i++){

            // If this notification should go, recreate it with the same id to override the old one
            if(notificationBools[i]){
                editNotification(activity, notificationIds[i], getAlarmTime(i));
            }
            else{
                cancelNotification(activity, notificationIds[i]);
            }
        }
        Log.i("ITEM", "Edit all notifications");
    }

    /**
     * Will "edit" the notification by cancelling and recreating it
     *
     * @param activity Activity to get the Alarm Service from
     * @param requestId Unique ID of the notification
     * @param alarmTime Time we want the notification to go off
     */
    private void editNotification(Activity activity, int requestId, long alarmTime){
        cancelNotification(activity, requestId);
        createNotification(activity, requestId, alarmTime);
    }

    /**
     * Cancels the notification with the given id
     *
     * @param activity Activity to get system service from
     * @param requestId Unique ID of the notification
     */
    private void cancelNotification(Activity activity, int requestId){
        AlarmManager alarmManager = (AlarmManager)activity.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(activity, NotificationPublisher.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(activity, requestId, intent, 0);
        alarmManager.cancel(pendingIntent);
        Log.i("ITEM", "Cancel Notification");
    }

    /**
     * Creates a notification with the given information
     *
     * @param activity Activity to get system service from
     * @param requestId Unique ID of the notification
     * @param alarmTime Time to publish this notification at
     */
    private void createNotification(Activity activity, int requestId, long alarmTime){
        // Build the notifications intent and pass it into a pending intent
        Intent notificationIntent = new Intent(activity, NotificationPublisher.class);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_ID, requestId);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_TYPE, type);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_CLASS, classStr);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_DETAILS, details);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(activity, requestId, notificationIntent, 0);

        // Make the alarm using the pending intent
        AlarmManager alarmManager = (AlarmManager) activity.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);
        Log.i("ITEM", "Create Notification");
    }

    /**
     * Returns the time we would like the notification to go off
     *
     * @param index used to determine the time
     *
     * @return the time the notification should go off
     */
    private long getAlarmTime(int index){
        // Using the due date set the notifications according to the switch statement
        long alarmTime = dueDate.getTimeInMillis();
        switch (index){
            case 0:
                return (alarmTime - NotificationPublisher.THIRD_NOTIFICATION_TIME); // 1 Hour before
            case 1:
                return (alarmTime - NotificationPublisher.SECOND_NOTIFICATION_TIME); // 1 Day before
            case 2:
                return (alarmTime - NotificationPublisher.FIRST_NOTIFICATION_TIME); // 2 Days before
        }
        return alarmTime;
    }
}