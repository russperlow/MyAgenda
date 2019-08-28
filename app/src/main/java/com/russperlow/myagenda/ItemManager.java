package com.russperlow.myagenda;

import android.app.Activity;
import android.support.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemManager {

    interface RetrieveItemsListener {
        void retrieveItems(List<Item> allItems);
    }

    interface RetrieveClassesListener{
        void retrieveClasses(List<String> allClasses);
    }

    interface RetrieveItemTypesListener{
        void retrieveItemTypes(List<String> allItemTypes);
    }

    // Database URL strings
    private static final String DATABASE_ITEMS = "Items";
    private static final String DATABASE_CLASS_TYPES = "ClassTypes";
    private static final String DATABASE_ITEM_TYPES = "ItemTypes";
    private static final String DATABASE_USERS = "Users";
    private static final String DATABASE_MY_USER = "Russ";

    private static final List<Item> allItems = new ArrayList<>();

    // Firebase Database references
    private static final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private static final DatabaseReference reference = database.getReference();
    private static DatabaseReference allUsersRef;
    private static DatabaseReference myUserRef;
    private static DatabaseReference myItemsRef;
    private static DatabaseReference myClassTypesRef;
    private static DatabaseReference myItemTypesRef;

    public static void initDatabaseRefs(final RetrieveItemsListener itemsListener, final RetrieveClassesListener classesListener, final RetrieveItemTypesListener itemTypesListener, final Activity activity){
        allUsersRef = reference.child(DATABASE_USERS);
        myUserRef = allUsersRef.child(DATABASE_MY_USER);
        myItemsRef = myUserRef.child(DATABASE_ITEMS);
        myClassTypesRef = myUserRef.child(DATABASE_CLASS_TYPES);
        myItemTypesRef = myUserRef.child(DATABASE_ITEM_TYPES);

        initItemRef(itemsListener, activity);
        initClassTypeRef(classesListener);
        initItemTypeRef(itemTypesListener);
    }

    /**
     * Inits reference for the items in firebase
     *
     * @param listener For items
     * @param activity For initializing items
     */
    private static void initItemRef(final RetrieveItemsListener listener, final Activity activity){

        // Listener to send items to MainFragment
        final RetrieveItemsListener _listener = new RetrieveItemsListener() {
            @Override
            public void retrieveItems(List<Item> _allItems){
                List<Item> copy = new ArrayList<>();

                if(copy.size() <= 0){return;}

                copy.addAll(_allItems);
                allItems.clear();
                allItems.addAll(_allItems);
                listener.retrieveItems(copy);
            }
        };

        // Listener for changes in database /Items/
        myItemsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                _listener.retrieveItems(createItemsFromDatabase((List<Object>)dataSnapshot.getValue(), activity));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    /**
     * Inits reference for the classes in firebase
     *
     * @param listener For classes
     */
    private static void initClassTypeRef(final RetrieveClassesListener listener){

        // Listener to send classTypes to MainFragment
        final RetrieveClassesListener _listener = new RetrieveClassesListener() {
            @Override
            public void retrieveClasses(List<String> _allClasses) {
                listener.retrieveClasses(_allClasses);
            }
        };

        // Listener for changes in database /ClassTypes/
        myClassTypesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                _listener.retrieveClasses(createClassesFromDatabase((List<Object>)dataSnapshot.getValue()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    /**
     * Inits reference for the item types in firebase
     *
     * @param listener For item types
     */
    private static void initItemTypeRef(final RetrieveItemTypesListener listener){

        // Listener to send itemTypes to MainFragment
        final RetrieveItemTypesListener _listener = new RetrieveItemTypesListener() {
            @Override
            public void retrieveItemTypes(List<String> _allItemTypes) {
                listener.retrieveItemTypes(_allItemTypes);
            }
        };

        // Listener for changes in database /ItemTypes/
        myItemTypesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                _listener.retrieveItemTypes(createItemTypesFromDatabase((List<Object>)dataSnapshot.getValue()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
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


    public static void updateItem(Item item){
        Map<String, Object> child = new HashMap<>();
        child.put(Integer.toString(allItems.size()), convertItem(item));

        reference.child(DATABASE_USERS).child(DATABASE_MY_USER).child(DATABASE_ITEMS).updateChildren(child);
    }

    /**
     * Create our agenda items from the database
     *
     * @param items Item objects as stored in Firebase
     * @param activity Activity used for creating notifications
     *
     * @return all items stored in Firebase
     */
    private static List<Item> createItemsFromDatabase(List<Object> items, final Activity activity){

        // List for all items we pull from the database
        List<Item> databaseItems = new ArrayList<>();

        if(databaseItems.size() <= 0)
            return null;

        // Loop through the given objects and parse them over to items
        for(Object object : items){

            // Used for cases when there is a gap in items (ie. 1, 2, 4, 5)
            if(object == null)
                continue;

            // Get this specific item as a map
            Map item = (Map)object;

            // Get all string information
            String className = (String)item.get("classname");
            String details = (String)item.get("details");
            String type = (String)item.get("type");

            // Get the timestamp parse to calendar
            long timestamp = (long)item.get("timestamp");
            Calendar dueDate = Calendar.getInstance();
            dueDate.setTimeInMillis(timestamp);

            // Get the notification ids
            List<Object> notificationArray = (ArrayList)item.get("notificationIds");
            int[] notificationIds = new int[notificationArray.size()];
            for(int i = 0; i < notificationArray.size(); i++) {
                notificationIds[i] = (int) ((long) notificationArray.get(i));
            }

            // Add this to the list we will return
            databaseItems.add(new Item(className, type, details, dueDate, activity, notificationIds));
        }

        return databaseItems;
    }

    /**
     * Create classes from firebase
     *
     * @param objects Class names to parse
     *
     * @return Class names as list of strings
     */
    private static List<String> createClassesFromDatabase(List<Object> objects){
        List<String> allClasses = new ArrayList<>();

        for(Object object : objects){
            allClasses.add(object.toString());
        }

        return allClasses;
    }

    /**
     * Create classes from firebase
     *
     * @param objects Item type names to parse
     *
     * @return Item types as list of strings
     */
    private static List<String> createItemTypesFromDatabase(List<Object> objects){
        List<String> allItemTypes = new ArrayList<>();

        for(Object object : objects){
            allItemTypes.add(object.toString());
        }

        return allItemTypes;
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
