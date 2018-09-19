package com.russperlow.myagenda;

import android.os.Bundle;
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
                Toast.makeText(this, "Help Button Clicked", Toast.LENGTH_SHORT);
                return true;
            case R.id.toolbar_settings_gear:
                Toast.makeText(this, "Settings Button Clicked", Toast.LENGTH_SHORT);
                return true;
            default:
                return super.onOptionsItemSelected(menuItem);
        }
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
