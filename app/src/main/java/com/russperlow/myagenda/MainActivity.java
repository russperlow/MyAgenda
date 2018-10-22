package com.russperlow.myagenda;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        NotificationID.setStartValue(sharedPreferences.getInt(NotificationID.NOTIFICATION_COUTNTER, 0));

        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_layout, MainFragment.newInstance());
        transaction.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        // Inflate the custom toolbar we created
        getMenuInflater().inflate(R.menu.main_toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem){

        int id = menuItem.getItemId();

        switch (id){
            case R.id.toolbar_help_button:
                Toast.makeText(getApplicationContext(), "Help Button Clicked", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.toolbar_settings_gear:
                startActivity(new Intent(this, SettingsPreferenceActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                //Toast.makeText(getApplicationContext(), "Settings Button Clicked", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }

    @Override
    public void onPause(){
        super.onPause();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putInt(NotificationID.NOTIFICATION_COUTNTER, NotificationID.nextValue());
        editor.commit();
    }

    // Display calendar for user to choose due date from
    public void showDatePicker(View view){
        DialogFragment fragment = new MainFragment.DatePickerFragment();
        fragment.show(getSupportFragmentManager(), "date picker");
    }

    // Display the picker for user to choose the due time from
    public void showTimePicker(View view){
        DialogFragment fragment = new MainFragment.TimePickerFragment();
        fragment.show(getSupportFragmentManager(), "time picker");
    }

}
