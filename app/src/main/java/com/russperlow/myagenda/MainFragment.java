package com.russperlow.myagenda;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

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

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.main_fragment, container, false);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences((getContext()));

        // Add item fab init
        FloatingActionButton addItemFAB = (FloatingActionButton)view.findViewById(R.id.add_item_fab);
        addItemFAB.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                final Dialog dialog = new Dialog(getContext());
                dialog.setContentView(R.layout.new_item_dialog);
                dialog.setTitle("Add Item");

                dialog.show();
            }
        });


        List<Item> itemList = new ArrayList<>();
        for(int i = 0; i < 20; i++){
            Item item1 = new Item(ItemType.EXAM, "DSA Exam " + i);
            itemList.add(item1);
        }

        layoutInflater = getActivity().getLayoutInflater();
        listView = (ListView)view.findViewById(R.id.items_list);
        adapter = new MainAdapter(itemList);
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

            public ViewHolder(View view){
                name = (TextView)view.findViewById(R.id.item_name);
                date = (TextView)view.findViewById(R.id.item_date);
            }

        }
    }
}
