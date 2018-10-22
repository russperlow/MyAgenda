package com.russperlow.myagenda;

import android.app.Activity;
import android.support.annotation.NonNull;

import com.google.android.gms.common.util.ArrayUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.security.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class ItemManager {

    interface RetrieveItemsListner{
        void retrieveItems(List<Item> allItems);
    }

    private static final List<Item> allItems = new ArrayList<>();
    private static final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private static final DatabaseReference reference = database.getReference();

    private static final String DATABASE_ITEMS = "Items";
    private static final String DATABASE_CLASS_TYPES = "ClassTypes";
    private static final String DATABASE_ITEM_TYPES = "ItemTypes";
    private static final String DATABASE_USERS = "Users";
    private static final String DATABASE_MY_USER = "Russ";

    public static List<Item> getItems(final RetrieveItemsListner listner, final Activity activity){

        // Listener for getting items from the database
        final RetrieveItemsListner _listener = new RetrieveItemsListner() {
            @Override
            public void retrieveItems(List<Item> allItems){
                List<Item> copy = new ArrayList<>();
                copy.addAll(allItems);
                listner.retrieveItems(copy);
            }
        };

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                allItems.addAll(createItemsFromDatabase((Map<String, Object>)dataSnapshot.getValue(), activity));
                _listener.retrieveItems(createItemsFromDatabase((Map<String, Object>)dataSnapshot.getValue(), activity));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return allItems;
    }

    public static void updateDatabase(List<Item> items){
        Map<String, Object> children = new HashMap<>();

        List<Object> objects = new ArrayList<>();
        for(Item item : items){
            objects.add(convertItem(item));
        }

        children.put(DATABASE_ITEMS, objects);

        reference.child(DATABASE_USERS).child(DATABASE_MY_USER).updateChildren(children);
    }

    /**
     * Creates agenda items from the Firebase
     *
     * @param items
     * @param activity
     *
     * @return
     */
    private static List<Item> createItemsFromDatabase(Map<String, Object> items, final Activity activity){

        // List for all items we pull from the database
        List<Item> databaseItems = new ArrayList<>();

        for(Map.Entry<String, Object> entry : items.entrySet()){
            Map<String, Object> allUsers = (Map<String, Object>)entry.getValue(); // Get the users map of KVPair Name - Items List

            Map<String, Object> thisUser = (Map<String, Object>)(allUsers.get(DATABASE_MY_USER));

            List<String> itemTypes = (ArrayList)thisUser.get(DATABASE_ITEM_TYPES);
            List<String> classTypes = (ArrayList)thisUser.get(DATABASE_CLASS_TYPES);

            // Get all items
            List<Object> itemList = (ArrayList)thisUser.get(DATABASE_ITEMS);
            for(Object object : itemList){
                Map item = (Map)object;

                // Get all string information
                String className = (String)item.get("classname");
                String details = (String)item.get("details");
                String type = (String)item.get("type");

                // Get the timestamp parse to calendar
                long timestamp = (long)item.get("timestamp");
                Calendar dueDate = Calendar.getInstance();
                dueDate.setTimeInMillis(timestamp);

                List<Object> notificationArray = (ArrayList)item.get("notificationIds");
                int[] notificationIds = new int[notificationArray.size()];
                for(int i = 0; i < notificationArray.size(); i++){
                    notificationIds[i] = (int)((long)notificationArray.get(i));
                }

                databaseItems.add(new Item(className, type, details, dueDate, activity, notificationIds));
            }
        }

        return databaseItems;
    }

    private static SaveItemState convertItem(Item item){
        return new SaveItemState(item.getClassStr(), item.getDetails(), item.getType(), item.getCalendar().getTimeInMillis(), item.getNotificationIds());
    }

    private static class SaveItemState{

        public String classname, details, type;
        public long timestamp;
        public List<Integer> notificationIds = new ArrayList<>();

        public SaveItemState(){

        }

        public SaveItemState(String classname, String details, String type, long timestamp, int[] notificationIds){
            this.classname = classname;
            this.details = details;
            this.type = type;
            this.timestamp = timestamp;

            for(int i = 0; i < notificationIds.length; i++){
                this.notificationIds.add(notificationIds[i]);
            }
        }
    }


}
