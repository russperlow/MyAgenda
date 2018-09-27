package com.russperlow.myagenda;

import android.app.Activity;
import android.support.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.security.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class ItemManager {

    interface RetrieveItemsListner{
        void retrieveItems(List<Item> allItems);
    }

    private static final List<Item> allItems = new ArrayList<>();

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

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference reference = database.getReference();
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

    private static List<Item> createItemsFromDatabase(Map<String, Object> items, final Activity activity){

        // List for all items we pull from the database
        List<Item> databaseItems = new ArrayList<>();

        for(Map.Entry<String, Object> entry : items.entrySet()){
            Map<String, Object> allUsers = (Map<String, Object>)entry.getValue(); // Get the users map of KVPair Name - Items List

            Map<String, Object> thisUser = (Map<String, Object>)(allUsers.get("Russ"));

            List<String> itemTypes = (ArrayList)thisUser.get("ItemTypes");
            List<String> classTypes = (ArrayList)thisUser.get("ClassTypes");

            // Get all items
            List<Object> itemList = (ArrayList)thisUser.get("Items");
            for(Object object : itemList){
                Map item = (Map)object;

                // Get all string information
                String className = (String)item.get("classname");
                String details = (String)item.get("details");
                String type = (String)item.get("type");

                // Get the timestamp parse to calendar
                long timestamp = (long)item.get("timestamp");
                Calendar dueDate = Calendar.getInstance(TimeZone.getTimeZone("EST"));
                dueDate.setTimeInMillis(timestamp * 1000);

                databaseItems.add(new Item(className, type, details, dueDate));
            }
        }

        return databaseItems;
    }



}
