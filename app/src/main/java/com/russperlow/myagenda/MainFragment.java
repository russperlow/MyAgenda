package com.russperlow.myagenda;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainFragment extends Fragment
    implements  ItemManager.RetrieveItemsListener,
                ItemManager.RetrieveClassesListener,
                ItemManager.RetrieveItemTypesListener{

    public static MainFragment newInstance(){
        return new MainFragment();
    }

    /**
     * The string const for number of agenda items
     */
    private final String ITEMS_COUNT = "ITEMS_COUNT";

    /**
     * The prefix for items we have stored/will store
     */
    private final String ITEM_PREFIX = "ITEM_";

    /**
     * The list of agenda items to be displayed
     */
    ListView listView;

    /**
     * The adapter for the list view
     */
    MainAdapter adapter;

    /**
     * Date set listener to save the date when making new item
     */
    static DatePickerDialog.OnDateSetListener dateSetListener;

    /**
     * Time set listener to save the time when making a new item
     */
    static TimePickerDialog.OnTimeSetListener timeSetListener;

    /**
     * Global SharedPrefernces
     */
    SharedPreferences sharedPreferences;

    /**
     * Layout inflater for the current view
     */
    LayoutInflater layoutInflater;

    /**
     * List of items on the user's agenda
     */
    List<Item> allItems = new ArrayList<>();

    /**
     * Hashmap of item types to color tags
     */
    Map<String, Integer> colorTags = new HashMap<>();

    /**
     * List of all notification switches
     */
    boolean[] notificationList = new boolean[3];

    /**
     * The spinner we use for sorting elements
     */
    Spinner sortSpinner;

    /**
     * The variable used for checking the delete element after due setting
     */
    boolean deleteAfterDue;

    /**
     * The class names pulled from firebase
     */
    List<String> classes = new ArrayList<>();

    /**
     * The types of items pulled from firebase
     */
    List<String> itemTypes = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStop(){
        super.onStop();

        // Save all agenda items
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putInt(ITEMS_COUNT, allItems.size());

        // Loop through all the agenda items and save them
        Gson gson = new Gson();
        for(int i = 0; i < allItems.size(); i++) {
            Item item = allItems.get(i);
            String json = gson.toJson(item);
            editor.putString(ITEM_PREFIX + i, json);
        }
        editor.commit();
    }

    @Override
    public void onResume(){
        super.onResume();
        checkSettings();
        refreshView();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.main_fragment, container, false);

        // Init all references to the database (classes, item types, items)
        ItemManager.initDatabaseRefs(this, this, this, getActivity());

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences((getContext()));

        colorTags.put("Exam", Color.RED);
        colorTags.put("Project", Color.BLUE);
        colorTags.put("Paper", Color.GREEN);
        colorTags.put("Exercise", Color.YELLOW);
        colorTags.put("Homework", Color.CYAN);
        colorTags.put("Quiz", Color.MAGENTA);

        // Add item fab init
        FloatingActionButton addItemFAB = (FloatingActionButton)view.findViewById(R.id.add_item_fab);
        addItemFAB.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                View addItemView = inflater.inflate(R.layout.new_item_dialog, null);

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());

                // Set the new item dialog to the builder
                alertDialogBuilder.setView(addItemView);

                // Variables to store the user input that we will add to the list
                final Spinner typeInput = (Spinner)addItemView.findViewById(R.id.new_item_type);
                typeInput.setAdapter(populateTypeSpinner());

                final EditText nameInput = (EditText)addItemView.findViewById(R.id.new_item_name);
                final TextView dateTimeDisplay = (TextView)addItemView.findViewById(R.id.new_item_date_time_text);

                // Grab the class input and populate it from sharedPrefs
                final Spinner classInput = (Spinner)addItemView.findViewById(R.id.new_item_class);
                classInput.setAdapter(populateClassesSpinner());

                // Create the calendar and formatter
                final Calendar calendar = Calendar.getInstance();
                final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(getString(R.string.date_time_format));

                // Default the time to 11:59pm since most assignments are due then
                calendar.set(Calendar.HOUR_OF_DAY, 23);
                calendar.set(Calendar.MINUTE, 59);

                // Display the date and time
                dateTimeDisplay.setText(simpleDateFormat.format(calendar.getTime()));

                // Set the date when the datepickerdialog is saved
                dateSetListener = new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, month);
                        calendar.set(Calendar.DAY_OF_MONTH, day);
                        dateTimeDisplay.setText(simpleDateFormat.format(calendar.getTime()));
                    }
                };

                // Set the time when the timepickerdialog is saved
                timeSetListener = new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                        calendar.set(Calendar.HOUR_OF_DAY, hour);
                        calendar.set(Calendar.MINUTE, minute);
                        dateTimeDisplay.setText(simpleDateFormat.format(calendar.getTime()));
                    }
                };

                // Alert dialog pop-up when the user wants to add a new item to their agenda
                alertDialogBuilder
                        .setCancelable(false)
                        .setPositiveButton(R.string.SAVE, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                allItems.add(new Item(classInput.getSelectedItem().toString(), typeInput.getSelectedItem().toString(), nameInput.getText().toString(), calendar, getActivity(), notificationList));
                                ItemManager.updateDatabase(allItems);
                                refreshView();

                            }
                        })
                        .setNegativeButton(R.string.CANCEL, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        });

                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            }
        });

        layoutInflater = getActivity().getLayoutInflater();
        listView = (ListView)view.findViewById(R.id.items_list);
        adapter = new MainAdapter(allItems);
        listView.setAdapter(adapter);

        // Spinner for sorting agenda items
        sortSpinner = (Spinner)view.findViewById(R.id.sort_spinner);
        sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {
                String type = parent.getItemAtPosition(position).toString();
                if(type.equals(getResources().getStringArray(R.array.drop_down_array_all)[0])) {
                    adapter.items = allItems;
                }else{
                    List<Item> filteredByType = new ArrayList<>();
                    for(Item item : allItems){
                        if(item.getType().equals(type)){
                            filteredByType.add(item);
                        }
                    }
                    adapter.items = filteredByType;
                }
                refreshView();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        return view;
    }

    @Override
    public void retrieveItems(final List<Item> retrievedItems) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                allItems = retrievedItems;
                adapter.items = allItems;
                sortItems();
                refreshView();
            }
        });
    }

    @Override
    public void retrieveClasses(final List<String> retrievedClasses) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                classes = retrievedClasses;
            }
        });
    }

    @Override
    public void retrieveItemTypes(final List<String> retrievedItemTypes) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                itemTypes = retrievedItemTypes;
            }
        });
    }

    class MainAdapter extends BaseAdapter{

        List<Item> items;

        public MainAdapter(List<Item> items){
            this.items = items;
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Item getItem(int i) {
            return items.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public int getViewTypeCount() {
            return 1;//getCount();
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

        @Override
        public View getView(final int i, View view, ViewGroup viewGroup) {

            if(view == null){
                view = layoutInflater.inflate(R.layout.item_row, viewGroup, false);
            }

            final ViewHolder viewHolder = new ViewHolder(view);
            final Item thisItem = getItem(i);

            if(!deleteAfterDue && thisItem.hasPassed()){
                view.setBackgroundColor(Color.GRAY);
            }
            else{
                view.setBackgroundColor(Color.WHITE);
            }

            viewHolder.name.setText(thisItem.getClassStr() + " - " + thisItem.getDetails());
            viewHolder.date.setText(thisItem.getDueDate());
            viewHolder.tag.setColorFilter(colorTags.get(thisItem.getType()), PorterDuff.Mode.SRC_ATOP);
            
            // Will allow for user to long click and edit details of item
            view.setOnLongClickListener(new View.OnLongClickListener(){

                @Override
                public boolean onLongClick(View view) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    LayoutInflater inflater = getActivity().getLayoutInflater();
                    View editItemView = inflater.inflate(R.layout.new_item_dialog, null);
                    final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());

                    // Set the new item dialog to the builder
                    alertDialogBuilder.setView(editItemView);

                    // Variables to store the user input that we will add to the list
                    final Spinner typeInput = (Spinner)editItemView.findViewById(R.id.new_item_type);
                    final EditText nameInput = (EditText)editItemView.findViewById(R.id.new_item_name);
                    final TextView dateTimeDisplay = (TextView)editItemView.findViewById(R.id.new_item_date_time_text);
                    final Button deleteItemButton = (Button)editItemView.findViewById(R.id.new_item_delete);

                    // Show the delete button on edit
                    deleteItemButton.setVisibility(View.VISIBLE);

                    // Grab the class input and populate it from sharedPrefs
                    final Spinner classInput = (Spinner)editItemView.findViewById(R.id.new_item_class);
                    classInput.setAdapter(populateClassesSpinner());

                    // Create the formatter
                    final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(getString(R.string.date_time_format));

                    // Set all of the information to match the item we are editing
                    final Calendar calendar = thisItem.getCalendar();
                    List<String> arrayList = Arrays.asList(getResources().getStringArray(R.array.drop_down_array));
                    typeInput.setSelection(arrayList.indexOf(thisItem.getType()));
                    classInput.setSelection(getClassList().indexOf(thisItem.getClassStr()));
                    nameInput.setText(thisItem.getDetails());
                    dateTimeDisplay.setText(thisItem.getDueDate());

                    // Display the date and time
                    dateTimeDisplay.setText(simpleDateFormat.format(calendar.getTime()));

                    // Set the date when the datepickerdialog is saved
                    dateSetListener = new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                            calendar.set(Calendar.YEAR, year);
                            calendar.set(Calendar.MONTH, month);
                            calendar.set(Calendar.DAY_OF_MONTH, day);
                            dateTimeDisplay.setText(simpleDateFormat.format(calendar.getTime()));
                        }
                    };

                    // Set the time when the timepickerdialog is saved
                    timeSetListener = new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                            calendar.set(Calendar.HOUR_OF_DAY, hour);
                            calendar.set(Calendar.MINUTE, minute);
                            dateTimeDisplay.setText(simpleDateFormat.format(calendar.getTime()));
                        }
                    };

                    // Alert dialog pop-up when the user wants to add a new item to their agenda
                    alertDialogBuilder
                            .setCancelable(false)
                            .setPositiveButton(R.string.SAVE, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    thisItem.onEdit(classInput.getSelectedItem().toString(),
                                            typeInput.getSelectedItem().toString(),
                                            nameInput.getText().toString(),
                                            calendar,
                                            true,
                                            getActivity());
                                    ItemManager.updateDatabase(allItems);
                                    refreshView();
                                }
                            })
                            .setNegativeButton(R.string.CANCEL, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.cancel();
                                }
                            });

                    final AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();

                    // Delete button onClick listener, deletes this object from the list
                    deleteItemButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            AlertDialog.Builder confirmDeleteBuilder = new AlertDialog.Builder(getActivity());
                            confirmDeleteBuilder
                                    .setCancelable(false)
                                    .setMessage(R.string.new_item_confirm_delete_message)
                                    .setPositiveButton(R.string.YES, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int j) {
                                            items.remove(i);
                                            ItemManager.updateDatabase(allItems);
                                            refreshView();
                                            alertDialog.cancel();
                                        }
                                    })
                                    .setNegativeButton(R.string.NO, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            dialogInterface.cancel();
                                        }
                                    });
                            AlertDialog confirmDeleteDialog = confirmDeleteBuilder.create();
                            confirmDeleteDialog.show();

                        }
                    });
                    return false;
                }
            });


            return view;
        }

        class ViewHolder{
            TextView name, date;
            ImageView tag;

            public ViewHolder(View view){
                name = (TextView)view.findViewById(R.id.item_name);
                date = (TextView)view.findViewById(R.id.item_date);
                tag = (ImageView)view.findViewById(R.id.item_tag);
            }

        }

    }

    public static class DatePickerFragment extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            return new DatePickerDialog(getActivity(), dateSetListener, year, month, day);
        }
    }

    public static class TimePickerFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState){
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            return new TimePickerDialog(getActivity(), timeSetListener, hour, minute, false);
        }
    }


    /**
     * Sorts all the items by date
     */
    protected void sortItems(){
        Collections.sort(allItems, new Comparator<Item>() {
            @Override
            public int compare(Item item1, Item item2) {
                return item1.getCalendar().compareTo(item2.getCalendar());
            }
        });
    }

    /**
     * Refreshes the view to reflect a change made
     */
    protected void refreshView(){
        listView.invalidateViews();

        sortItems();
        adapter.notifyDataSetChanged();
        listView = (ListView)getActivity().findViewById(R.id.items_list);

    }

    /**
     * Checks the settings every time we reload the screen to see if anything changed
     */
    protected void checkSettings(){
        notificationList[0] = sharedPreferences.getBoolean(getResources().getString(R.string.pref_key_one_hour_notify), true);
        notificationList[1] = sharedPreferences.getBoolean(getResources().getString(R.string.pref_key_one_day_notify), true);
        notificationList[2] = sharedPreferences.getBoolean(getResources().getString(R.string.pref_key_two_days_notify), true);

        boolean deleteAll = sharedPreferences.getBoolean(getResources().getString(R.string.pref_key_delete_all), false);
        if(deleteAll == true){
            allItems.clear();
            ItemManager.updateDatabase(allItems);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(getResources().getString(R.string.pref_key_delete_all), false);
            editor.commit();
            refreshView();
        }
    }

    /**
     * Populates the spinner, when adding a new item, with all the classes option
     */
    protected SpinnerAdapter populateClassesSpinner(){


        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                getContext(), android.R.layout.simple_spinner_item, classes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return adapter;
    }

    protected List<String> getClassList(){
        // Get the classes and split them all by the commas
        String bigString = sharedPreferences.getString(getResources().getString(R.string.pref_key_classes), getResources().getString(R.string.pref_hint_classes));
        List<String> classesList = Arrays.asList(bigString.split(","));
        return classesList;
    }

    /**
     * Populates the spinner, when adding a new item, with all the classes option
     */
    protected SpinnerAdapter populateTypeSpinner(){

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                getContext(), android.R.layout.simple_spinner_item, itemTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return adapter;
    }

    protected List<String> getPrefArrayAsList(int countKey, int prefix, int defaultArray, boolean includeAll){

        int count = sharedPreferences.getInt(getString(countKey), 0);
        List<String> list = new ArrayList<>();

        if(includeAll)
            list.add(getString(R.string.item_type_all));

        if(count > 0) {
            for (int i = 0; i < count; i++) {
                list.add(sharedPreferences.getString(getString(prefix) + i, ""));
            }
        }
        else{
            list = Arrays.asList(getResources().getStringArray(defaultArray));
        }
        return list;
    }
}
