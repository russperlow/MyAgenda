package com.russperlow.myagenda;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
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
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainFragment extends Fragment{

    public static MainFragment newInstance(){
        return new MainFragment();
    }

    /**
     * The list of agenda items to be displayed
     */
    ListView listView;

    /**
     * The adapter for the list view
     */
    MainAdapter adapter;

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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.main_fragment, container, false);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences((getContext()));

        colorTags.put("Exam", 16711680);
//        colorTags.put("Project", "#FFFF00");
//        colorTags.put("Paper", "#FF00FF");
//        colorTags.put("Exercise", "#00FF00");
//        colorTags.put("Homework", "#0000FF");
//        colorTags.put("Quiz", "#00FFFF");

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
//                final DatePicker datePicker = (DatePicker)addItemView.findViewById(R.id.new_item_date_picker);

                // Alert dialog pop-up when the user wants to add a new item to their agenda
                alertDialogBuilder
                        .setCancelable(false)
                        .setPositiveButton("SAVE", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                items.add(new Item(typeInput.getSelectedItem().toString(), nameInput.getText().toString()));
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
        public View getView(int i, View view, ViewGroup viewGroup) {

            if(view == null){
                view = layoutInflater.inflate(R.layout.item_row, viewGroup, false);
            }

            final ViewHolder viewHolder = new ViewHolder(view);
            final Item item = getItem(i);

            viewHolder.name.setText(item.details);
            viewHolder.date.setText(item.dueDate.toString());
            viewHolder.tag.setColorFilter(Color.RED, PorterDuff.Mode.SRC_ATOP);


            // Will allow for user to long click and edit details of item
            view.setOnLongClickListener(new View.OnLongClickListener(){

                @Override
                public boolean onLongClick(View view) {
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

        private DatePickerDialog.OnDateSetListener dateSetListener =
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int i, int i1, int i2) {
                        // Confirm date with user via toast display
                        Toast.makeText(getActivity(), "selected date is " + view.getYear() +
                                " / " + (view.getMonth()+1) +
                                " / " + view.getDayOfMonth(), Toast.LENGTH_SHORT).show();
                    }
                };
    }

    /**
     * Refreshes the view to reflect a change made
     */
    public void refreshView(){
        listView.invalidateViews();

        adapter.notifyDataSetChanged();
        listView = (ListView)getActivity().findViewById(R.id.items_list);
    }
}
