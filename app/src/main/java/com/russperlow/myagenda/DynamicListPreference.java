package com.russperlow.myagenda;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.DataSetObserver;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DynamicListPreference extends ListPreference {

    /**
     * The layout inflater object for getting all views
     */
    private LayoutInflater layoutInflater;

    /**
     * Shared preferences for getting and storing preferences
     */
    private SharedPreferences sharedPreferences;

    /**
     * The string used for accessing and storing the number of items we have
     */
    private final String ITEM_TYPE_COUNT = getContext().getResources().getString(R.string.pref_key_item_names_count);

    /**
     * The string used for accessing and storing all item names
     */
    private final String ITEM_TYPE_NAME_PREFIX = getContext().getResources().getString(R.string.pref_key_item_names_prefix);

    /**
     * The adapter for this main dialog pop-up
     */
    final PreferenceListAdapter adapter = new PreferenceListAdapter();

    ListView listView;
    View preferenceView;

    public DynamicListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DynamicListPreference(Context context){
        super(context);
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder){

        sharedPreferences = getSharedPreferences();

        int itemListCount = sharedPreferences.getInt(ITEM_TYPE_COUNT, 0);

        if(itemListCount > 0) {
            List<String> itemsLoadList = new ArrayList<>();
            for (int i = 0; i < itemListCount; i++) {
                itemsLoadList.add(sharedPreferences.getString(ITEM_TYPE_NAME_PREFIX + i, ""));
            }
            adapter.initList(itemsLoadList);
        }
        else{
            adapter.initList(Arrays.asList(getContext().getResources().getStringArray(R.array.drop_down_array)));
        }


        // Inflate, find and set the main view for this preference
        layoutInflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        preferenceView = layoutInflater.inflate(R.layout.dynamic_list_preference_fragment, null);
        builder.setView(preferenceView);

        // Find the list view, use the custom adapter and populate it
        listView = (ListView)preferenceView.findViewById(R.id.dynamic_list_preference_view);
        listView.setAdapter(adapter);

        // Button for adding agenda items
        final Button addNewItemType = (Button)preferenceView.findViewById(R.id.dynamic_list_preference_button);
        addNewItemType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View parentView) {
                AlertDialog.Builder addTypeBuilder = new AlertDialog.Builder(getContext());

                final View addTypeView = layoutInflater.inflate(R.layout.add_new_item_type, null);
                addTypeBuilder.setView(addTypeView);
                addTypeBuilder.setPositiveButton(getContext().getResources().getString(R.string.SAVE), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String newType = ((EditText)(addTypeView.findViewById(R.id.new_item_type_name))).getText().toString();
                        adapter.addElement(newType);
                    }
                });
                addTypeBuilder.setNegativeButton(getContext().getResources().getString(R.string.CANCEL), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });

                AlertDialog addTypeDialog = addTypeBuilder.create();
                addTypeDialog.show();
            }
        });

        builder.create();
    }

    @Override
    protected void onDialogClosed(boolean positiveResult){
        sharedPreferences = getSharedPreferences();
        SharedPreferences.Editor editor = sharedPreferences.edit();

        int itemTypeCount = adapter.getCount();
        editor.putInt(ITEM_TYPE_COUNT, adapter.getCount());

        // Loop through all strings and put them in with the prefix + their number
        for(int i = 0; i < itemTypeCount; i++){
            editor.putString(ITEM_TYPE_NAME_PREFIX + i, adapter.getItem(i));
        }
        editor.commit();

    }

    class PreferenceListAdapter implements ListAdapter {

        private List<String> itemsList = new ArrayList<>();

        public PreferenceListAdapter(){
        }

        /**
         * Used to populate the items list
         *
         * @param initItemsList that we should copy over
         */
        public void initList(List<String> initItemsList){
            itemsList.clear();
            for(int i = 0; i < initItemsList.size(); i++){
                itemsList.add(initItemsList.get(i));
            }
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
        public String getItem(int i) {
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

            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle(getContext().getResources().getString(R.string.pref_title_delete_all));
                    builder.setMessage(getContext().getResources().getString(R.string.new_item_confirm_delete_message));
                    builder.setPositiveButton(getContext().getResources().getString(R.string.YES), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            removeElement(viewHolder.text);
                            refreshView();
                        }
                    });
                    builder.setNegativeButton(getContext().getResources().getString(R.string.NO), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                    return false;
                }
            });

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

        /**
         * Add the given element to the list
         *
         * @param string a new item type name
         */
        public void addElement(String string){
            itemsList.add(string);
            notifyChanged();
        }

        /**
         * Remove the given element from the list
         *
         * @param string to remove
         */
        public void removeElement(String string){
            if(!itemsList.contains(string))
                return;

            itemsList.remove(itemsList.indexOf(string));
            notifyChanged();
        }

        public void changed(){
            notifyChanged();
        }
    }

    /**
     * Refreshes the view to reflect a change made
     */
    protected void refreshView(){
        listView.invalidateViews();

        adapter.changed();
        listView = (ListView)preferenceView.findViewById(R.id.items_list);
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
