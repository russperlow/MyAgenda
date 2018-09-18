package com.russperlow.myagenda;

import android.annotation.SuppressLint;
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
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.gson.Gson;

import org.w3c.dom.Text;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        int numItems = sharedPreferences.getInt(ITEMS_COUNT, 0);
        Gson gson = new Gson();

        // Loop through the number of items we stored
        for(int i = 0; i < numItems; i++){
            String json = sharedPreferences.getString(ITEM_PREFIX + i, "");
            Item item = gson.fromJson(json, Item.class);
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

                final Calendar calendar = Calendar.getInstance();

                // Format the date
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
                        .setPositiveButton("SAVE", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                items.add(new Item(typeInput.getSelectedItem().toString(), nameInput.getText().toString(), calendar));
                                refreshView();
                                Toast.makeText(getContext(), "Item added & refreshed", Toast.LENGTH_LONG);
                            }
                        })
                        .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
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
            final Item item = getItem(i);

            viewHolder.name.setText(item.getDetails());
            viewHolder.date.setText(item.getDueDate());
            viewHolder.tag.setColorFilter(colorTags.get(item.getType()), PorterDuff.Mode.SRC_ATOP);


            // Will allow for user to long click and edit details of item
            view.setOnLongClickListener(new View.OnLongClickListener(){

                @Override
                public boolean onLongClick(View view) {

                    // Build alert dialog for deletion of long clicked item
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("Delete Item")
                            .setMessage("Are you sure you want to delete this agenda item?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int j) {
                                    items.remove(i);
                                    refreshView();
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.cancel();
                                }
                            });

                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();

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
    public void sortItems(){
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
    public void refreshView(){
        listView.invalidateViews();

        sortItems();
        adapter.notifyDataSetChanged();
        listView = (ListView)getActivity().findViewById(R.id.items_list);
    }
}
