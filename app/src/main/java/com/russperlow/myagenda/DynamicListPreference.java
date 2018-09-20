package com.russperlow.myagenda;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.DataSetObserver;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class DynamicListPreference extends ListPreference {

    private LayoutInflater layoutInflater;

    public DynamicListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DynamicListPreference(Context context){
        super(context);
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder){

        // Inflate, find and set the main view for this preference
        layoutInflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.dynamic_list_preference_fragment, null);
        builder.setView(view);

        // Find the list view, use the custom adapter and populate it
        ListView listView = (ListView)view.findViewById(R.id.dynamic_list_preference_view);
        PreferenceListAdapter adapter = new PreferenceListAdapter();
        listView.setAdapter(adapter);

        builder.create();
    }

    class PreferenceListAdapter implements ListAdapter {

        List<String> itemsList = new ArrayList<>();

        public PreferenceListAdapter(){
            itemsList.add("One");
            itemsList.add("Two");
        }

        @Override
        public boolean areAllItemsEnabled() {
            return false;
        }

        @Override
        public boolean isEnabled(int i) {
            return false;
        }

        @Override
        public void registerDataSetObserver(DataSetObserver dataSetObserver) {

        }

        @Override
        public void unregisterDataSetObserver(DataSetObserver dataSetObserver) {

        }

        @Override
        public int getCount() {
            return itemsList.size();
        }

        @Override
        public Object getItem(int i) {
            return itemsList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if(view == null){
                view = layoutInflater.inflate(R.layout.dynamic_list_row, viewGroup, false);
            }

            final ViewHolder viewHolder = new ViewHolder(itemsList.get(i), 0xFFFF00FF);

            ImageView colorTag = (ImageView)view.findViewById(R.id.item_tag);
            TextView itemTypeName = (TextView)view.findViewById(R.id.item_type_name);

            colorTag.setColorFilter(viewHolder.colorValue);
            itemTypeName.setText(viewHolder.text);

            return view;
        }

        @Override
        public int getItemViewType(int i) {
            return 0;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public boolean isEmpty() {
            return getCount() == 0;
        }
    }

    class ViewHolder{
        String text;
        int colorValue;

        public ViewHolder(String text, int colorValue){
            this.text = text;
            this.colorValue = colorValue;
        }
    }
}
