package com.russperlow.myagenda;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
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
import android.widget.Toast;
import android.app.PendingIntent;
import android.content.Intent;
import com.google.gson.Gson;

import org.w3c.dom.Text;

import java.lang.reflect.Array;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainFragment extends Fragment{

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
    List<Item> items = new ArrayList<>();

    /**
     * Hashmap of item types to color tags
     */
    Map<String, Integer> colorTags = new HashMap<>();

    /**
     * List of all notification switches
     */
    boolean[] notificationList = new boolean[3];

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        int numItems = sharedPreferences.getInt(ITEMS_COUNT, 0);
        boolean deleteAfterDue = sharedPreferences.getBoolean(getString(R.string.pref_key_delete_after_due), true);
        Calendar nowCalendar = Calendar.getInstance();
        Gson gson = new Gson();

        // Loop through the number of items we stored
        for(int i = 0; i < numItems; i++){
            String json = sharedPreferences.getString(ITEM_PREFIX + i, "");
            Item item = gson.fromJson(json, Item.class);
            if(!deleteAfterDue || item.getCalendar().compareTo(nowCalendar) >= 0)
                items.add(item);
        }
    }

    @Override
    public void onStop(){
        super.onStop();

        // Save all agenda items
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putInt(ITEMS_COUNT, items.size());

        // Loop through all the agenda items and save them
        Gson gson = new Gson();
        for(int i = 0; i < items.size(); i++) {
            Item item = items.get(i);
            String json = gson.toJson(item);
            editor.putString(ITEM_PREFIX + i, json);
        }
        editor.commit();
    }

    @Override
    public void onResume(){
        super.onResume();
        checkSettings();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.main_fragment, container, false);

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
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                LayoutInflater inflater = getActivity().getLayoutInflater();
                View addItemView = inflater.inflate(R.layout.new_item_dialog, null);

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());

                // Set the new item dialog to the builder
                alertDialogBuilder.setView(addItemView);

                // Variables to store the user input that we will add to the list
                final Spinner typeInput = (Spinner)addItemView.findViewById(R.id.new_item_type);
                final EditText nameInput = (EditText)addItemView.findViewById(R.id.new_item_name);
                final TextView dateTimeDisplay = (TextView)addItemView.findViewById(R.id.new_item_date_time_text);

                // Grab the class input and populate it from sharedPrefs
                final Spinner classInput = (Spinner)addItemView.findViewById(R.id.new_item_class);
                classInput.setAdapter(populateClassesSpinner());

                // Create the calendar and formatter
                final Calendar calendar = Calendar.getInstance();
                final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(getString(R.string.date_time_format));

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
                                items.add(new Item(classInput.getSelectedItem().toString(), typeInput.getSelectedItem().toString(), nameInput.getText().toString(), calendar, getActivity(), notificationList));
                                refreshView();

//                                Toast.makeText(getContext(), "Item added & refreshed", Toast.LENGTH_LONG).show();
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
        adapter = new MainAdapter(items);
        listView.setAdapter(adapter);

        return view;
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
        public View getView(final int i, View view, ViewGroup viewGroup) {

            if(view == null){
                view = layoutInflater.inflate(R.layout.item_row, viewGroup, false);
            }

            final ViewHolder viewHolder = new ViewHolder(view);
            final Item thisItem = getItem(i);

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
                                    Item newItem = new Item(classInput.getSelectedItem().toString(), typeInput.getSelectedItem().toString(), nameInput.getText().toString(), calendar, getActivity(), notificationList);
                                    thisItem.onEdit(newItem);
                                    refreshView();
//                                    Toast.makeText(getContext(), "Item added & refreshed", Toast.LENGTH_LONG).show();
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
        Collections.sort(items, new Comparator<Item>() {
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
            items.clear();
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
                getContext(), android.R.layout.simple_spinner_item, getClassList());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return adapter;
    }

    protected List<String> getClassList(){
        // Get the classes and split them all by the commas
        String bigString = sharedPreferences.getString(getResources().getString(R.string.pref_key_classes), getResources().getString(R.string.pref_hint_classes));
        List<String> classesList = Arrays.asList(bigString.split(","));
        return classesList;
    }
}
